package io.github.yaforster.trails.adapter.api.rest.action;

import io.github.yaforster.trails.core.PagedResult;

import io.github.yaforster.trails.adapter.api.rest.model.PagedActionDTO;
import io.github.yaforster.trails.app.services.ActionQueryService;
import io.github.yaforster.trails.core.persisted.PersistedAction;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ActionControllerTest {

	private final ActionQueryService actionQueryService = mock(ActionQueryService.class);

	private final ActionHATEOASFacade facade = mock(ActionHATEOASFacade.class);

	private final ActionControllerValidator validator = mock(ActionControllerValidator.class);

	private final ActionController controller = new ActionController(actionQueryService, facade, validator);

	@Test
	void getActions_shouldReturnOkWhenFound() {
		PagedResult<PersistedAction> page = PagedResult.singlePage(
				List.of(new PersistedAction(9L, 3L, 7L, mock(io.github.yaforster.trails.core.test.Action.class))));
		PagedActionDTO dto = new PagedActionDTO();
		when(actionQueryService.listActions(new ActionQueryService.ActionPage(1L, 2L, 3L, 0, 20)))
			.thenReturn(Optional.of(page));
		when(facade.toPagedDTO(1L, 2L, 3L, page)).thenReturn(dto);

		ResponseEntity<PagedActionDTO> response = controller.getActions(1L, 2L, 3L, 0, 20);

		assertEquals(HttpStatus.OK, response.getStatusCode());
		assertSame(dto, response.getBody());
		verify(validator).validate(1L, 2L, 3L, 0, 20);
		verify(actionQueryService).listActions(new ActionQueryService.ActionPage(1L, 2L, 3L, 0, 20));
		verify(facade).toPagedDTO(1L, 2L, 3L, page);
	}

	@Test
	void getActions_shouldReturnNotFoundWhenMissing() {
		when(actionQueryService.listActions(new ActionQueryService.ActionPage(1L, 2L, 3L, 0, 20)))
			.thenReturn(Optional.empty());

		ResponseEntity<PagedActionDTO> response = controller.getActions(1L, 2L, 3L, 0, 20);

		assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
		assertNull(response.getBody());
		verify(validator).validate(1L, 2L, 3L, 0, 20);
		verify(actionQueryService).listActions(new ActionQueryService.ActionPage(1L, 2L, 3L, 0, 20));
		verifyNoInteractions(facade);
	}

}
