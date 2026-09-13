package io.github.yaforster.trails.adapter.api.rest.print;

import io.github.yaforster.trails.adapter.api.APIRequestValidationException;
import io.github.yaforster.trails.core.ValidationViolation;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.junit.jupiter.api.Assertions.assertThrows;

class PrintControllerValidatorTest {

	private static List<ValidationViolation> validateAndGetErrors(ValidationCall validationCall) {
		APIRequestValidationException exception = assertThrows(APIRequestValidationException.class,
				validationCall::validate);
		return exception.getValidationViolations();
	}

	@Test
	void validatePrintPathResultInTestSet_shouldThrowAPIRequestValidationExceptionWithAllErrors_whenAllArgumentsAreNull() {
		PrintControllerValidator validator = new PrintControllerValidator();

		List<ValidationViolation> validationViolations = validateAndGetErrors(
				() -> validator.validatePrintPathResultInTestSet(null, null, null, null, null));

		assertThat(validationViolations).hasSize(5);
	}

	@Test
	void validatePrintPathResultInTestSet_shouldThrowAPIRequestValidationException_whenApplicationIdIsNull() {
		PrintControllerValidator validator = new PrintControllerValidator();

		List<ValidationViolation> validationViolations = validateAndGetErrors(
				() -> validator.validatePrintPathResultInTestSet(null, 2L, 3L, 4L, 5L));

		assertThat(validationViolations.getFirst()).isEqualTo(new ValidationViolation("PRINT_APPLICATION_ID_NULL",
				"Application id must not be null.", "/applicationId"));
	}

	@Test
	void validatePrintPathResultInTestSet_shouldThrowAPIRequestValidationException_whenStageIdIsNull() {
		PrintControllerValidator validator = new PrintControllerValidator();

		List<ValidationViolation> validationViolations = validateAndGetErrors(
				() -> validator.validatePrintPathResultInTestSet(1L, null, 3L, 4L, 5L));

		assertThat(validationViolations.getFirst())
			.isEqualTo(new ValidationViolation("PRINT_STAGE_ID_NULL", "Stage id must not be null.", "/stageId"));
	}

	@Test
	void validatePrintPathResultInTestSet_shouldThrowAPIRequestValidationException_whenTestRunIdIsNull() {
		PrintControllerValidator validator = new PrintControllerValidator();

		List<ValidationViolation> validationViolations = validateAndGetErrors(
				() -> validator.validatePrintPathResultInTestSet(1L, 2L, null, 4L, 5L));

		assertThat(validationViolations.getFirst()).isEqualTo(
				new ValidationViolation("PRINT_TEST_RUN_ID_NULL", "Test run id must not be null.", "/testRunId"));
	}

	@Test
	void validatePrintTestSetResultInTestRun_shouldThrowAPIRequestValidationException_whenTestSetResultIdIsNull() {
		PrintControllerValidator validator = new PrintControllerValidator();

		List<ValidationViolation> validationViolations = validateAndGetErrors(
				() -> validator.validatePrintTestSetResultInTestRun(1L, 2L, 3L, null));

		assertThat(validationViolations.getFirst()).isEqualTo(new ValidationViolation("PRINT_TEST_SET_RESULT_ID_NULL",
				"Test set result id must not be null.", "/testSetResultId"));
	}

	@Test
	void validatePrintPathResultInTestSet_shouldThrowAPIRequestValidationException_whenPathResultIdIsNull() {
		PrintControllerValidator validator = new PrintControllerValidator();

		List<ValidationViolation> validationViolations = validateAndGetErrors(
				() -> validator.validatePrintPathResultInTestSet(1L, 2L, 3L, 4L, null));

		assertThat(validationViolations.getFirst()).isEqualTo(new ValidationViolation("PRINT_PATH_RESULT_ID_NULL",
				"Path result id must not be null.", "/pathResultId"));
	}

	@Test
	void validatePrintPathResultInTestSet_shouldAcceptArguments_whenAllArgumentsArePresent() {
		PrintControllerValidator validator = new PrintControllerValidator();

		assertThatCode(() -> validator.validatePrintPathResultInTestSet(1L, 2L, 3L, 4L, 5L)).doesNotThrowAnyException();
	}

	@Test
	void validatePrintTestSetResultInTestRun_shouldAcceptArguments_whenAllArgumentsArePresent() {
		PrintControllerValidator validator = new PrintControllerValidator();

		assertThatCode(() -> validator.validatePrintTestSetResultInTestRun(1L, 2L, 3L, 4L)).doesNotThrowAnyException();
	}

	@Test
	void validatePrintTestRun_shouldAcceptArguments_whenAllArgumentsArePresent() {
		PrintControllerValidator validator = new PrintControllerValidator();

		assertThatCode(() -> validator.validatePrintTestRun(1L, 2L, 3L)).doesNotThrowAnyException();
	}

	@FunctionalInterface
	private interface ValidationCall {

		void validate();

	}

}
