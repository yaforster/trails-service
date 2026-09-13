package io.github.yaforster.trails.adapter.api.rest.artifact;

import io.github.yaforster.trails.adapter.api.APIRequestValidationException;
import io.github.yaforster.trails.core.ValidationViolation;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ArtifactControllerValidatorTest {

	private static List<ValidationViolation> validateAndGetErrors(ValidationCall validationCall) {
		APIRequestValidationException exception = assertThrows(APIRequestValidationException.class,
				validationCall::validate);
		return exception.getValidationViolations();
	}

	@Test
	void validateDownloadArtifact_shouldThrowAPIRequestValidationExceptionWithAllErrors_whenAllArgumentsAreNull() {
		ArtifactControllerValidator validator = new ArtifactControllerValidator();

		List<ValidationViolation> validationViolations = validateAndGetErrors(
				() -> validator.validateDownloadArtifact(null, null, null, null, null, null));

		assertThat(validationViolations).hasSize(6);
	}

	@Test
	void validateDownloadArtifact_shouldThrowAPIRequestValidationException_whenApplicationIdIsNull() {
		ArtifactControllerValidator validator = new ArtifactControllerValidator();

		List<ValidationViolation> validationViolations = validateAndGetErrors(
				() -> validator.validateDownloadArtifact(null, 2L, 3L, 4L, 5L, 6L));

		assertThat(validationViolations.getFirst()).isEqualTo(new ValidationViolation("ARTIFACT_APPLICATION_ID_NULL",
				"Application id must not be null.", "/applicationId"));
	}

	@Test
	void validateDownloadArtifact_shouldThrowAPIRequestValidationException_whenStageIdIsNull() {
		ArtifactControllerValidator validator = new ArtifactControllerValidator();

		List<ValidationViolation> validationViolations = validateAndGetErrors(
				() -> validator.validateDownloadArtifact(1L, null, 3L, 4L, 5L, 6L));

		assertThat(validationViolations.getFirst())
			.isEqualTo(new ValidationViolation("ARTIFACT_STAGE_ID_NULL", "Stage id must not be null.", "/stageId"));
	}

	@Test
	void validateDownloadArtifact_shouldThrowAPIRequestValidationException_whenTestRunIdIsNull() {
		ArtifactControllerValidator validator = new ArtifactControllerValidator();

		List<ValidationViolation> validationViolations = validateAndGetErrors(
				() -> validator.validateDownloadArtifact(1L, 2L, null, 4L, 5L, 6L));

		assertThat(validationViolations.getFirst()).isEqualTo(
				new ValidationViolation("ARTIFACT_TEST_RUN_ID_NULL", "Test run id must not be null.", "/testRunId"));
	}

	@Test
	void validateDownloadArtifact_shouldThrowAPIRequestValidationException_whenTestSetResultIdIsNull() {
		ArtifactControllerValidator validator = new ArtifactControllerValidator();

		List<ValidationViolation> validationViolations = validateAndGetErrors(
				() -> validator.validateDownloadArtifact(1L, 2L, 3L, null, 5L, 6L));

		assertThat(validationViolations.getFirst()).isEqualTo(new ValidationViolation(
				"ARTIFACT_TEST_SET_RESULT_ID_NULL", "Test set result id must not be null.", "/testSetResultId"));
	}

	@Test
	void validateDownloadArtifact_shouldThrowAPIRequestValidationException_whenPathResultIdIsNull() {
		ArtifactControllerValidator validator = new ArtifactControllerValidator();

		List<ValidationViolation> validationViolations = validateAndGetErrors(
				() -> validator.validateDownloadArtifact(1L, 2L, 3L, 4L, null, 6L));

		assertThat(validationViolations.getFirst()).isEqualTo(new ValidationViolation("ARTIFACT_PATH_RESULT_ID_NULL",
				"Path result id must not be null.", "/pathResultId"));
	}

	@Test
	void validateDownloadArtifact_shouldThrowAPIRequestValidationException_whenArtifactIdIsNull() {
		ArtifactControllerValidator validator = new ArtifactControllerValidator();

		List<ValidationViolation> validationViolations = validateAndGetErrors(
				() -> validator.validateDownloadArtifact(1L, 2L, 3L, 4L, 5L, null));

		assertThat(validationViolations.getFirst())
			.isEqualTo(new ValidationViolation("ARTIFACT_ID_NULL", "Artifact id must not be null.", "/artifactId"));
	}

	@Test
	void validateDownloadActionResultScreenshot_shouldThrowAPIRequestValidationException_whenActionResultIdIsNull() {
		ArtifactControllerValidator validator = new ArtifactControllerValidator();

		List<ValidationViolation> validationViolations = validateAndGetErrors(
				() -> validator.validateDownloadActionResultScreenshot(1L, 2L, 3L, 4L, 5L, null));

		assertThat(validationViolations.getFirst()).isEqualTo(new ValidationViolation("ARTIFACT_ACTION_RESULT_ID_NULL",
				"Action result id must not be null.", "/actionResultId"));
	}

	@Test
	void validateListArtifacts_shouldThrowAPIRequestValidationException_whenPageIsNull() {
		ArtifactControllerValidator validator = new ArtifactControllerValidator();

		List<ValidationViolation> validationViolations = validateAndGetErrors(
				() -> validator.validateListArtifacts(1L, 2L, 3L, 4L, 5L, null, 20));

		assertThat(validationViolations.getFirst())
			.isEqualTo(new ValidationViolation("ARTIFACT_PAGE_NULL", "Page must not be null.", "/page"));
	}

	@Test
	void validateListArtifacts_shouldThrowAPIRequestValidationException_whenSizeIsNull() {
		ArtifactControllerValidator validator = new ArtifactControllerValidator();

		List<ValidationViolation> validationViolations = validateAndGetErrors(
				() -> validator.validateListArtifacts(1L, 2L, 3L, 4L, 5L, 0, null));

		assertThat(validationViolations.getFirst())
			.isEqualTo(new ValidationViolation("ARTIFACT_SIZE_NULL", "Size must not be null.", "/size"));
	}

	@Test
	void validateDownloadArtifact_shouldAcceptArguments_whenAllArgumentsArePresent() {
		ArtifactControllerValidator validator = new ArtifactControllerValidator();

		assertThatCode(() -> validator.validateDownloadArtifact(1L, 2L, 3L, 4L, 5L, 6L)).doesNotThrowAnyException();
	}

	@Test
	void validateDownloadActionResultScreenshot_shouldAcceptArguments_whenAllArgumentsArePresent() {
		ArtifactControllerValidator validator = new ArtifactControllerValidator();

		assertThatCode(() -> validator.validateDownloadActionResultScreenshot(1L, 2L, 3L, 4L, 5L, 6L))
			.doesNotThrowAnyException();
	}

	@Test
	void validateListArtifacts_shouldAcceptArguments_whenAllArgumentsArePresent() {
		ArtifactControllerValidator validator = new ArtifactControllerValidator();

		assertThatCode(() -> validator.validateListArtifacts(1L, 2L, 3L, 4L, 5L, 0, 20)).doesNotThrowAnyException();
	}

	@FunctionalInterface
	private interface ValidationCall {

		void validate();

	}

}
