package io.github.yaforster.trails.adapter.api.rest.testpath;

import io.github.yaforster.trails.adapter.api.APIRequestValidationException;
import io.github.yaforster.trails.core.ValidationViolation;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.junit.jupiter.api.Assertions.assertThrows;

class TestPathControllerValidatorTest {

	private static List<ValidationViolation> validateAndGetErrors(ValidationCall validationCall) {
		APIRequestValidationException exception = assertThrows(APIRequestValidationException.class,
				validationCall::validate);
		return exception.getValidationViolations();
	}

	@Test
	void validateListPathResultsInTestSet_shouldThrowAPIRequestValidationExceptionWithAllErrors_whenAllArgumentsAreNull() {
		TestPathControllerValidator validator = new TestPathControllerValidator();

		List<ValidationViolation> validationViolations = validateAndGetErrors(
				() -> validator.validateListPathResultsInTestSet(null, null, null, null, null, null));

		assertThat(validationViolations).hasSize(6);
	}

	@Test
	void validateListPathResultsInTestSet_shouldThrowAPIRequestValidationException_whenApplicationIdIsNull() {
		TestPathControllerValidator validator = new TestPathControllerValidator();

		List<ValidationViolation> validationViolations = validateAndGetErrors(
				() -> validator.validateListPathResultsInTestSet(null, 2L, 3L, 4L, 0, 20));

		assertThat(validationViolations.getFirst()).isEqualTo(new ValidationViolation("TEST_PATH_APPLICATION_ID_NULL",
				"Application id must not be null.", "/applicationId"));
	}

	@Test
	void validateListPathResultsInTestSet_shouldThrowAPIRequestValidationException_whenStageIdIsNull() {
		TestPathControllerValidator validator = new TestPathControllerValidator();

		List<ValidationViolation> validationViolations = validateAndGetErrors(
				() -> validator.validateListPathResultsInTestSet(1L, null, 3L, 4L, 0, 20));

		assertThat(validationViolations.getFirst())
			.isEqualTo(new ValidationViolation("TEST_PATH_STAGE_ID_NULL", "Stage id must not be null.", "/stageId"));
	}

	@Test
	void validateListPathResultsInTestSet_shouldThrowAPIRequestValidationException_whenTestRunIdIsNull() {
		TestPathControllerValidator validator = new TestPathControllerValidator();

		List<ValidationViolation> validationViolations = validateAndGetErrors(
				() -> validator.validateListPathResultsInTestSet(1L, 2L, null, 4L, 0, 20));

		assertThat(validationViolations.getFirst()).isEqualTo(
				new ValidationViolation("TEST_PATH_TEST_RUN_ID_NULL", "Test run id must not be null.", "/testRunId"));
	}

	@Test
	void validateListPathResultsInTestSet_shouldThrowAPIRequestValidationException_whenTestSetResultIdIsNull() {
		TestPathControllerValidator validator = new TestPathControllerValidator();

		List<ValidationViolation> validationViolations = validateAndGetErrors(
				() -> validator.validateListPathResultsInTestSet(1L, 2L, 3L, null, 0, 20));

		assertThat(validationViolations.getFirst()).isEqualTo(new ValidationViolation(
				"TEST_PATH_TEST_SET_RESULT_ID_NULL", "Test set result id must not be null.", "/testSetResultId"));
	}

	@Test
	void validateListPathResultsInTestSet_shouldThrowAPIRequestValidationException_whenPageIsNull() {
		TestPathControllerValidator validator = new TestPathControllerValidator();

		List<ValidationViolation> validationViolations = validateAndGetErrors(
				() -> validator.validateListPathResultsInTestSet(1L, 2L, 3L, 4L, null, 20));

		assertThat(validationViolations.getFirst())
			.isEqualTo(new ValidationViolation("TEST_PATH_PAGE_NULL", "Page must not be null.", "/page"));
	}

	@Test
	void validateListPathResultsInTestSet_shouldThrowAPIRequestValidationException_whenSizeIsNull() {
		TestPathControllerValidator validator = new TestPathControllerValidator();

		List<ValidationViolation> validationViolations = validateAndGetErrors(
				() -> validator.validateListPathResultsInTestSet(1L, 2L, 3L, 4L, 0, null));

		assertThat(validationViolations.getFirst())
			.isEqualTo(new ValidationViolation("TEST_PATH_SIZE_NULL", "Size must not be null.", "/size"));
	}

	@Test
	void validateGetPathResultInTestSet_shouldThrowAPIRequestValidationException_whenPathResultIdIsNull() {
		TestPathControllerValidator validator = new TestPathControllerValidator();

		List<ValidationViolation> validationViolations = validateAndGetErrors(
				() -> validator.validateGetPathResultInTestSet(1L, 2L, 3L, 4L, null));

		assertThat(validationViolations.getFirst()).isEqualTo(new ValidationViolation("TEST_PATH_RESULT_ID_NULL",
				"Path result id must not be null.", "/pathResultId"));
	}

	@Test
	void validateListPathResultsInTestSet_shouldAcceptArguments_whenAllArgumentsArePresent() {
		TestPathControllerValidator validator = new TestPathControllerValidator();

		assertThatCode(() -> validator.validateListPathResultsInTestSet(1L, 2L, 3L, 4L, 0, 20))
			.doesNotThrowAnyException();
	}

	@Test
	void validateGetPathResultInTestSet_shouldAcceptArguments_whenAllArgumentsArePresent() {
		TestPathControllerValidator validator = new TestPathControllerValidator();

		assertThatCode(() -> validator.validateGetPathResultInTestSet(1L, 2L, 3L, 4L, 5L)).doesNotThrowAnyException();
	}

	@FunctionalInterface
	private interface ValidationCall {

		void validate();

	}

}
