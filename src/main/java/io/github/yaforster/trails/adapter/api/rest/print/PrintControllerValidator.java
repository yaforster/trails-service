package io.github.yaforster.trails.adapter.api.rest.print;

import io.github.yaforster.trails.adapter.api.rest.validation.ValidationContext;
import io.github.yaforster.trails.core.ValidationViolation;
import org.springframework.stereotype.Component;

@Component
public class PrintControllerValidator {

	public void validatePrintPathResultInTestSet(Long applicationId, Long stageId, Long testRunId, Long testSetResultId,
			Long pathResultId) {
		ValidationContext validation = ValidationContext.begin();
		validateTestSet(validation, applicationId, stageId, testRunId, testSetResultId);
		if (pathResultId == null) {
			validation.addViolation(new ValidationViolation("PRINT_PATH_RESULT_ID_NULL",
					"Path result id must not be null.", "/pathResultId"));
		}
		validation.rejectIfErrors();
	}

	public void validatePrintTestSetResultInTestRun(Long applicationId, Long stageId, Long testRunId,
			Long testSetResultId) {
		ValidationContext validation = ValidationContext.begin();
		validateTestSet(validation, applicationId, stageId, testRunId, testSetResultId);
		validation.rejectIfErrors();
	}

	public void validatePrintTestRun(Long applicationId, Long stageId, Long testRunId) {
		ValidationContext validation = ValidationContext.begin();
		validateTestRun(validation, applicationId, stageId, testRunId);
		validation.rejectIfErrors();
	}

	private void validateTestSet(ValidationContext validation, Long applicationId, Long stageId, Long testRunId,
			Long testSetResultId) {
		validateTestRun(validation, applicationId, stageId, testRunId);
		if (testSetResultId == null) {
			validation.addViolation(new ValidationViolation("PRINT_TEST_SET_RESULT_ID_NULL",
					"Test set result id must not be null.", "/testSetResultId"));
		}
	}

	private void validateTestRun(ValidationContext validation, Long applicationId, Long stageId, Long testRunId) {
		if (applicationId == null) {
			validation.addViolation(new ValidationViolation("PRINT_APPLICATION_ID_NULL",
					"Application id must not be null.", "/applicationId"));
		}
		if (stageId == null) {
			validation
				.addViolation(new ValidationViolation("PRINT_STAGE_ID_NULL", "Stage id must not be null.", "/stageId"));
		}
		if (testRunId == null) {
			validation.addViolation(
					new ValidationViolation("PRINT_TEST_RUN_ID_NULL", "Test run id must not be null.", "/testRunId"));
		}
	}

}
