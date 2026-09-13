package io.github.yaforster.trails.adapter.api.rest.deployment;

import io.github.yaforster.trails.adapter.api.rest.model.DeploymentDefinitionDTO;
import io.github.yaforster.trails.adapter.api.rest.validation.ValidationContext;
import io.github.yaforster.trails.adapter.api.rest.validation.PaginationValidator;
import io.github.yaforster.trails.core.ValidationViolation;
import org.springframework.stereotype.Component;

@Component
public class DeploymentControllerValidator {

	public void validateReportDeployment(Long applicationId, Long stageId, DeploymentDefinitionDTO dto) {
		ValidationContext validation = ValidationContext.begin();
		validateApplicationAndStage(validation, applicationId, stageId);

		if (dto == null || dto.getVersion() == null || dto.getVersion().isBlank()) {
			validation.addViolation(new ValidationViolation("DEPLOYMENT_VERSION_EMPTY",
					"Deployment version must not be empty.", "/version"));
		}

		if (dto == null || dto.getDeployedAt() == null) {
			validation.addViolation(new ValidationViolation("DEPLOYMENT_TIMESTAMP_NULL",
					"Deployment timestamp must not be null.", "/deployedAt"));
		}

		validation.rejectIfErrors();
	}

	public void validateGetDeployment(Long applicationId, Long stageId, Long deploymentId) {
		ValidationContext validation = ValidationContext.begin();
		validateApplicationAndStage(validation, applicationId, stageId);

		if (deploymentId == null) {
			validation.addViolation(
					new ValidationViolation("DEPLOYMENT_ID_NULL", "Deployment id must not be null.", "/deploymentId"));
		}

		validation.rejectIfErrors();
	}

	public void validateListDeployments(Long applicationId, Long stageId, Integer page, Integer size) {
		ValidationContext validation = ValidationContext.begin();
		validateApplicationAndStage(validation, applicationId, stageId);

		PaginationValidator.validateWithGenericCodes(validation, page, size);

		validation.rejectIfErrors();
	}

	private void validateApplicationAndStage(ValidationContext validation, Long applicationId, Long stageId) {
		if (applicationId == null) {
			validation.addViolation(new ValidationViolation("DEPLOYMENT_APPLICATION_ID_NULL",
					"Application id must not be null.", "/applicationId"));
		}

		if (stageId == null) {
			validation.addViolation(
					new ValidationViolation("DEPLOYMENT_STAGE_ID_NULL", "Stage id must not be null.", "/stageId"));
		}
	}

}
