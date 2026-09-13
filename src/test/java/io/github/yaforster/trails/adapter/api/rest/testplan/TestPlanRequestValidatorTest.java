package io.github.yaforster.trails.adapter.api.rest.testplan;

import io.github.yaforster.trails.adapter.api.APIRequestValidationException;
import io.github.yaforster.trails.adapter.api.rest.model.ActionDefinitionDTO;
import io.github.yaforster.trails.adapter.api.rest.model.TestPlanDefinitionDTO;
import io.github.yaforster.trails.app.services.TestPlanDatabaseService;
import io.github.yaforster.trails.core.ValidationViolation;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class TestPlanRequestValidatorTest {

	private static TestPlanRequestValidator validator() {
		return new TestPlanRequestValidator(new CreateTestPlanRequestValidator(mock(TestPlanDatabaseService.class),
				new TestPlanActionGraphRequestValidator()));
	}

	private static TestPlanDefinitionDTO validDefinition() {
		return new TestPlanDefinitionDTO().label("plan").testSteps(List.of(new ActionDefinitionDTO().referenceID(1L)));
	}

	private static List<ValidationViolation> validateAndGetErrors(ValidationCall validationCall) {
		APIRequestValidationException exception = assertThrows(APIRequestValidationException.class,
				validationCall::validate);
		return exception.getValidationViolations();
	}

	@Test
	void validateCreateNewTestPlan_shouldThrowAPIRequestValidationExceptionWithAllErrors_whenAllArgumentsAreNull() {
		List<ValidationViolation> validationViolations = validateAndGetErrors(
				() -> validator().validateCreateNewTestPlan(null, null, null));

		assertThat(validationViolations).hasSize(4);
	}

	@Test
	void validateCreateNewTestPlan_shouldThrowAPIRequestValidationException_whenApplicationIdIsNull() {
		List<ValidationViolation> validationViolations = validateAndGetErrors(
				() -> validator().validateCreateNewTestPlan(null, 2L, validDefinition()));

		assertThat(validationViolations.getFirst()).isEqualTo(new ValidationViolation("TEST_PLAN_APPLICATION_ID_NULL",
				"Application id must not be null.", "/applicationId"));
	}

	@Test
	void validateCreateNewTestPlan_shouldThrowAPIRequestValidationException_whenStageIdIsNull() {
		List<ValidationViolation> validationViolations = validateAndGetErrors(
				() -> validator().validateCreateNewTestPlan(1L, null, validDefinition()));

		assertThat(validationViolations.getFirst())
			.isEqualTo(new ValidationViolation("TEST_PLAN_STAGE_ID_NULL", "Stage id must not be null.", "/stageId"));
	}

	@Test
	void validateCreateNewTestPlan_shouldThrowAPIRequestValidationException_whenLabelIsBlank() {
		TestPlanDefinitionDTO dto = validDefinition().label("  ");

		List<ValidationViolation> validationViolations = validateAndGetErrors(
				() -> validator().validateCreateNewTestPlan(1L, 2L, dto));

		assertThat(validationViolations.getFirst()).isEqualTo(
				new ValidationViolation("TEST_PLAN_LABEL_EMPTY", "Test plan label must not be empty.", "/label"));
	}

	@Test
	void validateCreateNewTestPlan_shouldThrowAPIRequestValidationException_whenTestStepsIsNull() {
		TestPlanDefinitionDTO dto = validDefinition().testSteps(null);

		List<ValidationViolation> validationViolations = validateAndGetErrors(
				() -> validator().validateCreateNewTestPlan(1L, 2L, dto));

		assertThat(validationViolations.getFirst()).isEqualTo(new ValidationViolation("TEST_PLAN_TEST_STEPS_NULL",
				"Test plan test steps must not be null.", "/testSteps"));
	}

	@Test
	void validateCreateNewTestPlan_shouldThrowAPIRequestValidationException_whenLabelAlreadyExists() {
		TestPlanDatabaseService databaseService = mock(TestPlanDatabaseService.class);
		when(databaseService.existsByLabel(new TestPlanDatabaseService.TestPlanLabel(1L, 2L, "plan"))).thenReturn(true);
		TestPlanRequestValidator validator = new TestPlanRequestValidator(
				new CreateTestPlanRequestValidator(databaseService, new TestPlanActionGraphRequestValidator()));

		List<ValidationViolation> validationViolations = validateAndGetErrors(
				() -> validator.validateCreateNewTestPlan(1L, 2L, validDefinition()));

		assertThat(validationViolations.getFirst()).isEqualTo(new ValidationViolation("TEST_PLAN_LABEL_ALREADY_EXISTS",
				"Test plan label must be unique within the stage.", "/label"));
	}

	@Test
	void validateGetTestPlan_shouldThrowAPIRequestValidationException_whenTestPlanIdIsNull() {
		List<ValidationViolation> validationViolations = validateAndGetErrors(
				() -> validator().validateGetTestPlan(1L, 2L, null));

		assertThat(validationViolations.getFirst())
			.isEqualTo(new ValidationViolation("TEST_PLAN_ID_NULL", "Test plan id must not be null.", "/testPlanId"));
	}

	@Test
	void validateDeleteTestPlan_shouldThrowAPIRequestValidationException_whenTestPlanIdIsNull() {
		List<ValidationViolation> validationViolations = validateAndGetErrors(
				() -> validator().validateDeleteTestPlan(1L, 2L, null));

		assertThat(validationViolations.getFirst())
			.isEqualTo(new ValidationViolation("TEST_PLAN_ID_NULL", "Test plan id must not be null.", "/testPlanId"));
	}

	@Test
	void validateListTestPlans_shouldThrowAPIRequestValidationException_whenPageIsNull() {
		List<ValidationViolation> validationViolations = validateAndGetErrors(
				() -> validator().validateListTestPlans(1L, 2L, null, 20));

		assertThat(validationViolations.getFirst())
			.isEqualTo(new ValidationViolation("TEST_PLAN_PAGE_NULL", "Page must not be null.", "/page"));
	}

	@Test
	void validateListTestPlans_shouldThrowAPIRequestValidationException_whenSizeIsNull() {
		List<ValidationViolation> validationViolations = validateAndGetErrors(
				() -> validator().validateListTestPlans(1L, 2L, 0, null));

		assertThat(validationViolations.getFirst())
			.isEqualTo(new ValidationViolation("TEST_PLAN_SIZE_NULL", "Size must not be null.", "/size"));
	}

	@Test
	void validateCreateNewTestPlan_shouldAcceptArguments_whenAllArgumentsArePresent() {
		assertThatCode(() -> validator().validateCreateNewTestPlan(1L, 2L, validDefinition()))
			.doesNotThrowAnyException();
	}

	@Test
	void validateGetTestPlan_shouldAcceptArguments_whenAllArgumentsArePresent() {
		assertThatCode(() -> validator().validateGetTestPlan(1L, 2L, 3L)).doesNotThrowAnyException();
	}

	@Test
	void validateDeleteTestPlan_shouldAcceptArguments_whenAllArgumentsArePresent() {
		assertThatCode(() -> validator().validateDeleteTestPlan(1L, 2L, 3L)).doesNotThrowAnyException();
	}

	@Test
	void validateListTestPlans_shouldAcceptArguments_whenAllArgumentsArePresent() {
		assertThatCode(() -> validator().validateListTestPlans(1L, 2L, 0, 20)).doesNotThrowAnyException();
	}

	@FunctionalInterface
	private interface ValidationCall {

		void validate();

	}

}
