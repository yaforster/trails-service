package io.github.yaforster.trails.adapter.api.rest.run;

import io.github.yaforster.trails.adapter.api.rest.model.TestPlanRunDefinitionDTO;
import io.github.yaforster.trails.adapter.api.rest.validation.ValidationContext;
import io.github.yaforster.trails.core.ValidationViolation;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class TestExecutionControllerValidator {

	public void validateRunTest(TestPlanRunDefinitionDTO dto) {
		ValidationContext validation = ValidationContext.begin();
		if (dto == null) {
			validation.addViolation(new ValidationViolation("TEST_EXECUTION_DEFINITION_NULL",
					"Test execution definition must not be null.", "/"));
		}
		if (isTestPlanIdMissing(dto)) {
			validation.addViolation(new ValidationViolation("TEST_EXECUTION_TEST_PLAN_ID_NULL",
					"Test plan id must not be null.", "/testPlanID"));
		}
		if (areBrowsersToTestMissing(dto)) {
			validation.addViolation(new ValidationViolation("TEST_EXECUTION_BROWSERS_TO_TEST_NULL",
					"Browsers to test must not be null.", "/browsersToTest"));
		}
		validation.rejectIfErrors();
	}

	public void validateStreamTestExecutionEvents(UUID executionId) {
		ValidationContext validation = ValidationContext.begin();
		if (executionId == null) {
			validation.addViolation(new ValidationViolation("TEST_EXECUTION_ID_NULL",
					"Test execution id must not be null.", "/executionId"));
		}
		validation.rejectIfErrors();
	}

	private boolean isTestPlanIdMissing(TestPlanRunDefinitionDTO dto) {
		return dto == null || dto.getTestPlanID() == null;
	}

	private boolean areBrowsersToTestMissing(TestPlanRunDefinitionDTO dto) {
		return dto == null || dto.getBrowsersToTest() == null;
	}

}
