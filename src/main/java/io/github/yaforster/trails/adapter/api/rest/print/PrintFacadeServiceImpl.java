package io.github.yaforster.trails.adapter.api.rest.print;

import com.google.common.annotations.VisibleForTesting;
import io.github.yaforster.trails.app.services.*;
import io.github.yaforster.trails.core.GeneratedFile;
import io.github.yaforster.trails.core.PagedResult;
import io.github.yaforster.trails.core.persisted.PersistedActionResult;
import io.github.yaforster.trails.core.persisted.PersistedTestPathResult;
import io.github.yaforster.trails.core.persisted.PersistedTestRunResult;
import io.github.yaforster.trails.core.persisted.PersistedTestSetResult;
import io.github.yaforster.trails.core.print.TestPathPrintContext;
import io.github.yaforster.trails.core.print.TestRunPrintContext;
import io.github.yaforster.trails.core.print.TestSetPrintContext;
import io.github.yaforster.trails.core.test.Browser;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.IntFunction;
import java.util.stream.Collectors;

@AllArgsConstructor
@Service
public class PrintFacadeServiceImpl implements PrintFacadeService {

	private static final int PRINT_QUERY_PAGE_SIZE = 100;

	private final TestSetResultQueryService testSetResultQueryService;

	private final TestPathResultQueryService testPathResultQueryService;

	private final ActionResultQueryService actionResultQueryService;

	private final TestRunResultDatabaseService testRunResultDatabaseService;

	private final PrintService printService;

	private final HierarchyValidationService hierarchyValidationService;

	@Override
	public Optional<GeneratedFile> printPath(TestPathPrint testPathPrint) {
		if (!pathBelongsToHierarchy(testPathPrint.applicationId(), testPathPrint.stageId(), testPathPrint.testRunId(),
				testPathPrint.testSetResultId(), testPathPrint.pathResultId())) {
			return Optional.empty();
		}
		Optional<TestPathPrintContext> testPathPrintContext = buildTestPathPrintContext(testPathPrint.pathResultId(),
				testPathPrint.testSetResultId());
		if (testPathPrintContext.isEmpty()) {
			return Optional.empty();
		}
		return Optional
			.of(new GeneratedFile("test-path-" + testPathPrint.pathResultId() + ".pdf", outputStream -> printService
				.renderTestPathPdf(new PrintService.TestPathPdf(testPathPrintContext.get(), outputStream))));
	}

	@Override
	public Optional<GeneratedFile> printTestSet(TestSetPrint testSetPrint) {
		if (!testSetBelongsToRun(testSetPrint.applicationId(), testSetPrint.stageId(), testSetPrint.testRunId(),
				testSetPrint.testSetResultId())) {
			return Optional.empty();
		}
		Optional<TestSetPrintContext> testSetPrintContext = buildTestSetPrintContext(testSetPrint.testSetResultId());
		if (testSetPrintContext.isEmpty()) {
			return Optional.empty();
		}
		return Optional
			.of(new GeneratedFile("test-set-" + testSetPrint.testSetResultId() + ".pdf", outputStream -> printService
				.renderTestSetPdf(new PrintService.TestSetPdf(testSetPrintContext.get(), outputStream))));
	}

	@Override
	public Optional<GeneratedFile> printTestRun(TestRunPrint testRunPrint) {
		Optional<TestRunPrintContext> testRunPrintContext = buildTestRunPrintContext(testRunPrint.applicationId(),
				testRunPrint.stageId(), testRunPrint.testRunId());
		if (testRunPrintContext.isEmpty()) {
			return Optional.empty();
		}

		return Optional
			.of(new GeneratedFile("test-run-" + testRunPrint.testRunId() + ".pdf", outputStream -> printService
				.renderTestRunPdf(new PrintService.TestRunPdf(testRunPrintContext.get(), outputStream))));
	}

	@VisibleForTesting
	protected Optional<TestPathPrintContext> buildTestPathPrintContext(Long pathResultId, Long testSetResultId) {
		Optional<PersistedTestPathResult> pathResult = testPathResultQueryService.getPathResult(pathResultId);
		Optional<List<PersistedActionResult>> actionResults = actionResultQueryService
			.listActionResultChain(pathResultId);
		Optional<Browser> executedBrowser = resolveExecutedBrowserForSetResultId(testSetResultId);
		if (pathResult.isEmpty() || actionResults.isEmpty() || executedBrowser.isEmpty()) {
			return Optional.empty();
		}

		TestPathPrintContext testPathPrintContext = new TestPathPrintContext(pathResult.get().id(),
				pathResult.get().testSetResultId(), pathResult.get().pathLabel(), executedBrowser.get(),
				actionResults.get());
		return Optional.of(testPathPrintContext);
	}

	@VisibleForTesting
	protected Optional<TestSetPrintContext> buildTestSetPrintContext(Long testSetResultId) {
		Optional<PersistedTestSetResult> testSetResult = testSetResultQueryService.getTestSetResult(testSetResultId);
		return testSetResult.flatMap(this::buildTestSetPrintContextFromResult);
	}

