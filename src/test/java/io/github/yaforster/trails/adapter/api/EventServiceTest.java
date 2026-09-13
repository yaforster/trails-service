package io.github.yaforster.trails.adapter.api;

import io.github.yaforster.trails.adapter.api.asyncapi.model.TestExecutionCompletedEventAsyncDTO;
import io.github.yaforster.trails.adapter.api.asyncapi.model.TestExecutionStartedEventAsyncDTO;
import io.github.yaforster.trails.app.services.TimeSource;
import org.junit.jupiter.api.Test;

import java.util.UUID;
import java.util.concurrent.atomic.AtomicLong;

import static org.assertj.core.api.Assertions.assertThat;

class EventServiceTest {

	@Test
	void shouldNotRetainStartedEvents() {
		TestExecutionEventHub eventHub = eventHub(0L);

		eventHub.publishStarted(UUID.randomUUID(), startedEvent());

		assertThat(eventHub.terminalEventCount()).isZero();
	}

	@Test
	void shouldRetainTerminalEventDuringReplayWindow() {
		TestExecutionEventHub eventHub = eventHub(0L);
		UUID executionId = UUID.randomUUID();
		TestExecutionCompletedEventAsyncDTO event = completedEvent();

		eventHub.publishCompleted(executionId, event);

		assertThat(eventHub.terminalEvent(executionId)).isSameAs(event);
	}

	@Test
	void shouldExpireTerminalEventAfterReplayWindow() {
		AtomicLong clock = new AtomicLong(0L);
		TestExecutionEventHub eventHub = eventHub(clock);
		UUID executionId = UUID.randomUUID();
		eventHub.publishCompleted(executionId, completedEvent());
		clock.set(10 * 60 * 1000L);

		assertThat(eventHub.terminalEvent(executionId)).isNull();
	}

	@Test
	void shouldEvictOldestTerminalEventAboveCapacity() {
		TestExecutionEventHub eventHub = eventHub(0L);
		UUID oldestExecutionId = UUID.randomUUID();
		eventHub.publishCompleted(oldestExecutionId, completedEvent());
		for (int index = 0; index < 100; index++) {
			eventHub.publishCompleted(UUID.randomUUID(), completedEvent());
		}

		assertThat(eventHub.terminalEvent(oldestExecutionId)).isNull();
	}

	@Test
	void shouldLimitTerminalEventsToConfiguredCapacity() {
		TestExecutionEventHub eventHub = eventHub(0L);
		for (int index = 0; index < 101; index++) {
			eventHub.publishCompleted(UUID.randomUUID(), completedEvent());
		}

		assertThat(eventHub.terminalEventCount()).isEqualTo(100);
	}

	private TestExecutionEventHub eventHub(long currentTimeMillis) {
		return eventHub(new AtomicLong(currentTimeMillis));
	}

	private TestExecutionEventHub eventHub(AtomicLong clock) {
		TimeSource timeSource = clock::get;
		return new TestExecutionEventHub(timeSource);
	}

	private TestExecutionStartedEventAsyncDTO startedEvent() {
		return TestExecutionStartedEventAsyncDTO.builder().executionId(UUID.randomUUID().toString()).build();
	}

	private TestExecutionCompletedEventAsyncDTO completedEvent() {
		return TestExecutionCompletedEventAsyncDTO.builder().executionId(UUID.randomUUID().toString()).build();
	}

}
