package io.github.yaforster.trails.adapter.api.asyncapi.run;

import io.github.yaforster.trails.adapter.api.TestExecutionEventHub;
import io.github.yaforster.trails.adapter.api.asyncapi.model.TestExecutionCompletedEventAsyncDTO;
import io.github.yaforster.trails.adapter.api.asyncapi.model.TestExecutionFailedEventAsyncDTO;
import io.github.yaforster.trails.adapter.api.asyncapi.model.TestExecutionStartedEventAsyncDTO;
import io.github.yaforster.trails.app.TestExecutionEventPublisher;
import io.github.yaforster.trails.core.persisted.PersistedTestRunResult;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@AllArgsConstructor
public class TestExecutionEventPublisherImpl implements TestExecutionEventPublisher {

	private final TestExecutionEventFacade eventFacade;

	private final TestExecutionEventHub eventHub;

	@Override
	public void publishStarted(UUID executionId) {
		TestExecutionStartedEventAsyncDTO started = eventFacade.toStartedEventDTO(executionId);
		eventHub.publishStarted(executionId, started);
	}

	@Override
	public void publishCompleted(UUID executionId, PersistedTestRunResult persistedResult) {
		TestExecutionCompletedEventAsyncDTO completed = eventFacade.toCompletedEventDTO(executionId, persistedResult);
		eventHub.publishCompleted(executionId, completed);
	}

	@Override
	public void publishFailed(UUID executionId) {
		TestExecutionFailedEventAsyncDTO failed = eventFacade.toFailedEventDTO(executionId);
		eventHub.publishFailed(executionId, failed);
	}

}
