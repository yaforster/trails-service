package io.github.yaforster.trails.adapter.test.execution;

import io.github.yaforster.trails.app.services.TimeSource;
import io.github.yaforster.trails.app.services.TestRunResultDatabaseService;
import io.github.yaforster.trails.app.services.TestService;
import io.github.yaforster.trails.app.services.TestStepArrangementService;
import io.github.yaforster.trails.app.services.WebdriverConfigurationFactory;
import io.github.yaforster.trails.core.data.ResultIndicator;
import io.github.yaforster.trails.core.persisted.PersistedTestRunResult;
import io.github.yaforster.trails.core.test.*;
import io.github.yaforster.trails.core.test.result.failure.ValidationFailure;
import io.github.yaforster.trails.core.test.result.success.Success;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.core.task.AsyncTaskExecutor;
import org.springframework.core.task.TaskExecutor;
import org.springframework.core.task.TaskRejectedException;
import org.springframework.scheduling.concurrent.ConcurrentTaskExecutor;

import java.sql.Timestamp;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class TestServiceImplTest {

	@Test
	void runTests_WiresDependenciesAndBuildsResult() {
		WebdriverConfigurationFactory webdriverFactory = Mockito.mock(WebdriverConfigurationFactory.class);
		TestStepArrangementService testStepArranger = Mockito.mock(TestStepArrangementService.class);
		TimeSource systemTimeSource = Mockito.mock(TimeSource.class);
		TestRunResultDatabaseService databaseService = Mockito.mock(TestRunResultDatabaseService.class);
		TaskExecutor taskExecutor = Mockito.mock(TaskExecutor.class);
		AsyncTaskExecutor testSetTaskExecutor = new ConcurrentTaskExecutor(Runnable::run);

		TestPlanRunDefinition runDefinition = new TestPlanRunDefinition(77L, List.of(Browser.CHROME));
		DriverProvider driverProvider = Mockito.mock(DriverProvider.class);
		TestExecutionData testExecutionData = new TestExecutionData(
				List.of(new WebDriverData(driverProvider, null, Browser.CHROME)), List.of(), 123L, 456L, 77L,
				"Plan Label");

		TestCaseResult testCaseResult = new TestCaseResult(new Timestamp(1L), Browser.CHROME,
				List.of(new TestPathResult(List.of(success()), List.of())));

		TestCase testCase = Mockito.mock(TestCase.class);
		when(testCase.run(driverProvider, Browser.CHROME)).thenReturn(testCaseResult);

		TestSet testSet = new TestSet(testCase, driverProvider, Browser.CHROME, "Plan Label");

		when(webdriverFactory.getTestExecutionData(runDefinition)).thenReturn(testExecutionData);
		when(testStepArranger.setUpTestsPerBrowser(testExecutionData)).thenReturn(List.of(testSet));
		when(systemTimeSource.currentTimeMillis()).thenReturn(1700000000000L);
		PersistedTestRunResult persisted = new PersistedTestRunResult(99L, 123L, 456L, 77L,
				new Timestamp(1700000000000L), ResultIndicator.SUCCESS, "Plan Label");
		when(databaseService.storeTestRunResult(Mockito.any(TestRunResult.class))).thenReturn(persisted);

		TestServiceImpl service = new TestServiceImpl(webdriverFactory, testStepArranger, systemTimeSource,
				databaseService, taskExecutor, testSetTaskExecutor);

		PersistedTestRunResult result = service.runTests(runDefinition);

		verify(webdriverFactory).getTestExecutionData(runDefinition);
		verify(testStepArranger).setUpTestsPerBrowser(testExecutionData);
		verify(systemTimeSource).currentTimeMillis();
		verify(testCase).run(driverProvider, Browser.CHROME);
		verify(databaseService).storeTestRunResult(Mockito.any(TestRunResult.class));

		assertEquals(123L, result.applicationId());
		assertEquals(456L, result.stageId());
		assertEquals("Plan Label", result.label());
		assertEquals(ResultIndicator.SUCCESS, result.status());
		assertEquals(new Timestamp(1700000000000L), result.timestamp());
	}

	@Test
	void testDetermineTestRunResultIndicator_AllSuccess() {
		TestPathResult testPathResult = new TestPathResult(List.of(success(), success()), List.of());
		TestCaseResult testCaseResult = new TestCaseResult(null, null, List.of(testPathResult));
		TestSetResult testSetResult = new TestSetResult(testCaseResult, 0, null, "Test Plan A");
		List<TestSetResult> testSetResults = List.of(testSetResult);
		TestServiceImpl testService = new TestServiceImpl(null, null, null, null, null, null);

		ResultIndicator resultIndicator = TestServiceImpl.determineTestRunResultIndicator(testSetResults);

		assertEquals(ResultIndicator.SUCCESS, resultIndicator);
	}

	@Test
	void testDetermineTestRunResultIndicator_AllFailure() {
		TestPathResult testPathResult = new TestPathResult(List.of(failure(), failure()), List.of());
		TestCaseResult testCaseResult = new TestCaseResult(null, null, List.of(testPathResult));
		TestSetResult testSetResult = new TestSetResult(testCaseResult, 0, null, "Test Plan B");
		List<TestSetResult> testSetResults = List.of(testSetResult);
		TestServiceImpl testService = new TestServiceImpl(null, null, null, null, null, null);

		ResultIndicator resultIndicator = TestServiceImpl.determineTestRunResultIndicator(testSetResults);

		assertEquals(ResultIndicator.FAILURE, resultIndicator);
	}

	@Test
	void testDetermineTestRunResultIndicator_PartialSuccess() {
		TestPathResult testPathResult1 = new TestPathResult(List.of(success(), failure()), List.of());
		TestPathResult testPathResult2 = new TestPathResult(List.of(success()), List.of());
		TestCaseResult testCaseResult = new TestCaseResult(null, null, List.of(testPathResult1, testPathResult2));
		TestSetResult testSetResult = new TestSetResult(testCaseResult, 0, null, "Test Plan C");
		List<TestSetResult> testSetResults = List.of(testSetResult);
		TestServiceImpl testService = new TestServiceImpl(null, null, null, null, null, null);

		ResultIndicator resultIndicator = TestServiceImpl.determineTestRunResultIndicator(testSetResults);

		assertEquals(ResultIndicator.PARTIAL_SUCCESS, resultIndicator);
	}

	@Test
	void testDetermineTestRunResultIndicator_EmptyResults() {
		TestPathResult testPathResult = new TestPathResult(List.of(), List.of());
		TestCaseResult testCaseResult = new TestCaseResult(null, null, List.of(testPathResult));
		TestSetResult testSetResult = new TestSetResult(testCaseResult, 0, null, "Test Plan D");
		List<TestSetResult> testSetResults = List.of(testSetResult);
		TestServiceImpl testService = new TestServiceImpl(null, null, null, null, null, null);

		ResultIndicator resultIndicator = TestServiceImpl.determineTestRunResultIndicator(testSetResults);

		assertEquals(ResultIndicator.FAILURE, resultIndicator);
	}

	private Success success() {
		return Success.builder().actionID(1L).label("label").resultMessage("ok").base64Screenshot(null).build();
	}

	private ValidationFailure failure() {
		return ValidationFailure.builder()
			.actionID(1L)
			.label("label")
			.resultMessage("failed")
			.base64Screenshot(null)
			.build();
	}

	@Test
	@SuppressWarnings("unchecked")
	void runTest_shouldInvokeStartAndSuccessCallbacks() {
		WebdriverConfigurationFactory webdriverFactory = Mockito.mock(WebdriverConfigurationFactory.class);
		TestStepArrangementService testStepArranger = Mockito.mock(TestStepArrangementService.class);
		TimeSource systemTimeSource = Mockito.mock(TimeSource.class);
		TestRunResultDatabaseService databaseService = Mockito.mock(TestRunResultDatabaseService.class);
		TaskExecutor taskExecutor = Mockito.mock(TaskExecutor.class);
		AsyncTaskExecutor testSetTaskExecutor = new ConcurrentTaskExecutor(Runnable::run);

		TestPlanRunDefinition runDefinition = new TestPlanRunDefinition(77L, List.of(Browser.CHROME));
		PersistedTestRunResult persisted = new PersistedTestRunResult(99L, 123L, 456L, 77L,
				new Timestamp(1700000000000L), ResultIndicator.SUCCESS, "Plan Label");

		TestExecutionData testExecutionData = new TestExecutionData(List.of(), List.of(), 123L, 456L, 77L,
				"Plan Label");
		when(webdriverFactory.getTestExecutionData(runDefinition)).thenReturn(testExecutionData);
		when(testStepArranger.setUpTestsPerBrowser(testExecutionData)).thenReturn(List.of());
		when(systemTimeSource.currentTimeMillis()).thenReturn(1700000000000L);
		when(databaseService.storeTestRunResult(any(TestRunResult.class))).thenReturn(persisted);

		doAnswer(invocation -> {
			Runnable runnable = invocation.getArgument(0);
			runnable.run();
			return null;
		}).when(taskExecutor).execute(any(Runnable.class));

		TestServiceImpl service = new TestServiceImpl(webdriverFactory, testStepArranger, systemTimeSource,
				databaseService, taskExecutor, testSetTaskExecutor);

		AtomicReference<PersistedTestRunResult> successRef = new AtomicReference<>();
		AtomicReference<Exception> failureRef = new AtomicReference<>();
		AtomicReference<Boolean> startedRef = new AtomicReference<>(false);

		service.runTest(new TestService.TestRunExecution(runDefinition,
				new TestService.TestRunLifecycle(() -> startedRef.set(true), successRef::set, failureRef::set)));

		assertTrue(startedRef.get());
		assertSame(persisted, successRef.get());
		assertNull(failureRef.get());
	}

	@Test
	@SuppressWarnings("unchecked")
	void runTest_shouldInvokeFailureCallback_onException() {
		WebdriverConfigurationFactory webdriverFactory = Mockito.mock(WebdriverConfigurationFactory.class);
		TestStepArrangementService testStepArranger = Mockito.mock(TestStepArrangementService.class);
		TimeSource systemTimeSource = Mockito.mock(TimeSource.class);
		TestRunResultDatabaseService databaseService = Mockito.mock(TestRunResultDatabaseService.class);
		TaskExecutor taskExecutor = Mockito.mock(TaskExecutor.class);
		AsyncTaskExecutor testSetTaskExecutor = new ConcurrentTaskExecutor(Runnable::run);

		TestPlanRunDefinition runDefinition = new TestPlanRunDefinition(77L, List.of(Browser.CHROME));
		RuntimeException expected = new RuntimeException("boom");

		doAnswer(invocation -> {
			Runnable runnable = invocation.getArgument(0);
			runnable.run();
			return null;
		}).when(taskExecutor).execute(any(Runnable.class));
		when(webdriverFactory.getTestExecutionData(runDefinition)).thenThrow(expected);

		TestServiceImpl service = new TestServiceImpl(webdriverFactory, testStepArranger, systemTimeSource,
				databaseService, taskExecutor, testSetTaskExecutor);

		AtomicReference<Exception> failureRef = new AtomicReference<>();

		service.runTest(new TestService.TestRunExecution(runDefinition, new TestService.TestRunLifecycle(() -> {
		}, persisted -> {
		}, failureRef::set)));

		assertSame(expected, failureRef.get());
	}

	@Test
	void runTest_shouldInvokeFailureCallback_whenRunAdmissionIsRejected() {
		TaskExecutor taskExecutor = Mockito.mock(TaskExecutor.class);
		AsyncTaskExecutor testSetTaskExecutor = new ConcurrentTaskExecutor(Runnable::run);
		TestPlanRunDefinition runDefinition = new TestPlanRunDefinition(77L, List.of(Browser.CHROME));
		TaskRejectedException expected = new TaskRejectedException("full queue");
		doThrow(expected).when(taskExecutor).execute(any(Runnable.class));
		TestServiceImpl service = new TestServiceImpl(null, null, null, null, taskExecutor, testSetTaskExecutor);
		AtomicReference<Exception> failureRef = new AtomicReference<>();

		service.runTest(new TestService.TestRunExecution(runDefinition, new TestService.TestRunLifecycle(() -> {
		}, persisted -> {
		}, failureRef::set)));

		assertSame(expected, failureRef.get());
	}

}
