package io.github.yaforster.trails.adapter.api.rest.artifact;

import io.github.yaforster.trails.adapter.api.rest.validation.ValidationContext;
import io.github.yaforster.trails.adapter.api.rest.validation.PaginationValidator;
import io.github.yaforster.trails.core.ValidationViolation;
import org.springframework.stereotype.Component;

@Component
public class ArtifactControllerValidator {

	public void validateDownloadArtifact(Long applicationId, Long stageId, Long testRunId, Long testSetResultId,
			Long pathResultId, Long artifactId) {
		ValidationContext validation = ValidationContext.begin();
		validateArtifactHierarchy(validation, applicationId, stageId, testRunId, testSetResultId, pathResultId);
		if (artifactId == null) {
			validation.addViolation(
					new ValidationViolation("ARTIFACT_ID_NULL", "Artifact id must not be null.", "/artifactId"));
		}
		validation.rejectIfErrors();
	}

	public void validateDownloadActionResultScreenshot(Long applicationId, Long stageId, Long testRunId,
			Long testSetResultId, Long pathResultId, Long actionResultId) {
		ValidationContext validation = ValidationContext.begin();
		validateArtifactHierarchy(validation, applicationId, stageId, testRunId, testSetResultId, pathResultId);
		if (actionResultId == null) {
			validation.addViolation(new ValidationViolation("ARTIFACT_ACTION_RESULT_ID_NULL",
					"Action result id must not be null.", "/actionResultId"));
		}
		validation.rejectIfErrors();
	}

	public void validateListArtifacts(Long applicationId, Long stageId, Long testRunId, Long testSetResultId,
			Long pathResultId, Integer page, Integer size) {
		ValidationContext validation = ValidationContext.begin();
		validateArtifactHierarchy(validation, applicationId, stageId, testRunId, testSetResultId, pathResultId);
		PaginationValidator.validate(validation, page, size, "ARTIFACT");
		validation.rejectIfErrors();
	}

	private void validateArtifactHierarchy(ValidationContext validation, Long applicationId, Long stageId,
			Long testRunId, Long testSetResultId, Long pathResultId) {
		if (applicationId == null) {
			validation.addViolation(new ValidationViolation("ARTIFACT_APPLICATION_ID_NULL",
					"Application id must not be null.", "/applicationId"));
		}
		if (stageId == null) {
			validation.addViolation(
					new ValidationViolation("ARTIFACT_STAGE_ID_NULL", "Stage id must not be null.", "/stageId"));
		}
		if (testRunId == null) {
			validation.addViolation(new ValidationViolation("ARTIFACT_TEST_RUN_ID_NULL",
					"Test run id must not be null.", "/testRunId"));
		}
		if (testSetResultId == null) {
			validation.addViolation(new ValidationViolation("ARTIFACT_TEST_SET_RESULT_ID_NULL",
					"Test set result id must not be null.", "/testSetResultId"));
		}
		if (pathResultId == null) {
			validation.addViolation(new ValidationViolation("ARTIFACT_PATH_RESULT_ID_NULL",
					"Path result id must not be null.", "/pathResultId"));
		}
	}

}
