package io.github.yaforster.trails.adapter.api.rest.testpath;

import io.github.yaforster.trails.core.PagedResult;

import io.github.yaforster.trails.adapter.api.rest.model.PagedTestPathResultDTO;
import io.github.yaforster.trails.adapter.api.rest.model.TestPathResultDTO;
import io.github.yaforster.trails.app.services.HierarchyValidationService;
import io.github.yaforster.trails.app.services.TestPathResultQueryService;
import io.github.yaforster.trails.core.persisted.PersistedTestPathResult;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class TestPathControllerTest {

	private final TestPathResultQueryService queryService = mock(TestPathResultQueryService.class);

	private final HierarchyValidationService hierarchyValidationService = mock(HierarchyValidationService.class);

	private final TestPathHATEOASFacade facade = mock(TestPathHATEOASFacade.class);

	private final TestPathControllerValidator validator = mock(TestPathControllerValidator.class);

	private final TestPathController controller = new TestPathController(queryService, hierarchyValidationService,
			facade, validator);

	@Test
	void getPathResultInTestSet_shouldReturnOkWhenFound() {
		PersistedTestPathResult persistedPath = new PersistedTestPathResult(11L, 9L, "path");
		TestPathResultDTO dto = new TestPathResultDTO().id(11L);
		when(hierarchyValidationService
			.testSetBelongsToRun(new HierarchyValidationService.TestSetHierarchy(1L, 2L, 3L, 9L))).thenReturn(true);
		when(queryService.getPathResult(11L)).thenReturn(Optional.of(persistedPath));
		when(facade.toDTOInHierarchy(1L, 2L, 3L, persistedPath)).thenReturn(dto);

		ResponseEntity<TestPathResultDTO> response = controller.getPathResultInTestSet(1L, 2L, 3L, 9L, 11L);

		assertEquals(HttpStatus.OK, response.getStatusCode());
		assertSame(dto, response.getBody());
		verify(validator).validateGetPathResultInTestSet(1L, 2L, 3L, 9L, 11L);
	}

	@Test
	void getPathResultInTestSet_shouldReturnNotFoundWhenHierarchyDoesNotMatch() {
		when(hierarchyValidationService
			.testSetBelongsToRun(new HierarchyValidationService.TestSetHierarchy(1L, 2L, 3L, 9L))).thenReturn(false);

		ResponseEntity<TestPathResultDTO> response = controller.getPathResultInTestSet(1L, 2L, 3L, 9L, 11L);

		assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
		assertNull(response.getBody());
		verify(validator).validateGetPathResultInTestSet(1L, 2L, 3L, 9L, 11L);
		verifyNoInteractions(facade);
	}

	@Test
	void listPathResultsInTestSet_shouldForwardParametersAndReturnOkWhenFound() {
		PagedResult<PersistedTestPathResult> paged = PagedResult
			.singlePage(List.of(new PersistedTestPathResult(11L, 9L, "path")));
		PagedTestPathResultDTO dto = new PagedTestPathResultDTO();
		when(hierarchyValidationService
			.testSetBelongsToRun(new HierarchyValidationService.TestSetHierarchy(1L, 2L, 3L, 9L))).thenReturn(true);
		when(queryService.listPathResults(new TestPathResultQueryService.TestSetPathPage(9L, 1, 20)))
			.thenReturn(Optional.of(paged));
		when(facade.toPagedDTOInHierarchy(1L, 2L, 3L, 9L, paged)).thenReturn(dto);

		ResponseEntity<PagedTestPathResultDTO> response = controller.listPathResultsInTestSet(1L, 2L, 3L, 9L, 1, 20);

		assertEquals(HttpStatus.OK, response.getStatusCode());
		assertSame(dto, response.getBody());
		verify(validator).validateListPathResultsInTestSet(1L, 2L, 3L, 9L, 1, 20);
		verify(queryService).listPathResults(new TestPathResultQueryService.TestSetPathPage(9L, 1, 20));
		verify(facade).toPagedDTOInHierarchy(1L, 2L, 3L, 9L, paged);
	}

	@Test
	void listPathResultsInTestSet_shouldReturnNotFoundWhenMissing() {
		when(hierarchyValidationService
			.testSetBelongsToRun(new HierarchyValidationService.TestSetHierarchy(1L, 2L, 3L, 9L))).thenReturn(false);

		ResponseEntity<PagedTestPathResultDTO> response = controller.listPathResultsInTestSet(1L, 2L, 3L, 9L, 1, 20);

		assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
		assertNull(response.getBody());
		verify(validator).validateListPathResultsInTestSet(1L, 2L, 3L, 9L, 1, 20);
		verifyNoInteractions(facade);
	}

}
