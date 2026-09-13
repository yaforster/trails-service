package io.github.yaforster.trails.integration.api;

import io.github.yaforster.trails.core.PagedResult;

import io.github.yaforster.trails.app.services.TestPathResultQueryService;
import io.github.yaforster.trails.app.services.TestSetResultQueryService;
import io.github.yaforster.trails.core.persisted.PersistedTestPathResult;
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

class TestPathApiIT extends RestApiIntegrationTestBase {

	@MockitoBean
	private TestPathResultQueryService testPathResultQueryService;

	@MockitoBean
	private TestSetResultQueryService testSetResultQueryService;

	private static PersistedTestSetResult set(Long id, Long testRunId, Long applicationId, Long stageId) {
		return new PersistedTestSetResult(id, testRunId, applicationId, stageId, 42, Browser.CHROME, "plan",
				PersistedTestSetResult.NO_TEST_CASE_TIMESTAMP, Browser.FIREFOX);
	}

	@Test
	void listPathResultsInTestSet_shouldReturnPagedResponseWithExpectedLinks() throws Exception {
		PersistedTestPathResult first = new PersistedTestPathResult(2001L, 1701L, "Path A");
		PersistedTestPathResult second = new PersistedTestPathResult(2002L, 1701L, "Path B");
		PagedResult<PersistedTestPathResult> pagedResults = new PagedResult<>(List.of(first, second), 1, 2, 5);

		when(testSetResultQueryService.getTestSetResult(1701L)).thenReturn(Optional.of(set(1701L, 99L, 77L, 88L)));
		when(testPathResultQueryService.listPathResults(new TestPathResultQueryService.TestSetPathPage(1701L, 1, 2)))
			.thenReturn(Optional.of(pagedResults));
		ResultActions response = performGet("/testruns/77/88/99/browsers/1701/paths?page=1&size=2")
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.items[0].id").value(2001))
			.andExpect(jsonPath("$.items[0]._links.self.href",
					containsString("/testruns/77/88/99/browsers/1701/paths/2001")))
			.andExpect(jsonPath("$.items[0]._links.collection.href",
					containsString("/testruns/77/88/99/browsers/1701/paths")))
			.andExpect(jsonPath("$.items[0]._links.testSetResult.href",
					containsString("/testruns/77/88/99/browsers/1701")))
			.andExpect(jsonPath("$.items[0]._links.actionResults.href",
					containsString("/testruns/77/88/99/browsers/1701/paths/2001/actions/chain")))
			.andExpect(jsonPath("$.items[0]._links.downloadedFiles.href",
					containsString("/testruns/77/88/99/browsers/1701/paths/2001/artifacts")))
			.andExpect(jsonPath("$.items[1].id").value(2002))
			.andExpect(jsonPath("$._links.self.href", containsString("/testruns/77/88/99/browsers/1701/paths")))
			.andExpect(jsonPath("$._links.testSetResult.href", containsString("/testruns/77/88/99/browsers/1701")));

		expectPage(response, 1, 2, 5, 3);
		expectPagingLinks(response, 0, 2, 0, 2);
	}

	@Test
	void listPathResultsInTestSet_shouldUseDefaultPagingParameters_whenQueryParamsMissing() throws Exception {
		PersistedTestPathResult persisted = new PersistedTestPathResult(2101L, 1701L, "Default Path");
		PagedResult<PersistedTestPathResult> pagedResults = new PagedResult<>(List.of(persisted), DEFAULT_PAGE,
				DEFAULT_SIZE, 1);

		when(testSetResultQueryService.getTestSetResult(1701L)).thenReturn(Optional.of(set(1701L, 99L, 77L, 88L)));
		when(testPathResultQueryService
			.listPathResults(new TestPathResultQueryService.TestSetPathPage(1701L, DEFAULT_PAGE, DEFAULT_SIZE)))
			.thenReturn(Optional.of(pagedResults));
		ResultActions response = performGet("/testruns/77/88/99/browsers/1701/paths").andExpect(status().isOk());

		expectPage(response, DEFAULT_PAGE, DEFAULT_SIZE, 1, 1);
		expectDefaultSelfPagingParams(response);
	}

	@Test
	void listPathResultsInTestSet_shouldReturnEmptyPageWithoutPrevAndNext() throws Exception {
		PagedResult<PersistedTestPathResult> emptyPage = new PagedResult<>(Collections.emptyList(), DEFAULT_PAGE,
				DEFAULT_SIZE, 0);

		when(testSetResultQueryService.getTestSetResult(1701L)).thenReturn(Optional.of(set(1701L, 99L, 77L, 88L)));
		when(testPathResultQueryService
			.listPathResults(new TestPathResultQueryService.TestSetPathPage(1701L, DEFAULT_PAGE, DEFAULT_SIZE)))
			.thenReturn(Optional.of(emptyPage));

		ResultActions response = performGet("/testruns/77/88/99/browsers/1701/paths?page=0&size=20")
			.andExpect(status().isOk());

		expectEmptyItems(response);
		expectPage(response, DEFAULT_PAGE, DEFAULT_SIZE, 0, 0);
		expectPagingLinks(response, 0, 0, null, null);
	}

	@Test
	void listPathResultsInTestSet_shouldReturnNotFound_whenMissing() throws Exception {
		when(testSetResultQueryService.getTestSetResult(1701L)).thenReturn(Optional.of(set(1701L, 99L, 77L, 88L)));
		when(testPathResultQueryService
			.listPathResults(new TestPathResultQueryService.TestSetPathPage(1701L, DEFAULT_PAGE, DEFAULT_SIZE)))
			.thenReturn(Optional.empty());

		performGet("/testruns/77/88/99/browsers/1701/paths").andExpect(status().isNotFound());
	}

	@Test
	void getPathResultInTestSet_shouldReturnResultWithLinks() throws Exception {
		PersistedTestPathResult persisted = new PersistedTestPathResult(2201L, 1701L, "Single Path");
		when(testSetResultQueryService.getTestSetResult(1701L)).thenReturn(Optional.of(set(1701L, 99L, 77L, 88L)));
		when(testPathResultQueryService.getPathResult(2201L)).thenReturn(Optional.of(persisted));

		performGet("/testruns/77/88/99/browsers/1701/paths/2201").andExpect(status().isOk())
			.andExpect(jsonPath("$.id").value(2201))
			.andExpect(jsonPath("$._links.self.href", containsString("/testruns/77/88/99/browsers/1701/paths/2201")))
			.andExpect(jsonPath("$._links.collection.href", containsString("/testruns/77/88/99/browsers/1701/paths")))
			.andExpect(jsonPath("$._links.testSetResult.href", containsString("/testruns/77/88/99/browsers/1701")))
			.andExpect(jsonPath("$._links.actionResults.href",
					containsString("/testruns/77/88/99/browsers/1701/paths/2201/actions/chain")))
			.andExpect(jsonPath("$._links.downloadedFiles.href",
					containsString("/testruns/77/88/99/browsers/1701/paths/2201/artifacts")));
	}

	@Test
	void getPathResultInTestSet_shouldReturnNotFound_whenMissing() throws Exception {
		when(testSetResultQueryService.getTestSetResult(1701L)).thenReturn(Optional.of(set(1701L, 99L, 77L, 88L)));
		when(testPathResultQueryService.getPathResult(2202L)).thenReturn(Optional.empty());

		performGet("/testruns/77/88/99/browsers/1701/paths/2202").andExpect(status().isNotFound());
	}

}
