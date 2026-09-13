package io.github.yaforster.trails.app;

import io.github.yaforster.trails.core.persisted.PersistedTestRunResult;

import java.util.UUID;

public interface TestExecutionEventPublisher {

	void publishStarted(UUID executionId);

	void publishCompleted(UUID executionId, PersistedTestRunResult persistedResult);

	void publishFailed(UUID executionId);

}
