package io.github.yaforster.trails.core.test;

import com.google.common.annotations.VisibleForTesting;
import io.github.yaforster.trails.core.test.result.failure.Failure;
import io.github.yaforster.trails.core.test.result.failure.SkippedResult;
import lombok.AllArgsConstructor;

import java.net.URL;
import java.sql.Timestamp;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@AllArgsConstructor
public class TestCase {

	private final URL applicationURL;

	private final TestStep startStep;

	public TestCaseResult run(DriverProvider webdriverProvider, Browser browserToRunIn) {
		List<TestPathResult> result = runActionsAndCollectResults(webdriverProvider);
		Timestamp timestamp = new Timestamp(System.currentTimeMillis());
		return new TestCaseResult(timestamp, browserToRunIn, result);
	}

	@VisibleForTesting
	List<TestPathResult> runActionsAndCollectResults(DriverProvider webdriverProvider) {
		try {
			return executeActionsAndCollectResults(webdriverProvider);
		}
		catch (Exception e) {
			return new LinkedList<>();
		}
	}

	/**
	 * @return ArrayList of {@link Result} for each action, with the first failed action
	 * in the List. Actions after the first failed action will be ignored, as the desired
	 * application state will not be present to properly test subsequent actions.
	 */
	@VisibleForTesting
	List<TestPathResult> executeActionsAndCollectResults(DriverProvider webdriverProvider) {
		List<List<TestStep>> testPaths = startStep.getTestPathsFromThisStep();
		return testPaths.stream().map(testStepList -> runTestsUntilFailure(webdriverProvider, testStepList)).toList();
	}

	@VisibleForTesting
	TestPathResult runTestsUntilFailure(DriverProvider webdriverProvider, List<TestStep> testStepList) {
		Optional<BrowserExecution> browserExecution = webdriverProvider.createDriver();
		if (browserExecution.isEmpty()) {
			return new TestPathResult(skipAllSteps(testStepList), Collections.emptyList());
		}
		BrowserExecution execution = browserExecution.get();
		try {
			TestExecutionContext context = new TestExecutionContext(execution);
			List<Result> results = runTestsUntilFailure(testStepList, context);
			List<FileData> downloadedFiles = context.downloadedFiles();
			return new TestPathResult(results, downloadedFiles);
		}
		catch (Exception exception) {
			return new TestPathResult(
					skipAllSteps(testStepList,
							"Skipped this action because the browser could not open or execute the test path."),
					Collections.emptyList());
		}
		finally {
			cleanUpBrowser(execution.browser());
		}
	}

	private List<Result> skipAllSteps(List<TestStep> testStepList) {
		return skipAllSteps(testStepList, "Skipped this action due to previous error.");
	}

	private List<Result> skipAllSteps(List<TestStep> testStepList, String message) {
		return testStepList.stream()
			.map(step -> SkippedResult.builder()
				.actionID(step.action().getActionID())
				.label(step.action().getLabel())
				.resultMessage(message)
				.build())
			.collect(Collectors.toCollection(LinkedList::new));
	}

	private void cleanUpBrowser(BrowserSession browser) {
		try {
			browser.clearDownloadedFiles();
		}
		catch (Exception ignored) {
		}
		try {
			browser.close();
		}
		catch (Exception ignored) {
		}
	}

	private List<Result> runTestsUntilFailure(List<TestStep> testStepList, TestExecutionContext context) {
		openWebsite(context.browser());
		List<Result> results = new LinkedList<>();
		boolean continueTesting = true;
		for (TestStep step : testStepList) {
			if (continueTesting) {
				Result stepResult = step.action().execute(context);
				results.add(stepResult);
				continueTesting = shouldContinueTesting(stepResult);
			}
			else {
				SkippedResult skippedResult = getSkipResult(step);
				results.add(skippedResult);
			}
		}
		return results;
	}

	private boolean shouldContinueTesting(Result result) {
		return !(result instanceof Failure);
	}

	@VisibleForTesting
	SkippedResult getSkipResult(TestStep step) {
		return SkippedResult.builder()
			.actionID(step.action().getActionID())
			.label(step.action().getLabel())
			.resultMessage("Skipped this action due to previous error.")
			.build();
	}

	@VisibleForTesting
	void openWebsite(BrowserSession browser) {
		browser.open(applicationURL.toString());
	}

}
