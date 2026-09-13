package io.github.yaforster.trails.adapter.api;

import com.google.common.annotations.VisibleForTesting;
import io.github.yaforster.trails.adapter.api.asyncapi.model.TestExecutionCompletedEventAsyncDTO;
import io.github.yaforster.trails.adapter.api.asyncapi.model.TestExecutionFailedEventAsyncDTO;
import io.github.yaforster.trails.adapter.api.asyncapi.model.TestExecutionStartedEventAsyncDTO;
import io.github.yaforster.trails.app.services.TimeSource;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

@Component
@RequiredArgsConstructor
public class TestExecutionEventHub {

	private static final long SSE_TIMEOUT_MS = 30 * 60 * 1000L;

	private static final long TERMINAL_EVENT_REPLAY_WINDOW_MS = 10 * 60 * 1000L;

	private static final int MAXIMUM_TERMINAL_EVENT_COUNT = 100;

	private static final String EVENT_TEST_STARTED = "test.started";

	private static final String EVENT_TEST_COMPLETED = "test.completed";

	private static final String EVENT_TEST_FAILED = "test.failed";

	private static final String EVENT_TEST_TERMINAL = "test.terminal";

	private final Map<UUID, List<SseEmitter>> emittersByExecutionId = new ConcurrentHashMap<>();

	private final Map<UUID, TerminalEvent> terminalEventsByExecutionId = new LinkedHashMap<>();

	private final TimeSource timeSource;

	public SseEmitter subscribe(UUID executionId) {
		SseEmitter emitter = createAndRegisterEmitter(executionId);
		replayTerminalEventIfPresent(executionId, emitter);
		return emitter;
	}

	public void publishStarted(UUID executionId, TestExecutionStartedEventAsyncDTO event) {
		publish(executionId, EVENT_TEST_STARTED, event);
	}

	public void publishCompleted(UUID executionId, TestExecutionCompletedEventAsyncDTO event) {
		publishTerminalEvent(executionId, EVENT_TEST_COMPLETED, event);
	}

	public void publishFailed(UUID executionId, TestExecutionFailedEventAsyncDTO event) {
		publishTerminalEvent(executionId, EVENT_TEST_FAILED, event);
	}

	@VisibleForTesting
	protected SseEmitter createAndRegisterEmitter(UUID executionId) {
		SseEmitter emitter = new SseEmitter(SSE_TIMEOUT_MS);
		emittersByExecutionId.computeIfAbsent(executionId, _ -> new CopyOnWriteArrayList<>()).add(emitter);
		registerLifecycleCallbacks(executionId, emitter);
		return emitter;
	}

	@VisibleForTesting
	protected void registerLifecycleCallbacks(UUID executionId, SseEmitter emitter) {
		emitter.onCompletion(() -> removeEmitter(executionId, emitter));
		emitter.onTimeout(() -> removeEmitter(executionId, emitter));
		emitter.onError(_ -> removeEmitter(executionId, emitter));
	}

	@VisibleForTesting
	protected void replayTerminalEventIfPresent(UUID executionId, SseEmitter emitter) {
		Object terminalEvent = terminalEvent(executionId);
		if (terminalEvent == null) {
			return;
		}

		trySend(emitter, EVENT_TEST_TERMINAL, terminalEvent);
		completeQuietly(emitter);
		removeEmitter(executionId, emitter);
	}

	@VisibleForTesting
	protected void publishTerminalEvent(UUID executionId, String eventName, Object event) {
		storeTerminalEvent(executionId, event);
		publish(executionId, eventName, event);
		cleanupEmittersByExecutionId(executionId);
	}

	@VisibleForTesting
	protected Object terminalEvent(UUID executionId) {
		synchronized (terminalEventsByExecutionId) {
			removeExpiredTerminalEvents();
			TerminalEvent terminalEvent = terminalEventsByExecutionId.get(executionId);
			return terminalEvent == null ? null : terminalEvent.event();
		}
	}

	@VisibleForTesting
	protected int terminalEventCount() {
		synchronized (terminalEventsByExecutionId) {
			removeExpiredTerminalEvents();
			return terminalEventsByExecutionId.size();
		}
	}

	private void storeTerminalEvent(UUID executionId, Object event) {
		Objects.requireNonNull(executionId);
		Objects.requireNonNull(event);
		synchronized (terminalEventsByExecutionId) {
			removeExpiredTerminalEvents();
			terminalEventsByExecutionId.remove(executionId);
			terminalEventsByExecutionId.put(executionId, new TerminalEvent(event, timeSource.currentTimeMillis()));
			removeOldestTerminalEventsAboveCapacity();
		}
	}

	private void removeExpiredTerminalEvents() {
		long expiryTimestamp = timeSource.currentTimeMillis() - TERMINAL_EVENT_REPLAY_WINDOW_MS;
		terminalEventsByExecutionId.entrySet().removeIf(entry -> entry.getValue().publishedAt() <= expiryTimestamp);
	}

	private void removeOldestTerminalEventsAboveCapacity() {
		Iterator<UUID> executionIds = terminalEventsByExecutionId.keySet().iterator();
		while (terminalEventsByExecutionId.size() > MAXIMUM_TERMINAL_EVENT_COUNT && executionIds.hasNext()) {
			executionIds.next();
			executionIds.remove();
		}
	}

	@VisibleForTesting
	protected void publish(UUID executionId, String eventName, Object event) {
		List<SseEmitter> executionEmitters = emittersByExecutionId.get(executionId);
		if (executionEmitters == null || executionEmitters.isEmpty()) {
			return;
		}

		executionEmitters.removeIf(emitter -> !trySend(emitter, eventName, event));
		if (executionEmitters.isEmpty()) {
			emittersByExecutionId.remove(executionId);
		}
	}

	@VisibleForTesting
	protected boolean trySend(SseEmitter emitter, String eventName, Object event) {
		try {
			emitter.send(prepareEventBuilder(eventName, event));
			return true;
		}
		catch (IOException ioException) {
			emitter.completeWithError(ioException);
			return false;
		}
		catch (IllegalStateException ignored) {
			return false;
		}
	}

	@VisibleForTesting
	protected SseEmitter.SseEventBuilder prepareEventBuilder(String eventName, Object event) {
		return SseEmitter.event().name(eventName).data(event);
	}

	@VisibleForTesting
	protected void cleanupEmittersByExecutionId(UUID executionId) {
		List<SseEmitter> removedEmitters = emittersByExecutionId.remove(executionId);
		if (removedEmitters == null || removedEmitters.isEmpty()) {
			return;
		}
		removedEmitters.forEach(this::completeQuietly);
	}

	@VisibleForTesting
	protected void completeQuietly(SseEmitter emitter) {
		try {
			emitter.complete();
		}
		catch (IllegalStateException ignored) {
			// already completed/disconnected
		}
	}

	@VisibleForTesting
	protected void removeEmitter(UUID executionId, SseEmitter emitter) {
		emittersByExecutionId.computeIfPresent(executionId, (_, executionEmitters) -> {
			executionEmitters.remove(emitter);
			return executionEmitters.isEmpty() ? null : executionEmitters;
		});
	}

	private record TerminalEvent(Object event, long publishedAt) {
	}

}
