package io.github.yaforster.trails.adapter.api.rest.actionresult;

import io.github.yaforster.trails.adapter.api.rest.validation.ValidationContext;
import io.github.yaforster.trails.core.ValidationViolation;
import org.springframework.stereotype.Component;

@Component
public class ActionResultControllerValidator {

	public void validate(Long applicationId, Long stageId, Long testRunId, Long testSetResultId, Long pathResultId) {
		ValidationContext validation = ValidationContext.begin();
		if (applicationId == null) {
			validation.addViolation(new ValidationViolation("ACTION_RESULT_APPLICATION_ID_NULL",
					"Application id must not be null.", "/applicationId"));
		}
		if (stageId == null) {
			validation.addViolation(
					new ValidationViolation("ACTION_RESULT_STAGE_ID_NULL", "Stage id must not be null.", "/stageId"));
		}
		if (testRunId == null) {
			validation.addViolation(new ValidationViolation("ACTION_RESULT_TEST_RUN_ID_NULL",
					"Test run id must not be null.", "/testRunId"));
		}
		if (testSetResultId == null) {
			validation.addViolation(new ValidationViolation("ACTION_RESULT_TEST_SET_RESULT_ID_NULL",
					"Test set result id must not be null.", "/testSetResultId"));
		}
		if (pathResultId == null) {
			validation.addViolation(new ValidationViolation("ACTION_RESULT_PATH_RESULT_ID_NULL",
					"Path result id must not be null.", "/pathResultId"));
		}
		validation.rejectIfErrors();
	}

}