	@VisibleForTesting
	protected Optional<TestRunPrintContext> buildTestRunPrintContext(Long applicationId, Long stageId, Long testRunId) {
		Optional<PersistedTestRunResult> testRunResult = testRunResultDatabaseService
			.getTestRunResult(new TestRunResultDatabaseService.TestRunResultDetails(applicationId, stageId, testRunId));
		Optional<List<PersistedTestSetResult>> testSetResults = loadTestSetResults(applicationId, stageId, testRunId);

		if (testRunResult.isEmpty() || testSetResults.isEmpty()) {
			return Optional.empty();
		}

		List<TestSetPrintContext> testSetPrintContexts = buildTestSetPrintContexts(testSetResults.get());
		if (testSetPrintContexts.size() != testSetResults.get().size()) {
			return Optional.empty();
		}

		PersistedTestRunResult persistedTestRunResult = testRunResult.get();
		return Optional.of(new TestRunPrintContext(persistedTestRunResult.id(), persistedTestRunResult.applicationId(),
				persistedTestRunResult.stageId(), persistedTestRunResult.testPlanId(),
				persistedTestRunResult.timestamp(), persistedTestRunResult.status(), persistedTestRunResult.label(),
				testSetPrintContexts));
	}

	@VisibleForTesting
	protected boolean pathBelongsToHierarchy(Long applicationId, Long stageId, Long testRunId, Long testSetResultId,
			Long pathResultId) {
		return hierarchyValidationService.pathBelongsToHierarchy(new HierarchyValidationService.TestPathHierarchy(
				applicationId, stageId, testRunId, testSetResultId, pathResultId));
	}

	@VisibleForTesting
	protected boolean testSetBelongsToRun(Long applicationId, Long stageId, Long testRunId, Long testSetResultId) {
		return hierarchyValidationService.testSetBelongsToRun(
				new HierarchyValidationService.TestSetHierarchy(applicationId, stageId, testRunId, testSetResultId));
	}

	@VisibleForTesting
	protected Optional<TestSetPrintContext> buildTestSetPrintContextFromResult(PersistedTestSetResult testSetResult) {
		return buildTestSetPrintContexts(List.of(testSetResult)).stream().findFirst();
	}

	private List<TestSetPrintContext> buildTestSetPrintContexts(List<PersistedTestSetResult> testSetResults) {
		if (testSetResults.stream().map(this::resolveExecutedBrowserFromSetResult).anyMatch(Optional::isEmpty)) {
			return List.of();
		}
		Map<Long, Browser> browsersBySetId = testSetResults.stream()
			.collect(Collectors.toMap(PersistedTestSetResult::id,
					testSetResult -> resolveExecutedBrowserFromSetResult(testSetResult).orElseThrow(),
					(left, _) -> left));

		List<PersistedTestPathResult> pathResults = loadPathResults(browsersBySetId.keySet());
		Map<Long, List<PersistedActionResult>> actionResultsByPathId = actionResultQueryService
			.listActionResultChains(new ActionResultQueryService.ActionResultChains(
					pathResults.stream().map(PersistedTestPathResult::id).toList()));
		if (actionResultsByPathId.size() != pathResults.size()) {
			return List.of();
		}
		Map<Long, List<PersistedTestPathResult>> pathsBySetId = pathResults.stream()
			.collect(Collectors.groupingBy(PersistedTestPathResult::testSetResultId));

		return testSetResults.stream()
			.map(testSetResult -> new TestSetPrintContext(testSetResult.id(), testSetResult.testRunId(),
					testSetResult.applicationId(), testSetResult.stageId(), browsersBySetId.get(testSetResult.id()),
					pathsBySetId.getOrDefault(testSetResult.id(), List.of())
						.stream()
						.map(pathResult -> new TestPathPrintContext(pathResult.id(), pathResult.testSetResultId(),
								pathResult.pathLabel(), browsersBySetId.get(testSetResult.id()),
								actionResultsByPathId.get(pathResult.id())))
						.toList()))
			.toList();
	}

	private List<PersistedTestPathResult> loadPathResults(java.util.Collection<Long> testSetResultIds) {
		return loadPages(page -> Optional.of(testPathResultQueryService.listPathResults(
				new TestPathResultQueryService.TestSetPaths(testSetResultIds, page, PRINT_QUERY_PAGE_SIZE))))
			.orElseThrow();
	}

	private Optional<List<PersistedTestSetResult>> loadTestSetResults(Long applicationId, Long stageId,
			Long testRunId) {
		return loadPages(page -> testSetResultQueryService
			.listBrowserResults(new TestSetResultQueryService.TestRunBrowserResults(applicationId, stageId, testRunId,
					page, PRINT_QUERY_PAGE_SIZE)));
	}

	private <T> Optional<List<T>> loadPages(IntFunction<Optional<PagedResult<T>>> pageLoader) {
		List<T> results = new ArrayList<>();
		int page = 0;
		Optional<PagedResult<T>> resultPage;
		do {
			resultPage = pageLoader.apply(page++);
			if (resultPage.isEmpty()) {
				return Optional.empty();
			}
			results.addAll(resultPage.get().items());
		}
		while (resultPage.get().hasNext());
		return Optional.of(results);
	}

	@VisibleForTesting
	protected Optional<Browser> resolveExecutedBrowserForSetResultId(Long testSetResultId) {
		return testSetResultQueryService.getTestSetResult(testSetResultId)
			.flatMap(this::resolveExecutedBrowserFromSetResult);
	}

	@VisibleForTesting
	protected Optional<Browser> resolveExecutedBrowserFromSetResult(PersistedTestSetResult testSetResult) {
		Browser executedBrowser = testSetResult.testedInBrowser() != null ? testSetResult.testedInBrowser()
				: testSetResult.browserToRunIn();
		return Optional.ofNullable(executedBrowser);
	}

}
