package io.github.yaforster.trails.adapter.api.rest.application;

import io.github.yaforster.trails.adapter.api.rest.model.*;
import io.github.yaforster.trails.app.services.ApplicationDatabaseService;
import io.github.yaforster.trails.core.PagedResult;
import io.github.yaforster.trails.core.definition.ApplicationDefinition;
import io.github.yaforster.trails.core.deletion.DatabaseDeletionResult;
import io.github.yaforster.trails.core.persisted.PersistedApplication;
import lombok.AllArgsConstructor;
import org.openapitools.api.ApplicationApi;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.RestController;

@AllArgsConstructor
@RestController
public class ApplicationController implements ApplicationApi {

	private final ApplicationDatabaseService applicationDatabaseService;

	private final ApplicationHATEOASFacade hateoasFacade;

	private final ApplicationControllerValidator validator;

	@Override
	@PreAuthorize("@resourceAuthorization.canManageResources(authentication)")
	public ResponseEntity<PersistedApplicationDTO> createNewApplication(ApplicationDefinitionDTO dto) {
		validator.validateCreateNewApplication(dto);
		ApplicationDefinition applicationDefinition = hateoasFacade.toDomain(dto);
		PersistedApplication persistedApplication = applicationDatabaseService.storeApplication(applicationDefinition);
		PersistedApplicationDTO persistedApplicationDTO = hateoasFacade.toDTO(persistedApplication);
		return ResponseEntity.ok(persistedApplicationDTO);
	}

	@Override
	@PreAuthorize("@resourceAuthorization.canManageResources(authentication)")
	public ResponseEntity<DatabaseDeletionResultDTO> deleteApplication(Long id) {
		validator.validateDeleteApplication(id);
		DatabaseDeletionResult deletionResult = applicationDatabaseService.deleteApplication(id);
		DatabaseDeletionResultDTO dto = hateoasFacade.toDTO(deletionResult);
		return ResponseEntity.status(hateoasFacade.toHTTPStatus(deletionResult)).body(dto);
	}

	@Override
	@PreAuthorize("@resourceAuthorization.canManageResources(authentication)")
	public ResponseEntity<DatabaseDeletionResultDTO> restoreApplication(Long id) {
		validator.validateDeleteApplication(id);
		DatabaseDeletionResult deletionResult = applicationDatabaseService.restoreApplication(id);
		DatabaseDeletionResultDTO dto = hateoasFacade.toDTO(deletionResult);
		return ResponseEntity.status(hateoasFacade.toHTTPStatus(deletionResult)).body(dto);
	}

	@Override
	public ResponseEntity<ApplicationDTO> getApplication(Long id, boolean includeRetired) {
		return applicationDatabaseService
			.getApplication(new ApplicationDatabaseService.ApplicationDetails(id, includeRetired))
			.map(hateoasFacade::mapToApplicationDTO)
			.map(ResponseEntity::ok)
			.orElse(ResponseEntity.notFound().build());
	}

	@Override
	public ResponseEntity<PagedApplicationDTO> listApplications(Integer page, Integer size, boolean includeRetired) {
		validator.validateListApplications(page, size);
		PagedResult<PersistedApplication> pagedApplications = applicationDatabaseService
			.getApplications(new ApplicationDatabaseService.ApplicationPage(page, size, includeRetired));
		PagedApplicationDTO dto = hateoasFacade.mapToPagedApplicationDTO(pagedApplications);
		return ResponseEntity.ok(dto);
	}

}
