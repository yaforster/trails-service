package io.github.yaforster.trails.adapter.api.rest.action;

import io.github.yaforster.trails.adapter.api.APIRequestValidationException;
import io.github.yaforster.trails.core.ValidationViolation;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ActionControllerValidatorTest {

	private static List<ValidationViolation> validateAndGetErrors(ActionControllerValidator validator,
			Long applicationId, Long stageId, Long testPlanId, Integer page, Integer size) {
		APIRequestValidationException exception = assertThrows(APIRequestValidationException.class,
				() -> validator.validate(applicationId, stageId, testPlanId, page, size));
		return exception.getValidationViolations();
	}

	@Test
	void validate_shouldThrowAPIRequestValidationExceptionWithAllErrors_whenAllArgumentsAreNull() {
		ActionControllerValidator validator = new ActionControllerValidator();

		List<ValidationViolation> validationViolations = validateAndGetErrors(validator, null, null, null, null, null);

		assertThat(validationViolations).hasSize(5);
	}

	@Test
	void validate_shouldThrowAPIRequestValidationException_whenApplicationIdIsNull() {
		ActionControllerValidator validator = new ActionControllerValidator();

		List<ValidationViolation> validationViolations = validateAndGetErrors(validator, null, 2L, 3L, 0, 20);

		assertThat(validationViolations.getFirst()).isEqualTo(new ValidationViolation("ACTION_APPLICATION_ID_NULL",
				"Application id must not be null.", "/applicationId"));
	}

	@Test
	void validate_shouldThrowAPIRequestValidationException_whenStageIdIsNull() {
		ActionControllerValidator validator = new ActionControllerValidator();

		List<ValidationViolation> validationViolations = validateAndGetErrors(validator, 1L, null, 3L, 0, 20);

		assertThat(validationViolations.getFirst())
			.isEqualTo(new ValidationViolation("ACTION_STAGE_ID_NULL", "Stage id must not be null.", "/stageId"));
	}

	@Test
	void validate_shouldThrowAPIRequestValidationException_whenTestPlanIdIsNull() {
		ActionControllerValidator validator = new ActionControllerValidator();

		List<ValidationViolation> validationViolations = validateAndGetErrors(validator, 1L, 2L, null, 0, 20);

		assertThat(validationViolations.getFirst()).isEqualTo(
				new ValidationViolation("ACTION_TEST_PLAN_ID_NULL", "Test plan id must not be null.", "/testPlanId"));
	}

	@Test
	void validate_shouldThrowAPIRequestValidationException_whenPageIsNull() {
		ActionControllerValidator validator = new ActionControllerValidator();

		List<ValidationViolation> validationViolations = validateAndGetErrors(validator, 1L, 2L, 3L, null, 20);

		assertThat(validationViolations.getFirst())
			.isEqualTo(new ValidationViolation("ACTION_PAGE_NULL", "Page must not be null.", "/page"));
	}

	@Test
	void validate_shouldThrowAPIRequestValidationException_whenSizeIsNull() {
		ActionControllerValidator validator = new ActionControllerValidator();

		List<ValidationViolation> validationViolations = validateAndGetErrors(validator, 1L, 2L, 3L, 0, null);

		assertThat(validationViolations.getFirst())
			.isEqualTo(new ValidationViolation("ACTION_SIZE_NULL", "Size must not be null.", "/size"));
	}

	@Test
	void validate_shouldAcceptArguments_whenAllArgumentsArePresent() {
		ActionControllerValidator validator = new ActionControllerValidator();

		assertThatCode(() -> validator.validate(1L, 2L, 3L, 0, 20)).doesNotThrowAnyException();
	}

}
