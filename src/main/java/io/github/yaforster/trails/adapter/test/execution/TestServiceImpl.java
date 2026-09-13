package io.github.yaforster.trails.adapter.test.execution;

import com.google.common.annotations.VisibleForTesting;
import io.github.yaforster.trails.app.services.TestRunResultDatabaseService;
import io.github.yaforster.trails.app.services.TestService;
import io.github.yaforster.trails.app.services.TestStepArrangementService;
import io.github.yaforster.trails.app.services.TimeSource;
import io.github.yaforster.trails.app.services.WebdriverConfigurationFactory;
import io.github.yaforster.trails.core.data.ResultIndicator;
import io.github.yaforster.trails.core.persisted.PersistedTestRunResult;
import io.github.yaforster.trails.core.test.*;
import io.github.yaforster.trails.core.test.result.success.Success;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.task.AsyncTaskExecutor;
import org.springframework.core.task.TaskExecutor;
import org.springframework.core.task.TaskRejectedException;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.stream.Collectors;

@Service
@Slf4j
public class TestServiceImpl implements TestService {

	private final WebdriverConfigurationFactory webdriverFactory;

	private final TestStepArrangementService testStepArranger;

	private final TimeSource timeSource;

	private final TestRunResultDatabaseService testRunResultDatabaseService;

	private final TaskExecutor testRunTaskExecutor;

	private final AsyncTaskExecutor testSetTaskExecutor;

	public TestServiceImpl(WebdriverConfigurationFactory webdriverFactory, TestStepArrangementService testStepArranger,
			TimeSource timeSource, TestRunResultDatabaseService testRunResultDatabaseService,
			@Qualifier("testRunTaskExecutor") TaskExecutor testRunTaskExecutor,
			@Qualifier("testSetTaskExecutor") AsyncTaskExecutor testSetTaskExecutor) {
		this.webdriverFactory = webdriverFactory;
		this.testStepArranger = testStepArranger;
		this.timeSource = timeSource;
		this.testRunResultDatabaseService = testRunResultDatabaseService;
		this.testRunTaskExecutor = testRunTaskExecutor;
		this.testSetTaskExecutor = testSetTaskExecutor;
	}

	private static TestRunResult toTestRunResult(List<TestSetResult> testSetResults, Timestamp timestamp,
			TestExecutionData testExecutionData) {
		ResultIndicator indicator = determineTestRunResultIndicator(testSetResults);
		return new TestRunResult(testSetResults, timestamp, indicator, testExecutionData.applicationID(),
				testExecutionData.stageID(), testExecutionData.testPlanID(), testExecutionData.testPlanLabel());
	}

	@VisibleForTesting
	protected static ResultIndicator determineTestRunResultIndicator(List<TestSetResult> testSetResults) {
		ResultStats resultStats = summarizeResultStats(testSetResults);
		if (resultStats.total() == 0) {
			return ResultIndicator.FAILURE;
		}
		if (resultStats.total() == resultStats.successes()) {
			return ResultIndicator.SUCCESS;
		}
		if (resultStats.successes() == 0) {
			return ResultIndicator.FAILURE;
		}
		return ResultIndicator.PARTIAL_SUCCESS;
	}

	private static ResultStats summarizeResultStats(List<TestSetResult> testSetResults) {
		return testSetResults.stream()
			.flatMap(testSetResult -> testSetResult.testCaseResult().results().stream())
			.flatMap(testPathResult -> testPathResult.results().stream())
			.collect(Collectors.teeing(Collectors.counting(),
					Collectors.filtering(Success.class::isInstance, Collectors.counting()), ResultStats::new));
	}

	@Override
	public void runTest(TestRunExecution testRunExecution) {
		try {
			testRunTaskExecutor.execute(() -> {
				testRunExecution.lifecycle().onStart().run();
				try {
					PersistedTestRunResult result = runTests(testRunExecution.runDefinition());
					testRunExecution.lifecycle().onSuccess().accept(result);
				}
				catch (Exception exception) {
					log.error("Failed to execute test run", exception);
					testRunExecution.lifecycle().onFailure().accept(exception);
				}
			});
		}
		catch (TaskRejectedException exception) {
			log.warn("Rejected test run because the execution queue is full");
			testRunExecution.lifecycle().onFailure().accept(exception);
		}
	}

	@VisibleForTesting
	protected PersistedTestRunResult runTests(TestPlanRunDefinition runDefinition) {
		TestExecutionData testExecutionData = webdriverFactory.getTestExecutionData(runDefinition);
		List<TestSet> testSets = testStepArranger.setUpTestsPerBrowser(testExecutionData);
		Timestamp timestamp = currentTimestamp();
		List<Future<TestSetResult>> testSetResultFutures = testSets.stream()
			.map(testSet -> testSetTaskExecutor.submit(testSet::runTests))
			.toList();
		List<TestSetResult> testSetResults = testSetResultFutures.stream().map(this::awaitTestSetResult).toList();
		TestRunResult testRunResult = toTestRunResult(testSetResults, timestamp, testExecutionData);
		return testRunResultDatabaseService.storeTestRunResult(testRunResult);
	}

	private Timestamp currentTimestamp() {
		return new Timestamp(timeSource.currentTimeMillis());
	}

	private TestSetResult awaitTestSetResult(Future<TestSetResult> future) {
		try {
			return future.get();
		}
		catch (InterruptedException exception) {
			Thread.currentThread().interrupt();
			throw new IllegalStateException("Interrupted while waiting for browser test execution", exception);
		}
		catch (ExecutionException exception) {
			throw new IllegalStateException("Browser test execution failed", exception.getCause());
		}
	}

	private record ResultStats(long total, long successes) {
	}

}
