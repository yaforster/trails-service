package io.github.yaforster.trails.adapter.api.rest.stage;

import io.github.yaforster.trails.adapter.api.APIRequestValidationException;
import io.github.yaforster.trails.adapter.api.rest.model.StageDefinitionDTO;
import io.github.yaforster.trails.app.services.StageDatabaseService;
import org.junit.jupiter.api.Test;

import java.net.URI;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class StageControllerValidatorTest {

	private static StageDatabaseService stageDatabaseService() {
		return mock(StageDatabaseService.class);
	}

	@Test
	void validate_shouldThrowAPIRequestValidationException_whenDtoIsNull() {
		StageControllerValidator validator = new StageControllerValidator(stageDatabaseService());

		APIRequestValidationException exception = assertThrows(APIRequestValidationException.class,
				() -> validator.validate(1L, null));

		assertEquals(2, exception.getValidationViolations().size());
	}

	@Test
	void validate_shouldThrowAPIRequestValidationException_whenLabelIsBlank() {
		StageControllerValidator validator = new StageControllerValidator(stageDatabaseService());
		StageDefinitionDTO dto = new StageDefinitionDTO().label("  ").url(URI.create("https://example.org"));

		APIRequestValidationException exception = assertThrows(APIRequestValidationException.class,
				() -> validator.validate(1L, dto));

		assertEquals("STAGE_LABEL_EMPTY", exception.getValidationViolations().getFirst().code());
	}

	@Test
	void validate_shouldThrowAPIRequestValidationException_whenUrlIsNull() {
		StageControllerValidator validator = new StageControllerValidator(stageDatabaseService());
		StageDefinitionDTO dto = new StageDefinitionDTO().label("stage");

		APIRequestValidationException exception = assertThrows(APIRequestValidationException.class,
				() -> validator.validate(1L, dto));

		assertEquals("STAGE_URL_EMPTY", exception.getValidationViolations().getFirst().code());
	}

	@Test
	void validate_shouldThrowAPIRequestValidationExceptionWithBothErrors_whenLabelAndUrlAreEmpty() {
		StageControllerValidator validator = new StageControllerValidator(stageDatabaseService());
		StageDefinitionDTO dto = new StageDefinitionDTO().label(" ");

		APIRequestValidationException exception = assertThrows(APIRequestValidationException.class,
				() -> validator.validate(1L, dto));

		assertEquals(2, exception.getValidationViolations().size());
	}

	@Test
	void validate_shouldAcceptStageDefinition_whenLabelAndUrlArePresent() {
		StageControllerValidator validator = new StageControllerValidator(stageDatabaseService());
		StageDefinitionDTO dto = new StageDefinitionDTO().label("stage").url(URI.create("https://example.org"));

		assertDoesNotThrow(() -> validator.validate(1L, dto));
	}

	@Test
	void validate_shouldThrowAPIRequestValidationException_whenLabelAlreadyExists() {
		StageDatabaseService databaseService = stageDatabaseService();
		when(databaseService.existsByLabel(new StageDatabaseService.StageLabel(1L, "stage"))).thenReturn(true);
		StageControllerValidator validator = new StageControllerValidator(databaseService);
		StageDefinitionDTO dto = new StageDefinitionDTO().label("stage").url(URI.create("https://example.org"));

		APIRequestValidationException exception = assertThrows(APIRequestValidationException.class,
				() -> validator.validate(1L, dto));

		assertEquals("STAGE_LABEL_ALREADY_EXISTS", exception.getValidationViolations().getFirst().code());
	}

	@Test
	void validate_shouldThrowAPIRequestValidationException_whenUrlAlreadyExists() {
		StageDatabaseService databaseService = stageDatabaseService();
		when(databaseService.existsByUrl(new StageDatabaseService.StageUrl(1L, "https://example.org")))
			.thenReturn(true);
		StageControllerValidator validator = new StageControllerValidator(databaseService);
		StageDefinitionDTO dto = new StageDefinitionDTO().label("stage").url(URI.create("https://example.org"));

		APIRequestValidationException exception = assertThrows(APIRequestValidationException.class,
				() -> validator.validate(1L, dto));

		assertEquals("STAGE_URL_ALREADY_EXISTS", exception.getValidationViolations().getFirst().code());
	}

	@Test
	void validateListStages_shouldRejectSizeAboveMaximum() {
		StageControllerValidator validator = new StageControllerValidator(stageDatabaseService());

		APIRequestValidationException exception = assertThrows(APIRequestValidationException.class,
				() -> validator.validateListStages(1L, 0, 101));

		assertEquals("STAGE_SIZE_OUT_OF_RANGE", exception.getValidationViolations().getFirst().code());
	}

}
