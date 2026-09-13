package io.github.yaforster.trails.adapter.api.rest.testset;

import io.github.yaforster.trails.adapter.api.APIRequestValidationException;
import io.github.yaforster.trails.core.ValidationViolation;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.junit.jupiter.api.Assertions.assertThrows;

class TestSetControllerValidatorTest {

	private static List<ValidationViolation> validateAndGetErrors(ValidationCall validationCall) {
		APIRequestValidationException exception = assertThrows(APIRequestValidationException.class,
				validationCall::validate);
		return exception.getValidationViolations();
	}

	@Test
	void validateListBrowserResults_shouldThrowAPIRequestValidationExceptionWithAllErrors_whenAllArgumentsAreNull() {
		TestSetControllerValidator validator = new TestSetControllerValidator();

		List<ValidationViolation> validationViolations = validateAndGetErrors(
				() -> validator.validateListBrowserResults(null, null, null, null, null));

		assertThat(validationViolations).hasSize(5);
	}

	@Test
	void validateGetBrowserResultInTestRun_shouldThrowAPIRequestValidationExceptionWithAllErrors_whenAllArgumentsAreNull() {
		TestSetControllerValidator validator = new TestSetControllerValidator();

		List<ValidationViolation> validationViolations = validateAndGetErrors(
				() -> validator.validateGetBrowserResultInTestRun(null, null, null, null));

		assertThat(validationViolations).hasSize(4);
	}

	@Test
	void validateGetBrowserResultInTestRun_shouldThrowAPIRequestValidationException_whenTestRunIdIsNull() {
		TestSetControllerValidator validator = new TestSetControllerValidator();

		List<ValidationViolation> validationViolations = validateAndGetErrors(
				() -> validator.validateGetBrowserResultInTestRun(null, 1L, 2L, 3L));

		assertThat(validationViolations.getFirst()).isEqualTo(
				new ValidationViolation("TEST_SET_TEST_RUN_ID_NULL", "Test run id must not be null.", "/testRunId"));
	}

	@Test
	void validateGetBrowserResultInTestRun_shouldThrowAPIRequestValidationException_whenApplicationIdIsNull() {
		TestSetControllerValidator validator = new TestSetControllerValidator();

		List<ValidationViolation> validationViolations = validateAndGetErrors(
				() -> validator.validateGetBrowserResultInTestRun(4L, null, 2L, 3L));

		assertThat(validationViolations.getFirst()).isEqualTo(new ValidationViolation("TEST_SET_APPLICATION_ID_NULL",
				"Application id must not be null.", "/applicationId"));
	}

	@Test
	void validateGetBrowserResultInTestRun_shouldThrowAPIRequestValidationException_whenStageIdIsNull() {
		TestSetControllerValidator validator = new TestSetControllerValidator();

		List<ValidationViolation> validationViolations = validateAndGetErrors(
				() -> validator.validateGetBrowserResultInTestRun(4L, 1L, null, 3L));

		assertThat(validationViolations.getFirst())
			.isEqualTo(new ValidationViolation("TEST_SET_STAGE_ID_NULL", "Stage id must not be null.", "/stageId"));
	}

	@Test
	void validateGetBrowserResultInTestRun_shouldThrowAPIRequestValidationException_whenTestSetResultIdIsNull() {
		TestSetControllerValidator validator = new TestSetControllerValidator();

		List<ValidationViolation> validationViolations = validateAndGetErrors(
				() -> validator.validateGetBrowserResultInTestRun(4L, 1L, 2L, null));

		assertThat(validationViolations.getFirst()).isEqualTo(new ValidationViolation("TEST_SET_RESULT_ID_NULL",
				"Test set result id must not be null.", "/testSetResultId"));
	}

	@Test
	void validateListBrowserResults_shouldThrowAPIRequestValidationException_whenPageIsNull() {
		TestSetControllerValidator validator = new TestSetControllerValidator();

		List<ValidationViolation> validationViolations = validateAndGetErrors(
				() -> validator.validateListBrowserResults(4L, 1L, 2L, null, 20));

		assertThat(validationViolations.getFirst())
			.isEqualTo(new ValidationViolation("TEST_SET_PAGE_NULL", "Page must not be null.", "/page"));
	}

	@Test
	void validateListBrowserResults_shouldThrowAPIRequestValidationException_whenSizeIsNull() {
		TestSetControllerValidator validator = new TestSetControllerValidator();

		List<ValidationViolation> validationViolations = validateAndGetErrors(
				() -> validator.validateListBrowserResults(4L, 1L, 2L, 0, null));

		assertThat(validationViolations.getFirst())
			.isEqualTo(new ValidationViolation("TEST_SET_SIZE_NULL", "Size must not be null.", "/size"));
	}

	@Test
	void validateListBrowserResults_shouldAcceptArguments_whenAllArgumentsArePresent() {
		TestSetControllerValidator validator = new TestSetControllerValidator();

		assertThatCode(() -> validator.validateListBrowserResults(4L, 1L, 2L, 0, 20)).doesNotThrowAnyException();
	}

	@Test
	void validateGetBrowserResultInTestRun_shouldAcceptArguments_whenAllArgumentsArePresent() {
		TestSetControllerValidator validator = new TestSetControllerValidator();

		assertThatCode(() -> validator.validateGetBrowserResultInTestRun(4L, 1L, 2L, 3L)).doesNotThrowAnyException();
	}

	@FunctionalInterface
	private interface ValidationCall {

		void validate();

	}

}
