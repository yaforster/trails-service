package io.github.yaforster.trails.adapter.api.rest.testrun;

import io.github.yaforster.trails.adapter.api.APIRequestValidationException;
import io.github.yaforster.trails.core.ValidationViolation;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.junit.jupiter.api.Assertions.assertThrows;

class TestRunControllerValidatorTest {

	private static List<ValidationViolation> validateAndGetErrors(ValidationCall validationCall) {
		APIRequestValidationException exception = assertThrows(APIRequestValidationException.class,
				validationCall::validate);
		return exception.getValidationViolations();
	}

	@Test
	void validateGetTestRun_shouldThrowAPIRequestValidationExceptionWithAllErrors_whenAllArgumentsAreNull() {
		TestRunControllerValidator validator = new TestRunControllerValidator();

		List<ValidationViolation> validationViolations = validateAndGetErrors(
				() -> validator.validateGetTestRun(null, null, null));

		assertThat(validationViolations).hasSize(3);
	}

	@Test
	void validateGetTestRun_shouldThrowAPIRequestValidationException_whenTestRunIdIsNull() {
		TestRunControllerValidator validator = new TestRunControllerValidator();

		List<ValidationViolation> validationViolations = validateAndGetErrors(
				() -> validator.validateGetTestRun(null, 1L, 2L));

		assertThat(validationViolations.getFirst())
			.isEqualTo(new ValidationViolation("TEST_RUN_ID_NULL", "Test run id must not be null.", "/testRunId"));
	}

	@Test
	void validateGetTestRun_shouldThrowAPIRequestValidationException_whenApplicationIdIsNull() {
		TestRunControllerValidator validator = new TestRunControllerValidator();

		List<ValidationViolation> validationViolations = validateAndGetErrors(
				() -> validator.validateGetTestRun(3L, null, 2L));

		assertThat(validationViolations.getFirst()).isEqualTo(new ValidationViolation("TEST_RUN_APPLICATION_ID_NULL",
				"Application id must not be null.", "/applicationId"));
	}

	@Test
	void validateGetTestRun_shouldThrowAPIRequestValidationException_whenStageIdIsNull() {
		TestRunControllerValidator validator = new TestRunControllerValidator();

		List<ValidationViolation> validationViolations = validateAndGetErrors(
				() -> validator.validateGetTestRun(3L, 1L, null));

		assertThat(validationViolations.getFirst())
			.isEqualTo(new ValidationViolation("TEST_RUN_STAGE_ID_NULL", "Stage id must not be null.", "/stageId"));
	}

	@Test
	void validateListTestRuns_shouldThrowAPIRequestValidationException_whenPageIsNull() {
		TestRunControllerValidator validator = new TestRunControllerValidator();

		List<ValidationViolation> validationViolations = validateAndGetErrors(
				() -> validator.validateListTestRuns(1L, 2L, null, 20));

		assertThat(validationViolations.getFirst())
			.isEqualTo(new ValidationViolation("TEST_RUN_PAGE_NULL", "Page must not be null.", "/page"));
	}

	@Test
	void validateListTestRuns_shouldThrowAPIRequestValidationException_whenSizeIsNull() {
		TestRunControllerValidator validator = new TestRunControllerValidator();

		List<ValidationViolation> validationViolations = validateAndGetErrors(
				() -> validator.validateListTestRuns(1L, 2L, 0, null));

		assertThat(validationViolations.getFirst())
			.isEqualTo(new ValidationViolation("TEST_RUN_SIZE_NULL", "Size must not be null.", "/size"));
	}

	@Test
	void validateGetTestRunStatistics_shouldThrowAPIRequestValidationException_whenTestPlanIdIsNull() {
		TestRunControllerValidator validator = new TestRunControllerValidator();

		List<ValidationViolation> validationViolations = validateAndGetErrors(
				() -> validator.validateGetTestRunStatistics(1L, 2L, null));

		assertThat(validationViolations.getFirst()).isEqualTo(
				new ValidationViolation("TEST_RUN_TEST_PLAN_ID_NULL", "Test plan id must not be null.", "/testPlanId"));
	}

	@Test
	void validateGetTestRun_shouldAcceptArguments_whenAllArgumentsArePresent() {
		TestRunControllerValidator validator = new TestRunControllerValidator();

		assertThatCode(() -> validator.validateGetTestRun(3L, 1L, 2L)).doesNotThrowAnyException();
	}

	@Test
	void validateListTestRuns_shouldAcceptArguments_whenAllArgumentsArePresent() {
		TestRunControllerValidator validator = new TestRunControllerValidator();

		assertThatCode(() -> validator.validateListTestRuns(1L, 2L, 0, 20)).doesNotThrowAnyException();
	}

	@Test
	void validateGetTestRunStatistics_shouldAcceptArguments_whenAllArgumentsArePresent() {
		TestRunControllerValidator validator = new TestRunControllerValidator();

		assertThatCode(() -> validator.validateGetTestRunStatistics(1L, 2L, 3L)).doesNotThrowAnyException();
	}

	@FunctionalInterface
	private interface ValidationCall {

		void validate();

	}

}
