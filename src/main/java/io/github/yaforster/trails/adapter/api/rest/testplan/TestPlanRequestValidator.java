package io.github.yaforster.trails.adapter.api.rest.testplan;

import io.github.yaforster.trails.adapter.api.rest.model.TestPlanDefinitionDTO;
import io.github.yaforster.trails.adapter.api.rest.validation.ValidationContext;
import io.github.yaforster.trails.adapter.api.rest.validation.PaginationValidator;
import io.github.yaforster.trails.core.ValidationViolation;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@AllArgsConstructor
public class TestPlanRequestValidator {

	private final CreateTestPlanRequestValidator createTestPlanRequestValidator;

	public void validateCreateNewTestPlan(Long applicationId, Long stageId, TestPlanDefinitionDTO dto) {
		createTestPlanRequestValidator.validate(new CreateTestPlanRequest(applicationId, stageId, dto));
	}

	public void validateDeleteTestPlan(Long applicationId, Long stageId, Long testPlanId) {
		ValidationContext validation = ValidationContext.begin();
		validateApplicationStageAndTestPlan(validation, applicationId, stageId, testPlanId);
		validation.rejectIfErrors();
	}

	public void validateGetTestPlan(Long applicationId, Long stageId, Long testPlanId) {
		ValidationContext validation = ValidationContext.begin();
		validateApplicationStageAndTestPlan(validation, applicationId, stageId, testPlanId);
		validation.rejectIfErrors();
	}

	public void validateListTestPlans(Long applicationId, Long stageId, Integer page, Integer size) {
		ValidationContext validation = ValidationContext.begin();
		validateApplicationAndStage(validation, applicationId, stageId);
		PaginationValidator.validate(validation, page, size, "TEST_PLAN");
		validation.rejectIfErrors();
	}

	public void validatePromoteTestPlan(Long applicationId, Long sourceStageId, Long testPlanId, Long targetStageId) {
		ValidationContext validation = ValidationContext.begin();
		validateApplicationStageAndTestPlan(validation, applicationId, sourceStageId, testPlanId);
		if (targetStageId == null) {
			validation.addViolation(new ValidationViolation("TEST_PLAN_TARGET_STAGE_ID_NULL",
					"Target stage id must not be null.", "/targetStageId"));
		}
		if (sourceStageId != null && sourceStageId.equals(targetStageId)) {
			validation.addViolation(new ValidationViolation("TEST_PLAN_PROMOTE_TARGET_STAGE_EQUALS_SOURCE_STAGE",
					"Target stage must be different from source stage.", "/targetStageId"));
		}
		validation.rejectIfErrors();
	}

	private void validateApplicationStageAndTestPlan(ValidationContext validation, Long applicationId, Long stageId,
			Long testPlanId) {
		validateApplicationAndStage(validation, applicationId, stageId);
		if (testPlanId == null) {
			validation.addViolation(
					new ValidationViolation("TEST_PLAN_ID_NULL", "Test plan id must not be null.", "/testPlanId"));
		}
	}

	private void validateApplicationAndStage(ValidationContext validation, Long applicationId, Long stageId) {
		if (applicationId == null) {
			validation.addViolation(new ValidationViolation("TEST_PLAN_APPLICATION_ID_NULL",
					"Application id must not be null.", "/applicationId"));
		}
		if (stageId == null) {
			validation.addViolation(
					new ValidationViolation("TEST_PLAN_STAGE_ID_NULL", "Stage id must not be null.", "/stageId"));
		}
	}

}
