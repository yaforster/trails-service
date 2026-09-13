package io.github.yaforster.trails.adapter.api.rest.stage;

import io.github.yaforster.trails.adapter.api.rest.model.*;
import io.github.yaforster.trails.app.services.ElementDatabaseService;
import io.github.yaforster.trails.app.services.StageDatabaseService;
import io.github.yaforster.trails.core.PagedResult;
import io.github.yaforster.trails.core.definition.StageDefinition;
import io.github.yaforster.trails.core.deletion.DatabaseDeletionResult;
import io.github.yaforster.trails.core.persisted.PersistedStage;
import lombok.AllArgsConstructor;
import org.openapitools.api.StageApi;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.RestController;

@AllArgsConstructor
@RestController
public class StageController implements StageApi {

	private final StageDatabaseService stageDatabaseService;

	private final ElementDatabaseService elementDatabaseService;

	private final StageHATEOASFacade hateoasFacade;

	private final StageControllerValidator validator;

	@Override
	@PreAuthorize("@resourceAuthorization.canManageResources(authentication)")
	public ResponseEntity<PersistedStageDTO> createNewStage(Long applicationId, StageDefinitionDTO dto) {
		validator.validate(applicationId, dto);
		StageDefinition stageDefinition = hateoasFacade.toDomain(dto);
		PersistedStage persistedStage = stageDatabaseService
			.storeStage(new StageDatabaseService.StageCreation(applicationId, stageDefinition));
		PersistedStageDTO persistedStageDTO = hateoasFacade.toDTO(applicationId, persistedStage);
		return ResponseEntity.ok(persistedStageDTO);
	}

	@Override
	@PreAuthorize("@resourceAuthorization.canManageResources(authentication)")
	public ResponseEntity<DatabaseDeletionResultDTO> deleteStage(Long applicationId, Long stageId) {
		validator.validateDeleteStage(applicationId, stageId);
		DatabaseDeletionResult deletionResult = stageDatabaseService
			.deleteStage(new StageDatabaseService.StageReference(applicationId, stageId));
		DatabaseDeletionResultDTO dto = hateoasFacade.toDTO(applicationId, deletionResult);
		return ResponseEntity.status(hateoasFacade.toHTTPStatus(deletionResult)).body(dto);
	}

	@Override
	@PreAuthorize("@resourceAuthorization.canManageResources(authentication)")
	public ResponseEntity<DatabaseDeletionResultDTO> restoreStage(Long applicationId, Long stageId) {
		validator.validateDeleteStage(applicationId, stageId);
		DatabaseDeletionResult deletionResult = stageDatabaseService
			.restoreStage(new StageDatabaseService.StageReference(applicationId, stageId));
		DatabaseDeletionResultDTO dto = hateoasFacade.toDTO(applicationId, deletionResult);
		return ResponseEntity.status(hateoasFacade.toHTTPStatus(deletionResult)).body(dto);
	}

	@Override
	public ResponseEntity<StageDTO> getStage(Long applicationId, Long stageId, boolean includeRetired) {
		return stageDatabaseService
			.getStage(new StageDatabaseService.StageDetails(applicationId, stageId, includeRetired))
			.map(stage -> {
				boolean hasElements = elementDatabaseService
					.hasElements(new ElementDatabaseService.StageReference(applicationId, stageId));
				return hateoasFacade.mapToStageDTO(applicationId, stage, hasElements);
			})
			.map(ResponseEntity::ok)
			.orElse(ResponseEntity.notFound().build());
	}

	@Override
	public ResponseEntity<PagedStageDTO> listStages(Long applicationId, Integer page, Integer size,
			boolean includeRetired) {
		validator.validateListStages(applicationId, page, size);
		PagedResult<PersistedStage> pagedStages = stageDatabaseService
			.getStages(new StageDatabaseService.StagePage(applicationId, page, size, includeRetired));
		PagedStageDTO dto = hateoasFacade.mapToPagedStageDTO(applicationId, pagedStages);
		return ResponseEntity.ok(dto);
	}

}
