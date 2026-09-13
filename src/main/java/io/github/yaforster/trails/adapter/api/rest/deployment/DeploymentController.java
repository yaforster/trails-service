package io.github.yaforster.trails.adapter.api.rest.deployment;

import io.github.yaforster.trails.adapter.api.rest.model.DeploymentDTO;
import io.github.yaforster.trails.adapter.api.rest.model.DeploymentDefinitionDTO;
import io.github.yaforster.trails.adapter.api.rest.model.PagedDeploymentDTO;
import io.github.yaforster.trails.app.services.DeploymentDatabaseService;
import io.github.yaforster.trails.core.definition.DeploymentDefinition;
import io.github.yaforster.trails.core.persisted.PersistedDeployment;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.openapitools.api.DeploymentApi;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@AllArgsConstructor
@RestController
public class DeploymentController implements DeploymentApi {

	private final DeploymentDatabaseService deploymentDatabaseService;

	private final DeploymentHATEOASFacade hateoasFacade;

	private final DeploymentControllerValidator validator;

	@Override
	public ResponseEntity<DeploymentDTO> getDeployment(Long applicationId, Long stageId, Long deploymentId) {
		validator.validateGetDeployment(applicationId, stageId, deploymentId);
		return deploymentDatabaseService
			.getDeployment(new DeploymentDatabaseService.DeploymentDetails(applicationId, stageId, deploymentId))
			.map(hateoasFacade::toDTO)
			.map(ResponseEntity::ok)
			.orElse(ResponseEntity.notFound().build());
	}

	@Override
	public ResponseEntity<PagedDeploymentDTO> listDeployments(Long applicationId, Long stageId, Integer page,
			Integer size) {
		validator.validateListDeployments(applicationId, stageId, page, size);
		return deploymentDatabaseService
			.listDeployments(new DeploymentDatabaseService.StageDeploymentPage(applicationId, stageId, page, size))
			.map(deployments -> hateoasFacade.toPagedDTO(applicationId, stageId, deployments))
			.map(ResponseEntity::ok)
			.orElse(ResponseEntity.notFound().build());
	}

	@Override
	@PreAuthorize("@resourceAuthorization.canReportDeployments(authentication)")
	public ResponseEntity<DeploymentDTO> reportDeployment(Long applicationId, Long stageId,
			DeploymentDefinitionDTO dto) {
		validator.validateReportDeployment(applicationId, stageId, dto);
		log.info("Accepted deployment report: applicationId={}, stageId={}, version={}, deployedAt={}", applicationId,
				stageId, dto.getVersion().trim(), dto.getDeployedAt());
		DeploymentDefinition definition = hateoasFacade.toDomain(dto);
		return deploymentDatabaseService
			.storeDeployment(new DeploymentDatabaseService.StageDeployment(applicationId, stageId, definition))
			.map(this::created)
			.orElse(ResponseEntity.notFound().build());
	}

	private ResponseEntity<DeploymentDTO> created(PersistedDeployment deployment) {
		return ResponseEntity.created(hateoasFacade.location(deployment)).body(hateoasFacade.toDTO(deployment));
	}

}
