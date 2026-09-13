package io.github.yaforster.trails.adapter.api.rest.testset;

import io.github.yaforster.trails.adapter.api.rest.validation.ValidationContext;
import io.github.yaforster.trails.adapter.api.rest.validation.PaginationValidator;
import io.github.yaforster.trails.core.ValidationViolation;
import org.springframework.stereotype.Component;

@Component
public class TestSetControllerValidator {

	public void validateListBrowserResults(Long testRunId, Long applicationId, Long stageId, Integer page,
			Integer size) {
		ValidationContext validation = ValidationContext.begin();
		validateTestRun(validation, testRunId, applicationId, stageId);
		PaginationValidator.validate(validation, page, size, "TEST_SET");
		validation.rejectIfErrors();
	}

	public void validateGetBrowserResultInTestRun(Long testRunId, Long applicationId, Long stageId,
			Long testSetResultId) {
		ValidationContext validation = ValidationContext.begin();
		validateTestRun(validation, testRunId, applicationId, stageId);
		if (testSetResultId == null) {
			validation.addViolation(new ValidationViolation("TEST_SET_RESULT_ID_NULL",
					"Test set result id must not be null.", "/testSetResultId"));
		}
		validation.rejectIfErrors();
	}

	private void validateTestRun(ValidationContext validation, Long testRunId, Long applicationId, Long stageId) {
		if (testRunId == null) {
			validation.addViolation(new ValidationViolation("TEST_SET_TEST_RUN_ID_NULL",
					"Test run id must not be null.", "/testRunId"));
		}
		if (applicationId == null) {
			validation.addViolation(new ValidationViolation("TEST_SET_APPLICATION_ID_NULL",
					"Application id must not be null.", "/applicationId"));
		}
		if (stageId == null) {
			validation.addViolation(
					new ValidationViolation("TEST_SET_STAGE_ID_NULL", "Stage id must not be null.", "/stageId"));
		}
	}

}
