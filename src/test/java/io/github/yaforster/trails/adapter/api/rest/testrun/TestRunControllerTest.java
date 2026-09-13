package io.github.yaforster.trails.adapter.api.rest.testrun;

import io.github.yaforster.trails.core.PagedResult;

import io.github.yaforster.trails.adapter.api.rest.model.PagedTestRunDTO;
import io.github.yaforster.trails.adapter.api.rest.model.PagedTestRunHistoryDTO;
import io.github.yaforster.trails.adapter.api.rest.model.PersistedTestRunResultDTO;
import io.github.yaforster.trails.adapter.api.rest.model.TestRunStatisticsDTO;
import io.github.yaforster.trails.app.services.TestRunResultDatabaseService;
import io.github.yaforster.trails.core.data.ResultIndicator;
import io.github.yaforster.trails.core.persisted.PersistedTestRunHistory;
import io.github.yaforster.trails.core.persisted.PersistedTestRunResult;
import io.github.yaforster.trails.core.persisted.PersistedTestRunStatistics;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.sql.Timestamp;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class TestRunControllerTest {

	private final TestRunHATEOASFacade facade = mock(TestRunHATEOASFacade.class);

	private final TestRunResultDatabaseService databaseService = mock(TestRunResultDatabaseService.class);

	private final TestRunControllerValidator validator = mock(TestRunControllerValidator.class);

	private final TestRunController controller = new TestRunController(facade, databaseService, validator);

	private static PersistedTestRunResult persisted(Long id, Long applicationId, Long stageId) {
		return new PersistedTestRunResult(id, applicationId, stageId, 4L, Timestamp.valueOf("2026-01-01 00:00:00"),
				ResultIndicator.SUCCESS, "label");
	}

	@Test
	void getTestRun_shouldReturnOkWhenFound() {
		PersistedTestRunResult persisted = persisted(9L, 1L, 2L);
		PersistedTestRunResultDTO dto = new PersistedTestRunResultDTO().id(9L);
		when(databaseService.getTestRunResult(new TestRunResultDatabaseService.TestRunResultDetails(1L, 2L, 9L)))
			.thenReturn(Optional.of(persisted));
		when(facade.toDTO(persisted)).thenReturn(dto);

		ResponseEntity<PersistedTestRunResultDTO> response = controller.getTestRun(9L, 1L, 2L);

		assertEquals(HttpStatus.OK, response.getStatusCode());
		assertSame(dto, response.getBody());
		verify(validator).validateGetTestRun(9L, 1L, 2L);
		verify(databaseService).getTestRunResult(new TestRunResultDatabaseService.TestRunResultDetails(1L, 2L, 9L));
		verify(facade).toDTO(persisted);
	}

	@Test
	void getTestRun_shouldReturnNotFoundWhenMissing() {
		when(databaseService.getTestRunResult(new TestRunResultDatabaseService.TestRunResultDetails(1L, 2L, 9L)))
			.thenReturn(Optional.empty());

		ResponseEntity<PersistedTestRunResultDTO> response = controller.getTestRun(9L, 1L, 2L);

		assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
		assertNull(response.getBody());
		verify(validator).validateGetTestRun(9L, 1L, 2L);
		verify(databaseService).getTestRunResult(new TestRunResultDatabaseService.TestRunResultDetails(1L, 2L, 9L));
		verifyNoInteractions(facade);
	}

	@Test
	void listTestRuns_shouldReturnOkWhenFound() {
		PagedResult<PersistedTestRunResult> paged = PagedResult.singlePage(List.of(persisted(9L, 1L, 2L)));
		PagedTestRunDTO dto = new PagedTestRunDTO();
		when(databaseService.listTestRuns(new TestRunResultDatabaseService.TestRunPage(1L, 2L, 3, 25)))
			.thenReturn(Optional.of(paged));
		when(facade.toPagedDTO(1L, 2L, paged)).thenReturn(dto);

		ResponseEntity<PagedTestRunDTO> response = controller.listTestRuns(1L, 2L, 3, 25);

		assertEquals(HttpStatus.OK, response.getStatusCode());
		assertSame(dto, response.getBody());
		verify(validator).validateListTestRuns(1L, 2L, 3, 25);
		verify(databaseService).listTestRuns(new TestRunResultDatabaseService.TestRunPage(1L, 2L, 3, 25));
		verify(facade).toPagedDTO(1L, 2L, paged);
	}

	@Test
	void listTestRuns_shouldReturnNotFoundWhenMissing() {
		when(databaseService.listTestRuns(new TestRunResultDatabaseService.TestRunPage(1L, 2L, 3, 25)))
			.thenReturn(Optional.empty());

		ResponseEntity<PagedTestRunDTO> response = controller.listTestRuns(1L, 2L, 3, 25);

		assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
		assertNull(response.getBody());
		verify(validator).validateListTestRuns(1L, 2L, 3, 25);
		verify(databaseService).listTestRuns(new TestRunResultDatabaseService.TestRunPage(1L, 2L, 3, 25));
		verifyNoInteractions(facade);
	}

	@Test
	void getTestRunStatistics_shouldReturnOkWhenFound() {
		PersistedTestRunStatistics statistics = new PersistedTestRunStatistics(1L, 2L, 9L, 10L, 5L, 3L, 2L);
		TestRunStatisticsDTO dto = new TestRunStatisticsDTO();
		when(databaseService.getTestRunStatistics(new TestRunResultDatabaseService.TestPlanStatistics(1L, 2L, 9L)))
			.thenReturn(Optional.of(statistics));
		when(facade.toStatisticsDTO(statistics)).thenReturn(dto);

		ResponseEntity<TestRunStatisticsDTO> response = controller.getTestRunStatistics(1L, 2L, 9L);

		assertEquals(HttpStatus.OK, response.getStatusCode());
		assertSame(dto, response.getBody());
		verify(validator).validateGetTestRunStatistics(1L, 2L, 9L);
		verify(databaseService).getTestRunStatistics(new TestRunResultDatabaseService.TestPlanStatistics(1L, 2L, 9L));
		verify(facade).toStatisticsDTO(statistics);
	}

	@Test
	void getTestRunStatistics_shouldReturnNotFoundWhenMissing() {
		when(databaseService.getTestRunStatistics(new TestRunResultDatabaseService.TestPlanStatistics(1L, 2L, 9L)))
			.thenReturn(Optional.empty());

		ResponseEntity<TestRunStatisticsDTO> response = controller.getTestRunStatistics(1L, 2L, 9L);

		assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
		assertNull(response.getBody());
		verify(validator).validateGetTestRunStatistics(1L, 2L, 9L);
		verify(databaseService).getTestRunStatistics(new TestRunResultDatabaseService.TestPlanStatistics(1L, 2L, 9L));
		verifyNoMoreInteractions(facade);
	}

	@Test
	void getTestRunHistory_shouldReturnOkWhenFound() {
		PersistedTestRunHistory history = new PersistedTestRunHistory(1L, 2L, 9L, 10L, 5L, 3L, 2L,
				new PagedResult<>(List.of(persisted(11L, 1L, 2L)), 3, 25, 100));
		PagedTestRunHistoryDTO dto = new PagedTestRunHistoryDTO();
		when(databaseService.getTestRunHistory(new TestRunResultDatabaseService.TestPlanHistory(1L, 2L, 9L, 3, 25)))
			.thenReturn(Optional.of(history));
		when(facade.toHistoryDTO(history)).thenReturn(dto);

		ResponseEntity<PagedTestRunHistoryDTO> response = controller.getTestRunHistory(1L, 2L, 9L, 3, 25);

		assertEquals(HttpStatus.OK, response.getStatusCode());
		assertSame(dto, response.getBody());
		verify(validator).validateGetTestRunHistory(1L, 2L, 9L, 3, 25);
		verify(databaseService).getTestRunHistory(new TestRunResultDatabaseService.TestPlanHistory(1L, 2L, 9L, 3, 25));
		verify(facade).toHistoryDTO(history);
	}

	@Test
	void getTestRunHistory_shouldReturnNotFoundWhenMissing() {
		when(databaseService.getTestRunHistory(new TestRunResultDatabaseService.TestPlanHistory(1L, 2L, 9L, 0, 50)))
			.thenReturn(Optional.empty());

		ResponseEntity<PagedTestRunHistoryDTO> response = controller.getTestRunHistory(1L, 2L, 9L, 0, 50);

		assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
		assertNull(response.getBody());
		verify(validator).validateGetTestRunHistory(1L, 2L, 9L, 0, 50);
		verify(databaseService).getTestRunHistory(new TestRunResultDatabaseService.TestPlanHistory(1L, 2L, 9L, 0, 50));
		verifyNoMoreInteractions(facade);
	}

}
