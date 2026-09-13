package io.github.yaforster.trails.adapter.api.rest.action;

import io.github.yaforster.trails.adapter.api.rest.validation.ValidationContext;
import io.github.yaforster.trails.adapter.api.rest.validation.PaginationValidator;
import io.github.yaforster.trails.core.ValidationViolation;
import org.springframework.stereotype.Component;

@Component
public class ActionControllerValidator {

	public void validate(Long applicationId, Long stageId, Long testPlanId, Integer page, Integer size) {
		ValidationContext validation = ValidationContext.begin();
		if (applicationId == null) {
			validation.addViolation(new ValidationViolation("ACTION_APPLICATION_ID_NULL",
					"Application id must not be null.", "/applicationId"));
		}
		if (stageId == null) {
			validation.addViolation(
					new ValidationViolation("ACTION_STAGE_ID_NULL", "Stage id must not be null.", "/stageId"));
		}
		if (testPlanId == null) {
			validation.addViolation(new ValidationViolation("ACTION_TEST_PLAN_ID_NULL",
					"Test plan id must not be null.", "/testPlanId"));
		}
		PaginationValidator.validate(validation, page, size, "ACTION");
		validation.rejectIfErrors();
	}

}
