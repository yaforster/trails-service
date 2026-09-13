package io.github.yaforster.trails.adapter.api.rest.run;

import io.github.yaforster.trails.adapter.api.APIRequestValidationException;
import io.github.yaforster.trails.adapter.api.rest.model.BrowserDTO;
import io.github.yaforster.trails.adapter.api.rest.model.TestPlanRunDefinitionDTO;
import io.github.yaforster.trails.core.ValidationViolation;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.junit.jupiter.api.Assertions.assertThrows;

class TestExecutionControllerValidatorTest {

	private static TestPlanRunDefinitionDTO validDefinition() {
		return new TestPlanRunDefinitionDTO().testPlanID(1L).browsersToTest(List.of(BrowserDTO.CHROME));
	}

	private static List<ValidationViolation> validateAndGetErrors(ValidationCall validationCall) {
		APIRequestValidationException exception = assertThrows(APIRequestValidationException.class,
				validationCall::validate);
		return exception.getValidationViolations();
	}

	@Test
	void validateRunTest_shouldThrowAPIRequestValidationExceptionWithAllErrors_whenDtoIsNull() {
		TestExecutionControllerValidator validator = new TestExecutionControllerValidator();

		List<ValidationViolation> validationViolations = validateAndGetErrors(() -> validator.validateRunTest(null));

		assertThat(validationViolations).hasSize(3);
	}

	@Test
	void validateRunTest_shouldThrowAPIRequestValidationException_whenTestPlanIdIsNull() {
		TestExecutionControllerValidator validator = new TestExecutionControllerValidator();
		TestPlanRunDefinitionDTO dto = validDefinition().testPlanID(null);

		List<ValidationViolation> validationViolations = validateAndGetErrors(() -> validator.validateRunTest(dto));

		assertThat(validationViolations.getFirst()).isEqualTo(new ValidationViolation(
				"TEST_EXECUTION_TEST_PLAN_ID_NULL", "Test plan id must not be null.", "/testPlanID"));
	}

	@Test
	void validateRunTest_shouldThrowAPIRequestValidationException_whenBrowsersToTestIsNull() {
		TestExecutionControllerValidator validator = new TestExecutionControllerValidator();
		TestPlanRunDefinitionDTO dto = validDefinition().browsersToTest(null);

		List<ValidationViolation> validationViolations = validateAndGetErrors(() -> validator.validateRunTest(dto));

		assertThat(validationViolations.getFirst()).isEqualTo(new ValidationViolation(
				"TEST_EXECUTION_BROWSERS_TO_TEST_NULL", "Browsers to test must not be null.", "/browsersToTest"));
	}

	@Test
	void validateStreamTestExecutionEvents_shouldThrowAPIRequestValidationException_whenExecutionIdIsNull() {
		TestExecutionControllerValidator validator = new TestExecutionControllerValidator();

		List<ValidationViolation> validationViolations = validateAndGetErrors(
				() -> validator.validateStreamTestExecutionEvents(null));

		assertThat(validationViolations.getFirst()).isEqualTo(new ValidationViolation("TEST_EXECUTION_ID_NULL",
				"Test execution id must not be null.", "/executionId"));
	}

	@Test
	void validateRunTest_shouldAcceptDefinition_whenAllArgumentsArePresent() {
		TestExecutionControllerValidator validator = new TestExecutionControllerValidator();

		assertThatCode(() -> validator.validateRunTest(validDefinition())).doesNotThrowAnyException();
	}

	@Test
	void validateStreamTestExecutionEvents_shouldAcceptExecutionId_whenPresent() {
		TestExecutionControllerValidator validator = new TestExecutionControllerValidator();

		assertThatCode(() -> validator.validateStreamTestExecutionEvents(UUID.randomUUID())).doesNotThrowAnyException();
	}

	@FunctionalInterface
	private interface ValidationCall {

		void validate();

	}

}
