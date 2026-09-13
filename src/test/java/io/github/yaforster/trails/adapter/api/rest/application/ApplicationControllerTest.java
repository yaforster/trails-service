package io.github.yaforster.trails.adapter.api.rest.application;

import io.github.yaforster.trails.core.PagedResult;

import io.github.yaforster.trails.adapter.api.rest.model.*;
import io.github.yaforster.trails.app.services.ApplicationDatabaseService;
import io.github.yaforster.trails.core.definition.ApplicationDefinition;
import io.github.yaforster.trails.core.deletion.DatabaseDeletionResult;
import io.github.yaforster.trails.core.deletion.DeletionSuccess;
import io.github.yaforster.trails.core.persisted.PersistedApplication;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ApplicationControllerTest {

	private final ApplicationDatabaseService applicationDatabaseService = mock(ApplicationDatabaseService.class);

	private final ApplicationHATEOASFacade facade = mock(ApplicationHATEOASFacade.class);

	private final ApplicationControllerValidator validator = mock(ApplicationControllerValidator.class);

	private final ApplicationController controller = new ApplicationController(applicationDatabaseService, facade,
			validator);

	@Test
	void createNewApplication_shouldMapStoreAndReturnOk() {
		ApplicationDefinitionDTO definitionDTO = new ApplicationDefinitionDTO().label("app");
		ApplicationDefinition definition = new ApplicationDefinition("app");
		PersistedApplication persisted = new PersistedApplication(9L, "app");
		PersistedApplicationDTO dto = new PersistedApplicationDTO().id(9L);
		when(facade.toDomain(definitionDTO)).thenReturn(definition);
		when(applicationDatabaseService.storeApplication(definition)).thenReturn(persisted);
		when(facade.toDTO(persisted)).thenReturn(dto);

		ResponseEntity<PersistedApplicationDTO> response = controller.createNewApplication(definitionDTO);

		assertEquals(HttpStatus.OK, response.getStatusCode());
		assertSame(dto, response.getBody());
		verify(validator).validateCreateNewApplication(definitionDTO);
		verify(facade).toDomain(definitionDTO);
		verify(applicationDatabaseService).storeApplication(definition);
		verify(facade).toDTO(persisted);
	}

	@Test
	void getApplication_shouldReturnOkWhenFound() {
		PersistedApplication persisted = new PersistedApplication(9L, "app");
		ApplicationDTO dto = new ApplicationDTO().id(9L);
		when(applicationDatabaseService.getApplication(new ApplicationDatabaseService.ApplicationDetails(9L, false)))
			.thenReturn(Optional.of(persisted));
		when(facade.mapToApplicationDTO(persisted)).thenReturn(dto);

		ResponseEntity<ApplicationDTO> response = controller.getApplication(9L, false);

		assertEquals(HttpStatus.OK, response.getStatusCode());
		assertSame(dto, response.getBody());
		verify(applicationDatabaseService).getApplication(new ApplicationDatabaseService.ApplicationDetails(9L, false));
		verify(facade).mapToApplicationDTO(persisted);
	}

	@Test
	void deleteApplication_shouldReturnStatusFromDeletionResult() {
		DatabaseDeletionResult deletionResult = new DeletionSuccess(9L);
		DatabaseDeletionResultDTO dto = new DatabaseDeletionResultDTO();
		when(applicationDatabaseService.deleteApplication(9L)).thenReturn(deletionResult);
		when(facade.toDTO(deletionResult)).thenReturn(dto);
		when(facade.toHTTPStatus(deletionResult)).thenReturn(HttpStatus.OK);

		ResponseEntity<DatabaseDeletionResultDTO> response = controller.deleteApplication(9L);

		assertEquals(HttpStatus.OK, response.getStatusCode());
		assertSame(dto, response.getBody());
		verify(validator).validateDeleteApplication(9L);
		verify(applicationDatabaseService).deleteApplication(9L);
		verify(facade).toDTO(deletionResult);
		verify(facade).toHTTPStatus(deletionResult);
	}

	@Test
	void restoreApplication_shouldReturnStatusFromRestoreResult() {
		DatabaseDeletionResult deletionResult = new DeletionSuccess(9L);
		DatabaseDeletionResultDTO dto = new DatabaseDeletionResultDTO();
		when(applicationDatabaseService.restoreApplication(9L)).thenReturn(deletionResult);
		when(facade.toDTO(deletionResult)).thenReturn(dto);
		when(facade.toHTTPStatus(deletionResult)).thenReturn(HttpStatus.OK);

		ResponseEntity<DatabaseDeletionResultDTO> response = controller.restoreApplication(9L);

		assertEquals(HttpStatus.OK, response.getStatusCode());
		assertSame(dto, response.getBody());
		verify(validator).validateDeleteApplication(9L);
		verify(applicationDatabaseService).restoreApplication(9L);
		verify(facade).toDTO(deletionResult);
		verify(facade).toHTTPStatus(deletionResult);
	}

	@Test
	void getApplication_shouldReturnNotFoundWhenMissing() {
		when(applicationDatabaseService.getApplication(new ApplicationDatabaseService.ApplicationDetails(9L, false)))
			.thenReturn(Optional.empty());

		ResponseEntity<ApplicationDTO> response = controller.getApplication(9L, false);

		assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
		assertNull(response.getBody());
		verify(applicationDatabaseService).getApplication(new ApplicationDatabaseService.ApplicationDetails(9L, false));
		verifyNoInteractions(facade);
	}

	@Test
	void listApplications_shouldReturnOkWithMappedPage() {
		PagedResult<PersistedApplication> paged = PagedResult.singlePage(List.of(new PersistedApplication(9L, "app")));
		PagedApplicationDTO dto = new PagedApplicationDTO();
		when(applicationDatabaseService.getApplications(new ApplicationDatabaseService.ApplicationPage(0, 20, false)))
			.thenReturn(paged);
		when(facade.mapToPagedApplicationDTO(paged)).thenReturn(dto);

		ResponseEntity<PagedApplicationDTO> response = controller.listApplications(0, 20, false);

		assertEquals(HttpStatus.OK, response.getStatusCode());
		assertSame(dto, response.getBody());
		verify(validator).validateListApplications(0, 20);
		verify(applicationDatabaseService)
			.getApplications(new ApplicationDatabaseService.ApplicationPage(0, 20, false));
		verify(facade).mapToPagedApplicationDTO(paged);
	}

}
