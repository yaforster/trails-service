package io.github.yaforster.trails.adapter.api.rest.testset;

import io.github.yaforster.trails.core.PagedResult;

import io.github.yaforster.trails.adapter.api.rest.model.PagedTestSetResultDTO;
import io.github.yaforster.trails.adapter.api.rest.model.TestSetResultDTO;
import io.github.yaforster.trails.app.services.TestSetResultQueryService;
import io.github.yaforster.trails.core.persisted.PersistedTestSetResult;
import io.github.yaforster.trails.core.test.Browser;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class TestSetControllerTest {

	private final TestSetResultQueryService queryService = mock(TestSetResultQueryService.class);

	private final TestSetHATEOASFacade facade = mock(TestSetHATEOASFacade.class);

	private final TestSetControllerValidator validator = mock(TestSetControllerValidator.class);

	private final TestSetController controller = new TestSetController(queryService, facade, validator);

	private static PersistedTestSetResult persisted(Long id, Long testRunId, Long applicationId, Long stageId) {
		return new PersistedTestSetResult(id, testRunId, applicationId, stageId, 42, Browser.CHROME, "plan",
				OffsetDateTime.parse("2026-01-01T00:00:00Z"), Browser.FIREFOX);
	}

	@Test
	void getBrowserResultInTestRun_shouldReturnOkWhenFound() {
		PersistedTestSetResult persisted = persisted(100L, 20L, 10L, 30L);
		TestSetResultDTO dto = new TestSetResultDTO().id(100L);
		when(queryService.getTestSetResult(100L)).thenReturn(Optional.of(persisted));
		when(facade.toDTO(persisted)).thenReturn(dto);

		ResponseEntity<TestSetResultDTO> response = controller.getBrowserResultInTestRun(20L, 10L, 30L, 100L);

		assertEquals(HttpStatus.OK, response.getStatusCode());
		assertSame(dto, response.getBody());
		verify(validator).validateGetBrowserResultInTestRun(20L, 10L, 30L, 100L);
		verify(queryService).getTestSetResult(100L);
		verify(facade).toDTO(persisted);
	}

	@Test
	void getBrowserResultInTestRun_shouldReturnNotFoundWhenMissing() {
		when(queryService.getTestSetResult(100L)).thenReturn(Optional.empty());

		ResponseEntity<TestSetResultDTO> response = controller.getBrowserResultInTestRun(20L, 10L, 30L, 100L);

		assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
		assertNull(response.getBody());
		verify(validator).validateGetBrowserResultInTestRun(20L, 10L, 30L, 100L);
		verify(queryService).getTestSetResult(100L);
		verifyNoInteractions(facade);
	}

	@Test
	void listBrowserResults_shouldForwardParametersAndReturnOkWhenFound() {
		Long testRunId = 9L;
		Long applicationId = 1L;
		Long stageId = 2L;
		int page = 3;
		int size = 25;

		PagedResult<PersistedTestSetResult> persistedPage = PagedResult
			.singlePage(List.of(persisted(100L, testRunId, applicationId, stageId)));
		PagedTestSetResultDTO dto = new PagedTestSetResultDTO();
		when(queryService.listBrowserResults(
				new TestSetResultQueryService.TestRunBrowserResults(applicationId, stageId, testRunId, page, size)))
			.thenReturn(Optional.of(persistedPage));
		when(facade.toPagedDTO(applicationId, stageId, testRunId, persistedPage)).thenReturn(dto);

		ResponseEntity<PagedTestSetResultDTO> response = controller.listBrowserResults(testRunId, applicationId,
				stageId, page, size);

		assertEquals(HttpStatus.OK, response.getStatusCode());
		assertSame(dto, response.getBody());
		verify(validator).validateListBrowserResults(testRunId, applicationId, stageId, page, size);
		verify(queryService).listBrowserResults(
				new TestSetResultQueryService.TestRunBrowserResults(applicationId, stageId, testRunId, page, size));
		verify(facade).toPagedDTO(applicationId, stageId, testRunId, persistedPage);
	}

	@Test
	void listBrowserResults_shouldReturnNotFoundWhenMissing() {
		when(queryService.listBrowserResults(new TestSetResultQueryService.TestRunBrowserResults(1L, 2L, 9L, 3, 25)))
			.thenReturn(Optional.empty());

		ResponseEntity<PagedTestSetResultDTO> response = controller.listBrowserResults(9L, 1L, 2L, 3, 25);

		assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
		assertNull(response.getBody());
		verify(validator).validateListBrowserResults(9L, 1L, 2L, 3, 25);
		verify(queryService).listBrowserResults(new TestSetResultQueryService.TestRunBrowserResults(1L, 2L, 9L, 3, 25));
		verifyNoInteractions(facade);
	}

}
