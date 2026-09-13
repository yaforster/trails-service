package io.github.yaforster.trails.integration.api;

import io.github.yaforster.trails.core.PagedResult;

import io.github.yaforster.trails.app.services.TestSetResultQueryService;
import io.github.yaforster.trails.core.persisted.PersistedTestSetResult;
import io.github.yaforster.trails.core.test.Browser;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.ResultActions;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class TestSetApiIT extends RestApiIntegrationTestBase {

	@MockitoBean
	private TestSetResultQueryService testSetResultQueryService;

	private static PersistedTestSetResult persistedTestSetResult(Long id, Long testRunId, Long applicationId,
			Long stageId, String testPlanLabel) {
		return new PersistedTestSetResult(id, testRunId, applicationId, stageId, 42, Browser.CHROME, testPlanLabel,
				PersistedTestSetResult.NO_TEST_CASE_TIMESTAMP, Browser.FIREFOX);
	}

	@Test
	void listBrowserResults_shouldReturnPagedResponseWithExpectedLinks() throws Exception {
		PersistedTestSetResult first = persistedTestSetResult(1501L, 99L, 77L, 88L, "Checkout Plan");
		PersistedTestSetResult second = persistedTestSetResult(1502L, 99L, 77L, 88L, "Login Plan");
		PagedResult<PersistedTestSetResult> pagedResults = new PagedResult<>(List.of(first, second), 1, 2, 5);

		when(testSetResultQueryService
			.listBrowserResults(new TestSetResultQueryService.TestRunBrowserResults(77L, 88L, 99L, 1, 2)))
			.thenReturn(Optional.of(pagedResults));

		ResultActions response = performGet("/testruns/77/88/99/browsers?page=1&size=2").andExpect(status().isOk())
			.andExpect(jsonPath("$.items[0].id").value(1501))
			.andExpect(jsonPath("$.items[0].testPlanLabel").value("Checkout Plan"))
			.andExpect(jsonPath("$.items[0]._links.self.href", containsString("/testruns/77/88/99/browsers/1501")))
			.andExpect(jsonPath("$.items[0]._links.collection.href", containsString("/testruns/77/88/99/browsers")))
			.andExpect(jsonPath("$.items[0]._links.testRun.href", containsString("/testruns/77/88/99")))
			.andExpect(
					jsonPath("$.items[0]._links.paths.href", containsString("/testruns/77/88/99/browsers/1501/paths")))
			.andExpect(jsonPath("$.items[1].id").value(1502))
			.andExpect(jsonPath("$.items[1].testPlanLabel").value("Login Plan"))
			.andExpect(jsonPath("$._links.self.href", containsString("/testruns/77/88/99/browsers")))
			.andExpect(jsonPath("$._links.testRun.href", containsString("/testruns/77/88/99")));

		expectPage(response, 1, 2, 5, 3);
		expectPagingLinks(response, 0, 2, 0, 2);
	}

	@Test
	void listBrowserResults_shouldUseDefaultPagingParameters_whenQueryParamsMissing() throws Exception {
		PersistedTestSetResult persisted = persistedTestSetResult(1601L, 99L, 77L, 88L, "Default Plan");
		PagedResult<PersistedTestSetResult> pagedResults = new PagedResult<>(List.of(persisted), DEFAULT_PAGE,
				DEFAULT_SIZE, 1);

		when(testSetResultQueryService.listBrowserResults(
				new TestSetResultQueryService.TestRunBrowserResults(77L, 88L, 99L, DEFAULT_PAGE, DEFAULT_SIZE)))
			.thenReturn(Optional.of(pagedResults));

		ResultActions response = performGet("/testruns/77/88/99/browsers").andExpect(status().isOk());

		expectPage(response, DEFAULT_PAGE, DEFAULT_SIZE, 1, 1);
		expectDefaultSelfPagingParams(response);
	}

	@Test
	void listBrowserResults_shouldReturnEmptyPageWithoutPrevAndNext() throws Exception {
		PagedResult<PersistedTestSetResult> emptyPage = new PagedResult<>(Collections.emptyList(), DEFAULT_PAGE,
				DEFAULT_SIZE, 0);

		when(testSetResultQueryService.listBrowserResults(
				new TestSetResultQueryService.TestRunBrowserResults(77L, 88L, 99L, DEFAULT_PAGE, DEFAULT_SIZE)))
			.thenReturn(Optional.of(emptyPage));

		ResultActions response = performGet("/testruns/77/88/99/browsers?page=0&size=20").andExpect(status().isOk());

		expectEmptyItems(response);
		expectPage(response, DEFAULT_PAGE, DEFAULT_SIZE, 0, 0);
		expectPagingLinks(response, 0, 0, null, null);
	}

	@Test
	void listBrowserResults_shouldReturnNotFound_whenMissing() throws Exception {
		when(testSetResultQueryService.listBrowserResults(
				new TestSetResultQueryService.TestRunBrowserResults(77L, 88L, 99L, DEFAULT_PAGE, DEFAULT_SIZE)))
			.thenReturn(Optional.empty());

		performGet("/testruns/77/88/99/browsers").andExpect(status().isNotFound());
	}

	@Test
	void getBrowserResultInTestRun_shouldReturnResultWithLinks() throws Exception {
		PersistedTestSetResult persisted = persistedTestSetResult(1701L, 99L, 77L, 88L, "Single Result");
		when(testSetResultQueryService.getTestSetResult(1701L)).thenReturn(Optional.of(persisted));

		performGet("/testruns/77/88/99/browsers/1701").andExpect(status().isOk())
			.andExpect(jsonPath("$.id").value(1701))
			.andExpect(jsonPath("$.totalRunTime").value(42))
			.andExpect(jsonPath("$.browserToRunIn").value("CHROME"))
			.andExpect(jsonPath("$.testPlanLabel").value("Single Result"))
			.andExpect(jsonPath("$.testCaseResult").doesNotExist())
			.andExpect(jsonPath("$._links.self.href", containsString("/testruns/77/88/99/browsers/1701")))
			.andExpect(jsonPath("$._links.collection.href", containsString("/testruns/77/88/99/browsers")))
			.andExpect(jsonPath("$._links.testRun.href", containsString("/testruns/77/88/99")))
			.andExpect(jsonPath("$._links.paths.href", containsString("/testruns/77/88/99/browsers/1701/paths")));
	}

	@Test
	void getBrowserResultInTestRun_shouldReturnNotFound_whenMissing() throws Exception {
		when(testSetResultQueryService.getTestSetResult(1702L)).thenReturn(Optional.empty());

		performGet("/testruns/77/88/99/browsers/1702").andExpect(status().isNotFound());
	}

}
