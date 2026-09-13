package io.github.yaforster.trails.adapter.api.rest.stage;

import io.github.yaforster.trails.core.PagedResult;

import io.github.yaforster.trails.adapter.api.rest.model.DatabaseDeletionResultDTO;
import io.github.yaforster.trails.adapter.api.rest.model.PagedStageDTO;
import io.github.yaforster.trails.adapter.api.rest.model.PersistedStageDTO;
import io.github.yaforster.trails.adapter.api.rest.model.StageDTO;
import io.github.yaforster.trails.adapter.api.rest.model.StageDefinitionDTO;
import io.github.yaforster.trails.app.services.ElementDatabaseService;
import io.github.yaforster.trails.app.services.StageDatabaseService;
import io.github.yaforster.trails.core.definition.StageDefinition;
import io.github.yaforster.trails.core.deletion.DatabaseDeletionResult;
import io.github.yaforster.trails.core.deletion.DeletionFailure;
import io.github.yaforster.trails.core.persisted.PersistedStage;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.net.URI;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class StageControllerTest {

	private final StageDatabaseService stageDatabaseService = mock(StageDatabaseService.class);

	private final ElementDatabaseService elementDatabaseService = mock(ElementDatabaseService.class);

	private final StageHATEOASFacade facade = mock(StageHATEOASFacade.class);

	private final StageControllerValidator validator = mock(StageControllerValidator.class);

	private final StageController controller = new StageController(stageDatabaseService, elementDatabaseService, facade,
			validator);

	@Test
	void createNewStage_shouldMapStoreAndReturnOk() {
		StageDefinitionDTO definitionDTO = new StageDefinitionDTO().label("stage")
			.url(URI.create("https://example.org"));
		StageDefinition definition = new StageDefinition("stage", "https://example.org", List.of());
		PersistedStage persisted = new PersistedStage(9L, 1L, "stage", "https://example.org");
		PersistedStageDTO dto = new PersistedStageDTO().id(9L);
		when(facade.toDomain(definitionDTO)).thenReturn(definition);
		when(stageDatabaseService.storeStage(new StageDatabaseService.StageCreation(1L, definition)))
			.thenReturn(persisted);
		when(facade.toDTO(1L, persisted)).thenReturn(dto);

		ResponseEntity<PersistedStageDTO> response = controller.createNewStage(1L, definitionDTO);

		assertEquals(HttpStatus.OK, response.getStatusCode());
		assertSame(dto, response.getBody());
		verify(validator).validate(1L, definitionDTO);
		verify(facade).toDomain(definitionDTO);
		verify(stageDatabaseService).storeStage(new StageDatabaseService.StageCreation(1L, definition));
		verify(facade).toDTO(1L, persisted);
	}

	@Test
	void getStage_shouldReturnOkWhenFound() {
		PersistedStage persisted = new PersistedStage(9L, 1L, "stage", "https://example.org");
		StageDTO dto = new StageDTO().id(9L);
		when(stageDatabaseService.getStage(new StageDatabaseService.StageDetails(1L, 9L, false)))
			.thenReturn(Optional.of(persisted));
		when(elementDatabaseService.hasElements(new ElementDatabaseService.StageReference(1L, 9L))).thenReturn(true);
		when(facade.mapToStageDTO(1L, persisted, true)).thenReturn(dto);

		ResponseEntity<StageDTO> response = controller.getStage(1L, 9L, false);

		assertEquals(HttpStatus.OK, response.getStatusCode());
		assertSame(dto, response.getBody());
		verify(stageDatabaseService).getStage(new StageDatabaseService.StageDetails(1L, 9L, false));
		verify(elementDatabaseService).hasElements(new ElementDatabaseService.StageReference(1L, 9L));
		verify(facade).mapToStageDTO(1L, persisted, true);
	}

	@Test
	void deleteStage_shouldReturnStatusFromDeletionResult() {
		DatabaseDeletionResult deletionResult = new DeletionFailure(9L, "conflict", null);
		DatabaseDeletionResultDTO dto = new DatabaseDeletionResultDTO();
		when(stageDatabaseService.deleteStage(new StageDatabaseService.StageReference(1L, 9L)))
			.thenReturn(deletionResult);
		when(facade.toDTO(1L, deletionResult)).thenReturn(dto);
		when(facade.toHTTPStatus(deletionResult)).thenReturn(HttpStatus.CONFLICT);

		ResponseEntity<DatabaseDeletionResultDTO> response = controller.deleteStage(1L, 9L);

		assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
		assertSame(dto, response.getBody());
		verify(validator).validateDeleteStage(1L, 9L);
		verify(stageDatabaseService).deleteStage(new StageDatabaseService.StageReference(1L, 9L));
		verify(facade).toDTO(1L, deletionResult);
	}

	@Test
	void getStage_shouldReturnNotFoundWhenMissing() {
		when(stageDatabaseService.getStage(new StageDatabaseService.StageDetails(1L, 9L, false)))
			.thenReturn(Optional.empty());

		ResponseEntity<StageDTO> response = controller.getStage(1L, 9L, false);

		assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
		assertNull(response.getBody());
		verify(stageDatabaseService).getStage(new StageDatabaseService.StageDetails(1L, 9L, false));
		verifyNoInteractions(elementDatabaseService, facade);
	}

	@Test
	void listStages_shouldReturnOkWithMappedPage() {
		PagedResult<PersistedStage> pagedStages = PagedResult
			.singlePage(List.of(new PersistedStage(9L, 1L, "stage", "https://example.org")));
		PagedStageDTO dto = new PagedStageDTO();
		when(stageDatabaseService.getStages(new StageDatabaseService.StagePage(1L, 0, 20, false)))
			.thenReturn(pagedStages);
		when(facade.mapToPagedStageDTO(1L, pagedStages)).thenReturn(dto);

		ResponseEntity<PagedStageDTO> response = controller.listStages(1L, 0, 20, false);

		assertEquals(HttpStatus.OK, response.getStatusCode());
		assertSame(dto, response.getBody());
		verify(validator).validateListStages(1L, 0, 20);
		verify(stageDatabaseService).getStages(new StageDatabaseService.StagePage(1L, 0, 20, false));
		verify(facade).mapToPagedStageDTO(1L, pagedStages);
	}

}
