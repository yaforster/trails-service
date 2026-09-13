package io.github.yaforster.trails.adapter.api.rest.run;

import io.github.yaforster.trails.app.TestExecutionEventPublisher;
import io.github.yaforster.trails.app.services.TestExecutionFacadeService;
import io.github.yaforster.trails.app.services.TestService;
import io.github.yaforster.trails.core.TrailsTest;
import io.github.yaforster.trails.core.persisted.PersistedTestRunResult;
import io.github.yaforster.trails.core.test.TestPlanRunDefinition;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.Mockito.*;

class TestExecutionFacadeServiceTest extends TrailsTest {

	private final TestService testService = mock(TestService.class);

	private final TestExecutionEventPublisher eventPublisher = mock(TestExecutionEventPublisher.class);

	private final TestExecutionFacadeServiceImpl service = new TestExecutionFacadeServiceImpl(testService,
			eventPublisher);

	@Test
	void executeTest_ShouldWireCallbacksToPublisher() {
		UUID executionId = executionId();
		TestPlanRunDefinition runDefinition = runDefinition();

		service.executeTest(new TestExecutionFacadeService.TestExecution(executionId, runDefinition));

		ArgumentCaptor<TestService.TestRunExecution> testRunExecutionCaptor = ArgumentCaptor
			.forClass(TestService.TestRunExecution.class);

		verify(testService).runTest(testRunExecutionCaptor.capture());

		PersistedTestRunResult persistedResult = mock(PersistedTestRunResult.class);
		Exception exception = new RuntimeException("boom");
		testRunExecutionCaptor.getValue().lifecycle().onStart().run();
		testRunExecutionCaptor.getValue().lifecycle().onSuccess().accept(persistedResult);
		testRunExecutionCaptor.getValue().lifecycle().onFailure().accept(exception);

		verify(eventPublisher).publishStarted(executionId);
		verify(eventPublisher).publishCompleted(executionId, persistedResult);
		verify(eventPublisher).publishFailed(executionId);
	}

	@Test
	void executeTest_ShouldPassNullSuccessResultToPublisher() {
		UUID executionId = executionId();
		TestPlanRunDefinition runDefinition = runDefinition();

		service.executeTest(new TestExecutionFacadeService.TestExecution(executionId, runDefinition));

		ArgumentCaptor<TestService.TestRunExecution> testRunExecutionCaptor = ArgumentCaptor
			.forClass(TestService.TestRunExecution.class);
		verify(testService).runTest(testRunExecutionCaptor.capture());

		assertDoesNotThrow(() -> testRunExecutionCaptor.getValue().lifecycle().onSuccess().accept(null));
		verify(eventPublisher).publishCompleted(executionId, null);
	}

	private UUID executionId() {
		return getInstancioOf(UUID.class).create();
	}

	private TestPlanRunDefinition runDefinition() {
		return new TestPlanRunDefinition(getInstancioOf(Long.class).create(), List.of());
	}

}
