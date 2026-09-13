package io.github.yaforster.trails.adapter.api.asyncapi.run;

import io.github.yaforster.trails.adapter.api.TestExecutionEventHub;
import io.github.yaforster.trails.adapter.api.asyncapi.IReceiveTestExecutionCompletedEvents;
import io.github.yaforster.trails.adapter.api.asyncapi.IReceiveTestExecutionFailedEvents;
import io.github.yaforster.trails.adapter.api.asyncapi.IReceiveTestExecutionStartedEvents;
import io.github.yaforster.trails.adapter.api.asyncapi.model.TestExecutionCompletedEventAsyncDTO;
import io.github.yaforster.trails.adapter.api.asyncapi.model.TestExecutionFailedEventAsyncDTO;
import io.github.yaforster.trails.adapter.api.asyncapi.model.TestExecutionStartedEventAsyncDTO;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.UUID;

@Component
@AllArgsConstructor
public class TestExecutionEventListener implements IReceiveTestExecutionStartedEvents,
		IReceiveTestExecutionCompletedEvents, IReceiveTestExecutionFailedEvents {

	private final TestExecutionEventHub eventHub;

	@Override
	public void receiveTestExecutionStartedEvents(TestExecutionStartedEventAsyncDTO value) {
		if (isMissingExecutionId(value == null ? null : value.getExecutionId())) {
			return;
		}

		parseExecutionId(value.getExecutionId()).ifPresent(executionId -> eventHub.publishStarted(executionId, value));
	}

	@Override
	public void receiveTestExecutionCompletedEvents(TestExecutionCompletedEventAsyncDTO value) {
		if (isMissingExecutionId(value == null ? null : value.getExecutionId())) {
			return;
		}

		parseExecutionId(value.getExecutionId())
			.ifPresent(executionId -> eventHub.publishCompleted(executionId, value));
	}

	@Override
	public void receiveTestExecutionFailedEvents(TestExecutionFailedEventAsyncDTO value) {
		if (isMissingExecutionId(value == null ? null : value.getExecutionId())) {
			return;
		}

		parseExecutionId(value.getExecutionId()).ifPresent(executionId -> eventHub.publishFailed(executionId, value));
	}

	private boolean isMissingExecutionId(String executionId) {
		return executionId == null || executionId.isBlank();
	}

	private Optional<UUID> parseExecutionId(String executionId) {
		try {
			return Optional.of(UUID.fromString(executionId));
		}
		catch (IllegalArgumentException exception) {
			return Optional.empty();
		}
	}

}
