package io.github.yaforster.trails.adapter.api.rest.testrun;

import io.github.yaforster.trails.adapter.api.rest.validation.ValidationContext;
import io.github.yaforster.trails.adapter.api.rest.validation.PaginationValidator;
import io.github.yaforster.trails.core.ValidationViolation;
import org.springframework.stereotype.Component;

@Component
public class TestRunControllerValidator {

	public void validateGetTestRun(Long testRunId, Long applicationId, Long stageId) {
		ValidationContext validation = ValidationContext.begin();
		validateApplicationAndStage(validation, applicationId, stageId);
		if (testRunId == null) {
			validation.addViolation(
					new ValidationViolation("TEST_RUN_ID_NULL", "Test run id must not be null.", "/testRunId"));
		}
		validation.rejectIfErrors();
	}

	public void validateListTestRuns(Long applicationId, Long stageId, Integer page, Integer size) {
		ValidationContext validation = ValidationContext.begin();
		validateApplicationAndStage(validation, applicationId, stageId);
		PaginationValidator.validate(validation, page, size, "TEST_RUN");
		validation.rejectIfErrors();
	}

	public void validateGetTestRunStatistics(Long applicationId, Long stageId, Long testPlanId) {
		ValidationContext validation = ValidationContext.begin();
		validateApplicationAndStage(validation, applicationId, stageId);
		if (testPlanId == null) {
			validation.addViolation(new ValidationViolation("TEST_RUN_TEST_PLAN_ID_NULL",
					"Test plan id must not be null.", "/testPlanId"));
		}
		validation.rejectIfErrors();
	}

	public void validateGetTestRunHistory(Long applicationId, Long stageId, Long testPlanId, Integer page,
			Integer size) {
		ValidationContext validation = ValidationContext.begin();
		validateApplicationAndStage(validation, applicationId, stageId);
		if (testPlanId == null) {
			validation.addViolation(new ValidationViolation("TEST_RUN_TEST_PLAN_ID_NULL",
					"Test plan id must not be null.", "/testPlanId"));
		}
		PaginationValidator.validate(validation, page, size, "TEST_RUN_HISTORY");
		validation.rejectIfErrors();
	}

	private void validateApplicationAndStage(ValidationContext validation, Long applicationId, Long stageId) {
		if (applicationId == null) {
			validation.addViolation(new ValidationViolation("TEST_RUN_APPLICATION_ID_NULL",
					"Application id must not be null.", "/applicationId"));
		}
		if (stageId == null) {
			validation.addViolation(
					new ValidationViolation("TEST_RUN_STAGE_ID_NULL", "Stage id must not be null.", "/stageId"));
		}
	}

}
