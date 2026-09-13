package io.github.yaforster.trails.adapter.api.rest.testpath;

import io.github.yaforster.trails.adapter.api.rest.validation.ValidationContext;
import io.github.yaforster.trails.adapter.api.rest.validation.PaginationValidator;
import io.github.yaforster.trails.core.ValidationViolation;
import org.springframework.stereotype.Component;

@Component
public class TestPathControllerValidator {

	public void validateListPathResultsInTestSet(Long applicationId, Long stageId, Long testRunId, Long testSetResultId,
			Integer page, Integer size) {
		ValidationContext validation = ValidationContext.begin();
		validateTestSet(validation, applicationId, stageId, testRunId, testSetResultId);
		PaginationValidator.validate(validation, page, size, "TEST_PATH");
		validation.rejectIfErrors();
	}

	public void validateGetPathResultInTestSet(Long applicationId, Long stageId, Long testRunId, Long testSetResultId,
			Long pathResultId) {
		ValidationContext validation = ValidationContext.begin();
		validateTestSet(validation, applicationId, stageId, testRunId, testSetResultId);
		if (pathResultId == null) {
			validation.addViolation(new ValidationViolation("TEST_PATH_RESULT_ID_NULL",
					"Path result id must not be null.", "/pathResultId"));
		}
		validation.rejectIfErrors();
	}

	private void validateTestSet(ValidationContext validation, Long applicationId, Long stageId, Long testRunId,
			Long testSetResultId) {
		if (applicationId == null) {
			validation.addViolation(new ValidationViolation("TEST_PATH_APPLICATION_ID_NULL",
					"Application id must not be null.", "/applicationId"));
		}
		if (stageId == null) {
			validation.addViolation(
					new ValidationViolation("TEST_PATH_STAGE_ID_NULL", "Stage id must not be null.", "/stageId"));
		}
		if (testRunId == null) {
			validation.addViolation(new ValidationViolation("TEST_PATH_TEST_RUN_ID_NULL",
					"Test run id must not be null.", "/testRunId"));
		}
		if (testSetResultId == null) {
			validation.addViolation(new ValidationViolation("TEST_PATH_TEST_SET_RESULT_ID_NULL",
					"Test set result id must not be null.", "/testSetResultId"));
		}
	}

}
