package io.github.yaforster.trails.adapter.api.rest.run;

import io.github.yaforster.trails.app.TestExecutionEventPublisher;
import io.github.yaforster.trails.app.services.TestExecutionFacadeService;
import io.github.yaforster.trails.app.services.TestService;
import io.github.yaforster.trails.core.persisted.PersistedTestRunResult;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.UUID;
import java.util.function.Consumer;

@AllArgsConstructor
@Service
public class TestExecutionFacadeServiceImpl implements TestExecutionFacadeService {

	private final TestService testService;

	private final TestExecutionEventPublisher eventPublisher;

	@Override
	public void executeTest(TestExecution testExecution) {
		testService.runTest(new TestService.TestRunExecution(testExecution.runDefinition(),
				new TestService.TestRunLifecycle(onTaskStart(testExecution.executionId()),
						onTaskSuccess(testExecution.executionId()), onTaskError(testExecution.executionId()))));
	}

	private Runnable onTaskStart(UUID executionId) {
		return () -> eventPublisher.publishStarted(executionId);
	}

	private Consumer<PersistedTestRunResult> onTaskSuccess(UUID executionId) {
		return result -> eventPublisher.publishCompleted(executionId, result);
	}

	private Consumer<Exception> onTaskError(UUID executionId) {
		return exception -> eventPublisher.publishFailed(executionId);
	}

}
