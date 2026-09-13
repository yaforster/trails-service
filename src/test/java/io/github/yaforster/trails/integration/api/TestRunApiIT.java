package io.github.yaforster.trails.integration.api;

import io.github.yaforster.trails.core.PagedResult;

import io.github.yaforster.trails.app.services.TestRunResultDatabaseService;
import io.github.yaforster.trails.core.data.ResultIndicator;
import io.github.yaforster.trails.core.persisted.PersistedTestRunHistory;
import io.github.yaforster.trails.core.persisted.PersistedTestRunResult;
import io.github.yaforster.trails.core.persisted.PersistedTestRunStatistics;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.ResultActions;

import java.sql.Timestamp;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class TestRunApiIT extends RestApiIntegrationTestBase {

	@MockitoBean
	private TestRunResultDatabaseService testRunResultDatabaseService;

	private static PersistedTestRunResult persisted(Long id, Long applicationId, Long stageId,
			ResultIndicator indicator) {
		return new PersistedTestRunResult(id, applicationId, stageId, 99L, null, indicator, "label");
	}

	@Test
	void listTestRuns_shouldReturnPagedResponseWithExpectedLinks() throws Exception {
		PersistedTestRunResult first = persisted(901L, 77L, 88L, ResultIndicator.SUCCESS);
		PersistedTestRunResult second = persisted(902L, 77L, 88L, ResultIndicator.FAILURE);
		PagedResult<PersistedTestRunResult> pagedRuns = new PagedResult<>(List.of(first, second), 1, 2, 5);

		when(testRunResultDatabaseService.listTestRuns(new TestRunResultDatabaseService.TestRunPage(77L, 88L, 1, 2)))
			.thenReturn(Optional.of(pagedRuns));

		ResultActions response = performGet("/applications/77/stages/88/testruns?page=1&size=2")
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.items[0].id").value(901))
			.andExpect(jsonPath("$.items[0].applicationId").value(77))
			.andExpect(jsonPath("$.items[0].stageId").value(88))
			.andExpect(jsonPath("$.items[0].indicator").value("SUCCESS"))
			.andExpect(jsonPath("$.items[0]._links.self.href", containsString("/testruns/77/88/901")))
			.andExpect(jsonPath("$.items[0]._links.testSets.href", containsString("/testruns/77/88/901/browsers")))
			.andExpect(jsonPath("$.items[1].id").value(902))
			.andExpect(jsonPath("$.items[1].indicator").value("FAILURE"))
			.andExpect(jsonPath("$.items[1]._links.self.href", containsString("/testruns/77/88/902")))
			.andExpect(jsonPath("$._links.self.href", containsString("/applications/77/stages/88/testruns")));

		expectPage(response, 1, 2, 5, 3);
		expectPagingLinks(response, 0, 2, 0, 2);
	}

	@Test
	void listTestRuns_shouldUseDefaultPagingParameters_whenQueryParamsMissing() throws Exception {
		PersistedTestRunResult persisted = persisted(911L, 77L, 88L, ResultIndicator.SUCCESS);
		PagedResult<PersistedTestRunResult> pagedRuns = new PagedResult<>(List.of(persisted), DEFAULT_PAGE,
				DEFAULT_SIZE, 1);

		when(testRunResultDatabaseService
			.listTestRuns(new TestRunResultDatabaseService.TestRunPage(77L, 88L, DEFAULT_PAGE, DEFAULT_SIZE)))
			.thenReturn(Optional.of(pagedRuns));

		ResultActions response = performGet("/applications/77/stages/88/testruns").andExpect(status().isOk());

		expectPage(response, DEFAULT_PAGE, DEFAULT_SIZE, 1, 1);
		expectDefaultSelfPagingParams(response);
	}

	@Test
	void listTestRuns_shouldReturnEmptyPageWithoutPrevAndNext() throws Exception {
		PagedResult<PersistedTestRunResult> emptyPage = new PagedResult<>(Collections.emptyList(), DEFAULT_PAGE,
				DEFAULT_SIZE, 0);

		when(testRunResultDatabaseService
			.listTestRuns(new TestRunResultDatabaseService.TestRunPage(77L, 88L, DEFAULT_PAGE, DEFAULT_SIZE)))
			.thenReturn(Optional.of(emptyPage));

		ResultActions response = performGet("/applications/77/stages/88/testruns?page=0&size=20")
			.andExpect(status().isOk());

		expectEmptyItems(response);
		expectPage(response, DEFAULT_PAGE, DEFAULT_SIZE, 0, 0);
		expectPagingLinks(response, 0, 0, null, null);
	}

	@Test
	void listTestRuns_shouldReturnNotFound_whenDatabaseReturnsEmptyOptional() throws Exception {
		when(testRunResultDatabaseService
			.listTestRuns(new TestRunResultDatabaseService.TestRunPage(77L, 88L, DEFAULT_PAGE, DEFAULT_SIZE)))
			.thenReturn(Optional.empty());

		performGet("/applications/77/stages/88/testruns").andExpect(status().isNotFound());
	}

	@Test
	void getTestRun_shouldReturnRunWithExpectedLinks_whenFound() throws Exception {
		PersistedTestRunResult persisted = persisted(921L, 77L, 88L, ResultIndicator.PARTIAL_SUCCESS);
		when(testRunResultDatabaseService
			.getTestRunResult(new TestRunResultDatabaseService.TestRunResultDetails(77L, 88L, 921L)))
			.thenReturn(Optional.of(persisted));

		performGet("/testruns/77/88/921").andExpect(status().isOk())
			.andExpect(jsonPath("$.id").value(921))
			.andExpect(jsonPath("$.applicationId").value(77))
			.andExpect(jsonPath("$.stageId").value(88))
			.andExpect(jsonPath("$.indicator").value("PARTIAL_SUCCESS"))
			.andExpect(jsonPath("$._links.self.href", containsString("/testruns/77/88/921")))
			.andExpect(jsonPath("$._links.collection.href", containsString("/applications/77/stages/88/testruns")))
			.andExpect(jsonPath("$._links.testSets.href", containsString("/testruns/77/88/921/browsers")));
	}

	@Test
	void getTestRun_shouldReturnNotFound_whenMissing() throws Exception {
		when(testRunResultDatabaseService
			.getTestRunResult(new TestRunResultDatabaseService.TestRunResultDetails(77L, 88L, 999L)))
			.thenReturn(Optional.empty());

		performGet("/testruns/77/88/999").andExpect(status().isNotFound());
	}

	@Test
	void getTestRunStatistics_shouldReturnStatisticsWithExpectedLinks_whenFound() throws Exception {
		PersistedTestRunStatistics statistics = new PersistedTestRunStatistics(77L, 88L, 1201L, 10L, 4L, 3L, 3L);
		when(testRunResultDatabaseService
			.getTestRunStatistics(new TestRunResultDatabaseService.TestPlanStatistics(77L, 88L, 1201L)))
			.thenReturn(Optional.of(statistics));

		performGet("/applications/77/stages/88/testPlans/1201/statistics").andExpect(status().isOk())
			.andExpect(jsonPath("$.applicationId").value(77))
			.andExpect(jsonPath("$.stageId").value(88))
			.andExpect(jsonPath("$.testPlanId").value(1201))
			.andExpect(jsonPath("$.totalRuns").value(10))
			.andExpect(jsonPath("$.successfulRuns").value(4))
			.andExpect(jsonPath("$.partialSuccessRuns").value(3))
			.andExpect(jsonPath("$.failedRuns").value(3))
			.andExpect(jsonPath("$._links.self.href",
					containsString("/applications/77/stages/88/testPlans/1201/statistics")))
			.andExpect(jsonPath("$._links.testPlan.href", containsString("/applications/77/stages/88/testPlans/1201")))
			.andExpect(jsonPath("$._links.collection.href", containsString("/applications/77/stages/88/testPlans")));
	}

	@Test
	void getTestRunStatistics_shouldReturnNotFound_whenMissing() throws Exception {
		when(testRunResultDatabaseService
			.getTestRunStatistics(new TestRunResultDatabaseService.TestPlanStatistics(77L, 88L, 1201L)))
			.thenReturn(Optional.empty());

		performGet("/applications/77/stages/88/testPlans/1201/statistics").andExpect(status().isNotFound());
	}

	@Test
	void getTestRunHistory_shouldReturnTimelineDataWithExpectedLinks_whenFound() throws Exception {
		PersistedTestRunResult first = new PersistedTestRunResult(1001L, 77L, 88L, 1201L,
				Timestamp.valueOf("2026-01-01 00:00:00"), ResultIndicator.SUCCESS, "first");
		PersistedTestRunResult second = new PersistedTestRunResult(1002L, 77L, 88L, 1201L,
				Timestamp.valueOf("2026-01-02 00:00:00"), ResultIndicator.FAILURE, "second");
		PersistedTestRunHistory history = new PersistedTestRunHistory(77L, 88L, 1201L, 2L, 1L, 0L, 1L,
				new PagedResult<>(List.of(first, second), 0, 2, 2));

		when(testRunResultDatabaseService
			.getTestRunHistory(new TestRunResultDatabaseService.TestPlanHistory(77L, 88L, 1201L, 0, 2)))
			.thenReturn(Optional.of(history));

		performGet("/applications/77/stages/88/testPlans/1201/metrics/history?page=0&size=2").andExpect(status().isOk())
			.andExpect(jsonPath("$.page").value(0))
			.andExpect(jsonPath("$.size").value(2))
			.andExpect(jsonPath("$.totalElements").value(2))
			.andExpect(jsonPath("$.totalPages").value(1))
			.andExpect(jsonPath("$.applicationId").value(77))
			.andExpect(jsonPath("$.stageId").value(88))
			.andExpect(jsonPath("$.testPlanId").value(1201))
			.andExpect(jsonPath("$.totalRuns").value(2))
			.andExpect(jsonPath("$.successfulRuns").value(1))
			.andExpect(jsonPath("$.partialSuccessRuns").value(0))
			.andExpect(jsonPath("$.failedRuns").value(1))
			.andExpect(jsonPath("$.items[0].id").value(1001))
			.andExpect(jsonPath("$.items[0].indicator").value("SUCCESS"))
			.andExpect(jsonPath("$.items[1].id").value(1002))
			.andExpect(jsonPath("$.items[1].indicator").value("FAILURE"))
			.andExpect(jsonPath("$._links.self.href",
					containsString("/applications/77/stages/88/testPlans/1201/metrics/history")))
			.andExpect(jsonPath("$._links.first.href", containsString("page=0")))
			.andExpect(jsonPath("$._links.last.href", containsString("page=0")))
			.andExpect(jsonPath("$._links.statistics.href",
					containsString("/applications/77/stages/88/testPlans/1201/statistics")))
			.andExpect(jsonPath("$._links.testPlan.href", containsString("/applications/77/stages/88/testPlans/1201")));
	}

	@Test
	void getTestRunHistory_shouldReturnNotFound_whenMissing() throws Exception {
		when(testRunResultDatabaseService
			.getTestRunHistory(new TestRunResultDatabaseService.TestPlanHistory(77L, 88L, 1201L, 0, 20)))
			.thenReturn(Optional.empty());

		performGet("/applications/77/stages/88/testPlans/1201/metrics/history").andExpect(status().isNotFound());
	}

}
