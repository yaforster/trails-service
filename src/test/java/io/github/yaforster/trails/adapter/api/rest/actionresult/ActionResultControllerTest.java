package io.github.yaforster.trails.adapter.api.rest.actionresult;

import io.github.yaforster.trails.adapter.api.rest.model.ActionResultChainDTO;
import io.github.yaforster.trails.app.services.ActionResultQueryService;
import io.github.yaforster.trails.app.services.HierarchyValidationService;
import io.github.yaforster.trails.core.persisted.ActionResultType;
import io.github.yaforster.trails.core.persisted.PersistedActionResult;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ActionResultControllerTest {

	private final ActionResultQueryService databaseService = mock(ActionResultQueryService.class);

	private final HierarchyValidationService hierarchyValidationService = mock(HierarchyValidationService.class);

	private final ActionResultHATEOASFacade facade = mock(ActionResultHATEOASFacade.class);

	private final ActionResultControllerValidator validator = mock(ActionResultControllerValidator.class);

	private final ActionResultController controller = new ActionResultController(databaseService,
			hierarchyValidationService, facade, validator);

	@Test
	void listActionResultsChainInTestPath_shouldReturnOkWhenFound() {
		List<PersistedActionResult> chain = List
			.of(new PersistedActionResult(9L, 100L, 7L, "first", "ok", ActionResultType.SUCCESS, null));
		ActionResultChainDTO dto = new ActionResultChainDTO();
		when(hierarchyValidationService
			.pathBelongsToHierarchy(new HierarchyValidationService.TestPathHierarchy(1L, 2L, 3L, 4L, 100L)))
			.thenReturn(true);
		when(databaseService.listActionResultChain(100L)).thenReturn(Optional.of(chain));
		when(facade.toChainDTOInHierarchy(1L, 2L, 3L, 4L, 100L, chain)).thenReturn(dto);

		ResponseEntity<ActionResultChainDTO> response = controller.listActionResultsChainInTestPath(1L, 2L, 3L, 4L,
				100L);

		assertEquals(HttpStatus.OK, response.getStatusCode());
		assertSame(dto, response.getBody());
		verify(validator).validate(1L, 2L, 3L, 4L, 100L);
		verify(databaseService).listActionResultChain(100L);
	}

	@Test
	void listActionResultsChainInTestPath_shouldReturnNotFoundWhenHierarchyMissing() {
		when(hierarchyValidationService
			.pathBelongsToHierarchy(new HierarchyValidationService.TestPathHierarchy(1L, 2L, 3L, 4L, 100L)))
			.thenReturn(false);

		ResponseEntity<ActionResultChainDTO> response = controller.listActionResultsChainInTestPath(1L, 2L, 3L, 4L,
				100L);

		assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
		assertNull(response.getBody());
		verify(validator).validate(1L, 2L, 3L, 4L, 100L);
		verifyNoInteractions(facade);
	}

}
