package io.github.yaforster.trails.adapter.api.rest.print;

import io.github.yaforster.trails.core.PagedResult;

import io.github.yaforster.trails.app.services.*;
import io.github.yaforster.trails.core.GeneratedFile;
import io.github.yaforster.trails.core.TrailsTest;
import io.github.yaforster.trails.core.persisted.PersistedActionResult;
import io.github.yaforster.trails.core.persisted.PersistedTestPathResult;
import io.github.yaforster.trails.core.persisted.PersistedTestRunResult;
import io.github.yaforster.trails.core.persisted.PersistedTestSetResult;
import io.github.yaforster.trails.core.print.TestPathPrintContext;
import io.github.yaforster.trails.core.print.TestRunPrintContext;
import io.github.yaforster.trails.core.test.Browser;
import org.junit.jupiter.api.Test;

import java.sql.Timestamp;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class PrintFacadeServiceTest extends TrailsTest {

	private final TestSetResultQueryService testSetResultQueryService = mock(TestSetResultQueryService.class);

	private final TestPathResultQueryService testPathResultQueryService = mock(TestPathResultQueryService.class);

	private final ActionResultQueryService actionResultQueryService = mock(ActionResultQueryService.class);

	private final TestRunResultDatabaseService testRunResultDatabaseService = mock(TestRunResultDatabaseService.class);

	private final PrintService printService = mock(PrintService.class);

	private final HierarchyValidationService hierarchyValidationService = mock(HierarchyValidationService.class);

	private final PrintFacadeServiceImpl service = new PrintFacadeServiceImpl(testSetResultQueryService,
			testPathResultQueryService, actionResultQueryService, testRunResultDatabaseService, printService,
			hierarchyValidationService);

	@Test
	void printPath_shouldReturnPdf_whenHierarchyAndDataExist() throws Exception {
		Long applicationId = randomId();
		Long stageId = randomId();
		Long testRunId = randomId();
		Long testSetResultId = randomId();
		Long pathResultId = randomId();
		Browser browserToRunIn = getInstancioOf(Browser.class).create();
		Browser testedInBrowser = getInstancioOf(Browser.class).create();
		mockHierarchy(applicationId, stageId, testRunId, testSetResultId, pathResultId, browserToRunIn,
				testedInBrowser);

		List<PersistedActionResult> actionResults = getInstancioOf(PersistedActionResult.class).stream()
			.limit(2)
			.toList();
		byte[] pdf = getInstancioOf(byte[].class).create();
		when(actionResultQueryService.listActionResultChain(pathResultId)).thenReturn(Optional.of(actionResults));
		doAnswer(invocation -> {
			invocation.getArgument(0, PrintService.TestPathPdf.class).outputStream().write(pdf);
			return null;
		}).when(printService).renderTestPathPdf(any(PrintService.TestPathPdf.class));

		Optional<GeneratedFile> result = service.printPath(
				new PrintFacadeService.TestPathPrint(applicationId, stageId, testRunId, testSetResultId, pathResultId));

		assertTrue(result.isPresent());
		assertArrayEquals(pdf, result.get().content());
		assertEquals("test-path-" + pathResultId + ".pdf", result.get().fileName());
		verify(printService).renderTestPathPdf(any(PrintService.TestPathPdf.class));
	}

	@Test
	void printPath_shouldReturnEmpty_whenHierarchyMismatch() {
		Long applicationId = randomId();
		Long stageId = randomId();
		Long testRunId = randomId();
		Long testSetResultId = randomId();
		Long pathResultId = randomId();
		when(hierarchyValidationService.pathBelongsToHierarchy(new HierarchyValidationService.TestPathHierarchy(
				applicationId, stageId, testRunId, testSetResultId, pathResultId)))
			.thenReturn(false);

		Optional<GeneratedFile> result = service.printPath(
				new PrintFacadeService.TestPathPrint(applicationId, stageId, testRunId, testSetResultId, pathResultId));

		assertTrue(result.isEmpty());
		verifyNoInteractions(actionResultQueryService, printService);
	}

	@Test
	void printPath_shouldReturnEmpty_whenActionChainMissing() {
		Long applicationId = randomId();
		Long stageId = randomId();
		Long testRunId = randomId();
		Long testSetResultId = randomId();
		Long pathResultId = randomId();
		Browser browserToRunIn = getInstancioOf(Browser.class).create();
		when(hierarchyValidationService.pathBelongsToHierarchy(new HierarchyValidationService.TestPathHierarchy(
				applicationId, stageId, testRunId, testSetResultId, pathResultId)))
			.thenReturn(true);
		mockHierarchy(applicationId, stageId, testRunId, testSetResultId, pathResultId, browserToRunIn, null);
		when(actionResultQueryService.listActionResultChain(pathResultId)).thenReturn(Optional.empty());

		Optional<GeneratedFile> result = service.printPath(
				new PrintFacadeService.TestPathPrint(applicationId, stageId, testRunId, testSetResultId, pathResultId));

		assertTrue(result.isEmpty());
		verifyNoInteractions(printService);
	}

	@Test
	void getTestPathPrintContext_shouldFallbackToBrowserToRunIn_whenTestedInBrowserIsNull() {
		Long testSetResultId = randomId();
		Long pathResultId = randomId();
		Browser browserToRunIn = getInstancioOf(Browser.class).create();
		when(testPathResultQueryService.getPathResult(pathResultId)).thenReturn(Optional
			.of(new PersistedTestPathResult(pathResultId, testSetResultId, getInstancioOf(String.class).create())));
		when(actionResultQueryService.listActionResultChain(pathResultId))
			.thenReturn(Optional.of(getInstancioOf(PersistedActionResult.class).stream().limit(2).toList()));
		PersistedTestSetResult testSetResult = getTestSetResult(testSetResultId, randomId(), randomId(), randomId(),
				browserToRunIn, null);
		when(testSetResultQueryService.getTestSetResult(testSetResultId)).thenReturn(Optional.of(testSetResult));

		Optional<TestPathPrintContext> result = service.buildTestPathPrintContext(pathResultId, testSetResultId);

		assertTrue(result.isPresent());
		assertEquals(browserToRunIn, result.get().browser());
	}

	@Test
	void getTestPathPrintContext_shouldReturnEmpty_whenNoBrowserCanBeResolved() {
		Long testSetResultId = randomId();
		Long pathResultId = randomId();
		when(testPathResultQueryService.getPathResult(pathResultId)).thenReturn(Optional
			.of(new PersistedTestPathResult(pathResultId, testSetResultId, getInstancioOf(String.class).create())));
		when(actionResultQueryService.listActionResultChain(pathResultId))
			.thenReturn(Optional.of(getInstancioOf(PersistedActionResult.class).stream().limit(2).toList()));
		PersistedTestSetResult testSetResult = getTestSetResult(testSetResultId, randomId(), randomId(), randomId(),
				null, null);
		when(testSetResultQueryService.getTestSetResult(testSetResultId)).thenReturn(Optional.of(testSetResult));

		Optional<TestPathPrintContext> result = service.buildTestPathPrintContext(pathResultId, testSetResultId);

		assertTrue(result.isEmpty());
	}

	@Test
	void testSetBelongsToRun_shouldReturnTrue_whenIdsMatch() {
		Long applicationId = randomId();
		Long stageId = randomId();
		Long testRunId = randomId();
		Long testSetResultId = randomId();
		when(hierarchyValidationService.testSetBelongsToRun(
				new HierarchyValidationService.TestSetHierarchy(applicationId, stageId, testRunId, testSetResultId)))
			.thenReturn(true);

		boolean result = service.testSetBelongsToRun(applicationId, stageId, testRunId, testSetResultId);

		assertTrue(result);
	}

	@Test
	void resolveExecutedBrowserFromSetResult_shouldFallbackToBrowserToRunIn() {
		Browser browserToRunIn = getInstancioOf(Browser.class).create();
		PersistedTestSetResult testSetResult = getTestSetResult(randomId(), randomId(), randomId(), randomId(),
				browserToRunIn, null);

		Optional<Browser> result = service.resolveExecutedBrowserFromSetResult(testSetResult);

		assertTrue(result.isPresent());
		assertEquals(browserToRunIn, result.get());
	}

	@Test
	void resolveExecutedBrowserForSetResultId_shouldLoadSetAndResolveBrowser() {
		Long testSetResultId = randomId();
		Browser testedInBrowser = getInstancioOf(Browser.class).create();
		PersistedTestSetResult testSetResult = getTestSetResult(testSetResultId, randomId(), randomId(), randomId(),
				getInstancioOf(Browser.class).create(), testedInBrowser);
		when(testSetResultQueryService.getTestSetResult(testSetResultId)).thenReturn(Optional.of(testSetResult));

		Optional<Browser> result = service.resolveExecutedBrowserForSetResultId(testSetResultId);

		assertTrue(result.isPresent());
		assertEquals(testedInBrowser, result.get());
	}

	@Test
	void printTestSet_shouldReturnPdf_whenHierarchyAndDataExist() throws Exception {
		Long applicationId = randomId();
		Long stageId = randomId();
		Long testRunId = randomId();
		Long testSetResultId = randomId();
		when(hierarchyValidationService.testSetBelongsToRun(
				new HierarchyValidationService.TestSetHierarchy(applicationId, stageId, testRunId, testSetResultId)))
			.thenReturn(true);
		Browser browserToRunIn = getInstancioOf(Browser.class).create();
		Browser testedInBrowser = getInstancioOf(Browser.class).create();
		PersistedTestSetResult testSetResult = getTestSetResult(testSetResultId, testRunId, applicationId, stageId,
				browserToRunIn, testedInBrowser);
		when(testSetResultQueryService.getTestSetResult(testSetResultId)).thenReturn(Optional.of(testSetResult));
		PersistedTestPathResult firstPath = new PersistedTestPathResult(randomId(), testSetResultId,
				getInstancioOf(String.class).create());
		PersistedTestPathResult secondPath = new PersistedTestPathResult(randomId(), testSetResultId,
				getInstancioOf(String.class).create());
		when(testPathResultQueryService.listPathResults(any(TestPathResultQueryService.TestSetPaths.class)))
			.thenReturn(PagedResult.singlePage(List.of(firstPath, secondPath)));
		when(actionResultQueryService.listActionResultChains(any(ActionResultQueryService.ActionResultChains.class)))
			.thenReturn(Map.of(firstPath.id(), getInstancioOf(PersistedActionResult.class).stream().limit(2).toList(),
					secondPath.id(), getInstancioOf(PersistedActionResult.class).stream().limit(2).toList()));
		byte[] pdf = getInstancioOf(byte[].class).create();
		doAnswer(invocation -> {
			invocation.getArgument(0, PrintService.TestSetPdf.class).outputStream().write(pdf);
			return null;
		}).when(printService).renderTestSetPdf(any(PrintService.TestSetPdf.class));

		Optional<GeneratedFile> result = service
			.printTestSet(new PrintFacadeService.TestSetPrint(applicationId, stageId, testRunId, testSetResultId));

		assertTrue(result.isPresent());
		assertArrayEquals(pdf, result.get().content());
		assertEquals("test-set-" + testSetResultId + ".pdf", result.get().fileName());
		verify(printService).renderTestSetPdf(any(PrintService.TestSetPdf.class));
	}

	@Test
	void printTestSet_shouldReturnEmpty_whenHierarchyMismatch() {
		Long applicationId = randomId();
		Long stageId = randomId();
		Long testRunId = randomId();
		Long testSetResultId = randomId();
		when(hierarchyValidationService.testSetBelongsToRun(
				new HierarchyValidationService.TestSetHierarchy(applicationId, stageId, testRunId, testSetResultId)))
			.thenReturn(false);

		Optional<GeneratedFile> result = service
			.printTestSet(new PrintFacadeService.TestSetPrint(applicationId, stageId, testRunId, testSetResultId));

		assertTrue(result.isEmpty());
		verifyNoInteractions(printService);
	}

	@Test
	void getTestSetPrintContext_shouldContainIdsBrowserAndPaths() {
		Long testSetResultId = randomId();
		Long testRunId = randomId();
		Long applicationId = randomId();
		Long stageId = randomId();
		Browser browserToRunIn = getInstancioOf(Browser.class).create();
		Browser testedInBrowser = getInstancioOf(Browser.class).create();
		PersistedTestSetResult testSetResult = getTestSetResult(testSetResultId, testRunId, applicationId, stageId,
				browserToRunIn, testedInBrowser);
		when(testSetResultQueryService.getTestSetResult(testSetResultId)).thenReturn(Optional.of(testSetResult));
		PersistedTestPathResult firstPath = new PersistedTestPathResult(randomId(), testSetResultId,
				getInstancioOf(String.class).create());
		PersistedTestPathResult secondPath = new PersistedTestPathResult(randomId(), testSetResultId,
				getInstancioOf(String.class).create());
		when(testPathResultQueryService.listPathResults(any(TestPathResultQueryService.TestSetPaths.class)))
			.thenReturn(PagedResult.singlePage(List.of(firstPath, secondPath)));
		List<PersistedActionResult> firstPathActions = getInstancioOf(PersistedActionResult.class).stream()
			.limit(2)
			.toList();
		List<PersistedActionResult> secondPathActions = getInstancioOf(PersistedActionResult.class).stream()
			.limit(2)
			.toList();
		when(actionResultQueryService.listActionResultChains(any(ActionResultQueryService.ActionResultChains.class)))
			.thenReturn(Map.of(firstPath.id(), firstPathActions, secondPath.id(), secondPathActions));

		var result = service.buildTestSetPrintContext(testSetResultId);

		assertTrue(result.isPresent());
		assertEquals(testSetResultId, result.get().id());
		assertEquals(testRunId, result.get().testRunId());
		assertEquals(applicationId, result.get().applicationId());
		assertEquals(stageId, result.get().stageId());
		assertEquals(testedInBrowser, result.get().browser());
		assertEquals(2, result.get().pathPrintContexts().size());
		assertEquals(firstPath.id(), result.get().pathPrintContexts().get(0).pathResultId());
		assertEquals(firstPath.pathLabel(), result.get().pathPrintContexts().get(0).pathLabel());
		assertEquals(firstPathActions, result.get().pathPrintContexts().get(0).actionResults());
		assertEquals(secondPath.id(), result.get().pathPrintContexts().get(1).pathResultId());
		assertEquals(secondPath.pathLabel(), result.get().pathPrintContexts().get(1).pathLabel());
		assertEquals(secondPathActions, result.get().pathPrintContexts().get(1).actionResults());
	}

	@Test
	void printTestRun_shouldReturnPdf_whenHierarchyAndDataExist() throws Exception {
		Long applicationId = randomId();
		Long stageId = randomId();
		Long testRunId = randomId();
		PersistedTestRunResult testRunResult = getTestRunResult(testRunId, applicationId, stageId);
		when(testRunResultDatabaseService
			.getTestRunResult(new TestRunResultDatabaseService.TestRunResultDetails(applicationId, stageId, testRunId)))
			.thenReturn(Optional.of(testRunResult));

		Long firstSetId = randomId();
		Browser firstBrowser = getInstancioOf(Browser.class).create();
		PersistedTestSetResult firstSet = getTestSetResult(firstSetId, testRunId, applicationId, stageId, firstBrowser,
				null);
		when(testSetResultQueryService.listBrowserResults(
				new TestSetResultQueryService.TestRunBrowserResults(applicationId, stageId, testRunId, 0, 100)))
			.thenReturn(Optional.of(PagedResult.singlePage(List.of(firstSet))));

		PersistedTestPathResult firstPath = new PersistedTestPathResult(randomId(), firstSetId,
				getInstancioOf(String.class).create());
		when(testPathResultQueryService.listPathResults(any(TestPathResultQueryService.TestSetPaths.class)))
			.thenReturn(PagedResult.singlePage(List.of(firstPath)));
		when(actionResultQueryService.listActionResultChains(any(ActionResultQueryService.ActionResultChains.class)))
			.thenReturn(Map.of(firstPath.id(), getInstancioOf(PersistedActionResult.class).stream().limit(2).toList()));

		byte[] pdf = getInstancioOf(byte[].class).create();
		doAnswer(invocation -> {
			invocation.getArgument(0, PrintService.TestRunPdf.class).outputStream().write(pdf);
			return null;
		}).when(printService).renderTestRunPdf(any(PrintService.TestRunPdf.class));

		Optional<GeneratedFile> result = service
			.printTestRun(new PrintFacadeService.TestRunPrint(applicationId, stageId, testRunId));

		assertTrue(result.isPresent());
		assertArrayEquals(pdf, result.get().content());
		assertEquals("test-run-" + testRunId + ".pdf", result.get().fileName());
		verify(printService).renderTestRunPdf(any(PrintService.TestRunPdf.class));
	}

	@Test
	void printTestRun_shouldReturnEmpty_whenTestRunIsMissing() {
		Long applicationId = randomId();
		Long stageId = randomId();
		Long testRunId = randomId();
		when(testRunResultDatabaseService
			.getTestRunResult(new TestRunResultDatabaseService.TestRunResultDetails(applicationId, stageId, testRunId)))
			.thenReturn(Optional.empty());

		Optional<GeneratedFile> result = service
			.printTestRun(new PrintFacadeService.TestRunPrint(applicationId, stageId, testRunId));

		assertTrue(result.isEmpty());
		verifyNoInteractions(printService);
	}

	@Test
	void getTestRunPrintContext_shouldContainNestedTestSetsAndPaths() {
		Long applicationId = randomId();
		Long stageId = randomId();
		Long testRunId = randomId();
		PersistedTestRunResult testRunResult = getTestRunResult(testRunId, applicationId, stageId);
		when(testRunResultDatabaseService
			.getTestRunResult(new TestRunResultDatabaseService.TestRunResultDetails(applicationId, stageId, testRunId)))
			.thenReturn(Optional.of(testRunResult));

		Long testSetResultId = randomId();
		Browser browserToRunIn = getInstancioOf(Browser.class).create();
		Browser testedInBrowser = getInstancioOf(Browser.class).create();
		PersistedTestSetResult testSetResult = getTestSetResult(testSetResultId, testRunId, applicationId, stageId,
				browserToRunIn, testedInBrowser);
		when(testSetResultQueryService.listBrowserResults(
				new TestSetResultQueryService.TestRunBrowserResults(applicationId, stageId, testRunId, 0, 100)))
			.thenReturn(Optional.of(PagedResult.singlePage(List.of(testSetResult))));

		PersistedTestPathResult pathResult = new PersistedTestPathResult(randomId(), testSetResultId,
				getInstancioOf(String.class).create());
		when(testPathResultQueryService.listPathResults(any(TestPathResultQueryService.TestSetPaths.class)))
			.thenReturn(PagedResult.singlePage(List.of(pathResult)));
		List<PersistedActionResult> actionResults = getInstancioOf(PersistedActionResult.class).stream()
			.limit(2)
			.toList();
		when(actionResultQueryService.listActionResultChains(any(ActionResultQueryService.ActionResultChains.class)))
			.thenReturn(Map.of(pathResult.id(), actionResults));

		var result = service.buildTestRunPrintContext(applicationId, stageId, testRunId);

		assertTrue(result.isPresent());
		assertEquals(testRunId, result.get().id());
		assertEquals(applicationId, result.get().applicationId());
		assertEquals(stageId, result.get().stageId());
		assertEquals(testRunResult.testPlanId(), result.get().testPlanId());
		assertEquals(testRunResult.status(), result.get().status());
		assertEquals(testRunResult.label(), result.get().label());
		assertEquals(1, result.get().testSetPrintContexts().size());
		assertEquals(testSetResultId, result.get().testSetPrintContexts().get(0).id());
		assertEquals(1, result.get().testSetPrintContexts().get(0).pathPrintContexts().size());
		assertEquals(pathResult.id(),
				result.get().testSetPrintContexts().get(0).pathPrintContexts().get(0).pathResultId());
		assertEquals(actionResults,
				result.get().testSetPrintContexts().get(0).pathPrintContexts().get(0).actionResults());
	}

	@Test
	void getTestRunPrintContext_shouldLoadAllTestSetAndPathPages() {
		Long applicationId = randomId();
		Long stageId = randomId();
		Long testRunId = randomId();
		PersistedTestRunResult testRunResult = getTestRunResult(testRunId, applicationId, stageId);
		when(testRunResultDatabaseService
			.getTestRunResult(new TestRunResultDatabaseService.TestRunResultDetails(applicationId, stageId, testRunId)))
			.thenReturn(Optional.of(testRunResult));

		PersistedTestSetResult firstTestSet = getTestSetResult(101L, testRunId, applicationId, stageId, Browser.CHROME,
				Browser.CHROME);
		PersistedTestSetResult secondTestSet = getTestSetResult(102L, testRunId, applicationId, stageId,
				Browser.FIREFOX, Browser.FIREFOX);
		when(testSetResultQueryService.listBrowserResults(any(TestSetResultQueryService.TestRunBrowserResults.class)))
			.thenReturn(Optional.of(new PagedResult<>(List.of(firstTestSet), 0, 100, 101)),
					Optional.of(new PagedResult<>(List.of(secondTestSet), 1, 100, 101)));

		PersistedTestPathResult firstPath = new PersistedTestPathResult(201L, firstTestSet.id(), "first");
		PersistedTestPathResult secondPath = new PersistedTestPathResult(202L, secondTestSet.id(), "second");
		when(testPathResultQueryService.listPathResults(any(TestPathResultQueryService.TestSetPaths.class))).thenReturn(
				new PagedResult<>(List.of(firstPath), 0, 100, 101),
				new PagedResult<>(List.of(secondPath), 1, 100, 101));
		List<PersistedActionResult> actionResults = getInstancioOf(PersistedActionResult.class).stream()
			.limit(1)
			.toList();
		when(actionResultQueryService.listActionResultChains(any(ActionResultQueryService.ActionResultChains.class)))
			.thenReturn(Map.of(firstPath.id(), actionResults, secondPath.id(), actionResults));

		Optional<TestRunPrintContext> result = service.buildTestRunPrintContext(applicationId, stageId, testRunId);

		assertTrue(result.isPresent());
		assertEquals(2, result.get().testSetPrintContexts().size());
		assertEquals(firstPath.id(),
				result.get().testSetPrintContexts().get(0).pathPrintContexts().get(0).pathResultId());
		assertEquals(secondPath.id(),
				result.get().testSetPrintContexts().get(1).pathPrintContexts().get(0).pathResultId());
	}

	private void mockHierarchy(Long applicationId, Long stageId, Long testRunId, Long testSetResultId,
			Long pathResultId, Browser browserToRunIn, Browser testedInBrowser) {
		when(hierarchyValidationService.pathBelongsToHierarchy(new HierarchyValidationService.TestPathHierarchy(
				applicationId, stageId, testRunId, testSetResultId, pathResultId)))
			.thenReturn(true);
		PersistedTestSetResult testSetResult = getTestSetResult(testSetResultId, testRunId, applicationId, stageId,
				browserToRunIn, testedInBrowser);
		when(testSetResultQueryService.getTestSetResult(testSetResultId)).thenReturn(Optional.of(testSetResult));
		PersistedTestPathResult randomPath = getInstancioOf(PersistedTestPathResult.class).create();
		when(testPathResultQueryService.getPathResult(pathResultId)).thenReturn(
				Optional.of(new PersistedTestPathResult(pathResultId, testSetResultId, randomPath.pathLabel())));
	}

	private PersistedTestSetResult getTestSetResult(Long testSetResultId, Long testRunId, Long applicationId,
			Long stageId, Browser browserToRunIn, Browser testedInBrowser) {
		PersistedTestSetResult randomSet = getInstancioOf(PersistedTestSetResult.class).create();
		return new PersistedTestSetResult(testSetResultId, testRunId, applicationId, stageId, randomSet.totalRunTime(),
				browserToRunIn, randomSet.testPlanLabel(), randomSet.testCaseTimestamp(), testedInBrowser);
	}

	private PersistedTestRunResult getTestRunResult(Long testRunId, Long applicationId, Long stageId) {
		return new PersistedTestRunResult(testRunId, applicationId, stageId, randomId(),
				Timestamp.valueOf("2026-01-01 00:00:00"),
				getInstancioOf(io.github.yaforster.trails.core.data.ResultIndicator.class).create(),
				getInstancioOf(String.class).create());
	}

	private Long randomId() {
		Long value = getInstancioOf(Long.class).create();
		if (value == Long.MIN_VALUE) {
			return 1L;
		}
		return Math.abs(value) + 1L;
	}

}
