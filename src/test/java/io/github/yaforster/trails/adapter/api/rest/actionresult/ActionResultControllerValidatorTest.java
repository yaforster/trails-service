package io.github.yaforster.trails.adapter.api.rest.actionresult;

import io.github.yaforster.trails.adapter.api.APIRequestValidationException;
import io.github.yaforster.trails.core.ValidationViolation;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ActionResultControllerValidatorTest {

	private static List<ValidationViolation> validateAndGetErrors(ActionResultControllerValidator validator,
			Long applicationId, Long stageId, Long testRunId, Long testSetResultId, Long pathResultId) {
		APIRequestValidationException exception = assertThrows(APIRequestValidationException.class,
				() -> validator.validate(applicationId, stageId, testRunId, testSetResultId, pathResultId));
		return exception.getValidationViolations();
	}

	@Test
	void validate_shouldThrowAPIRequestValidationExceptionWithAllErrors_whenAllArgumentsAreNull() {
		ActionResultControllerValidator validator = new ActionResultControllerValidator();

		List<ValidationViolation> validationViolations = validateAndGetErrors(validator, null, null, null, null, null);

		assertThat(validationViolations).hasSize(5);
	}

	@Test
	void validate_shouldThrowAPIRequestValidationException_whenApplicationIdIsNull() {
		ActionResultControllerValidator validator = new ActionResultControllerValidator();

		List<ValidationViolation> validationViolations = validateAndGetErrors(validator, null, 2L, 3L, 4L, 5L);

		assertThat(validationViolations.getFirst()).isEqualTo(new ValidationViolation(
				"ACTION_RESULT_APPLICATION_ID_NULL", "Application id must not be null.", "/applicationId"));
	}

	@Test
	void validate_shouldThrowAPIRequestValidationException_whenStageIdIsNull() {
		ActionResultControllerValidator validator = new ActionResultControllerValidator();

		List<ValidationViolation> validationViolations = validateAndGetErrors(validator, 1L, null, 3L, 4L, 5L);

		assertThat(validationViolations.getFirst()).isEqualTo(
				new ValidationViolation("ACTION_RESULT_STAGE_ID_NULL", "Stage id must not be null.", "/stageId"));
	}

	@Test
	void validate_shouldThrowAPIRequestValidationException_whenTestRunIdIsNull() {
		ActionResultControllerValidator validator = new ActionResultControllerValidator();

		List<ValidationViolation> validationViolations = validateAndGetErrors(validator, 1L, 2L, null, 4L, 5L);

		assertThat(validationViolations.getFirst()).isEqualTo(new ValidationViolation("ACTION_RESULT_TEST_RUN_ID_NULL",
				"Test run id must not be null.", "/testRunId"));
	}

	@Test
	void validate_shouldThrowAPIRequestValidationException_whenTestSetResultIdIsNull() {
		ActionResultControllerValidator validator = new ActionResultControllerValidator();

		List<ValidationViolation> validationViolations = validateAndGetErrors(validator, 1L, 2L, 3L, null, 5L);

		assertThat(validationViolations.getFirst()).isEqualTo(new ValidationViolation(
				"ACTION_RESULT_TEST_SET_RESULT_ID_NULL", "Test set result id must not be null.", "/testSetResultId"));
	}

	@Test
	void validate_shouldThrowAPIRequestValidationException_whenPathResultIdIsNull() {
		ActionResultControllerValidator validator = new ActionResultControllerValidator();

		List<ValidationViolation> validationViolations = validateAndGetErrors(validator, 1L, 2L, 3L, 4L, null);

		assertThat(validationViolations.getFirst()).isEqualTo(new ValidationViolation(
				"ACTION_RESULT_PATH_RESULT_ID_NULL", "Path result id must not be null.", "/pathResultId"));
	}

	@Test
	void validate_shouldAcceptArguments_whenAllArgumentsArePresent() {
		ActionResultControllerValidator validator = new ActionResultControllerValidator();

		assertThatCode(() -> validator.validate(1L, 2L, 3L, 4L, 5L)).doesNotThrowAnyException();
	}

}
