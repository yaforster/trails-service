package io.github.yaforster.trails.adapter.api.rest.testplan;

import io.github.yaforster.trails.adapter.api.rest.validation.RequestValidator;
import io.github.yaforster.trails.adapter.api.rest.validation.ValidationContext;
import io.github.yaforster.trails.app.services.TestPlanDatabaseService;
import io.github.yaforster.trails.core.ValidationViolation;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@AllArgsConstructor
public class CreateTestPlanRequestValidator implements RequestValidator<CreateTestPlanRequest> {

	private final TestPlanDatabaseService testPlanDatabaseService;

	private final TestPlanActionGraphRequestValidator actionGraphValidator;

	@Override
	public void validate(CreateTestPlanRequest request) {
		ValidationContext validation = ValidationContext.begin();
		validateApplicationAndStage(validation, request.applicationId(), request.stageId());
		validateLabel(validation, request);
		actionGraphValidator.validate(request.definition(), validation);
		validateUniqueLabel(validation, request);
		validation.rejectIfErrors();
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

	private void validateLabel(ValidationContext validation, CreateTestPlanRequest request) {
		if (request.definition() == null || request.definition().getLabel() == null
				|| request.definition().getLabel().isBlank()) {
			validation.addViolation(
					new ValidationViolation("TEST_PLAN_LABEL_EMPTY", "Test plan label must not be empty.", "/label"));
		}
	}

	private void validateUniqueLabel(ValidationContext validation, CreateTestPlanRequest request) {
		if (request.applicationId() != null && request.stageId() != null && request.definition() != null
				&& request.definition().getLabel() != null && !request.definition().getLabel().isBlank()
				&& testPlanDatabaseService.existsByLabel(new TestPlanDatabaseService.TestPlanLabel(
						request.applicationId(), request.stageId(), request.definition().getLabel()))) {
			validation.addViolation(new ValidationViolation("TEST_PLAN_LABEL_ALREADY_EXISTS",
					"Test plan label must be unique within the stage.", "/label"));
		}
	}

}
