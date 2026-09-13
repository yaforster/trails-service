package io.github.yaforster.trails.adapter.api.rest.application;

import io.github.yaforster.trails.adapter.api.APIRequestValidationException;
import io.github.yaforster.trails.adapter.api.rest.model.ApplicationDefinitionDTO;
import io.github.yaforster.trails.app.services.ApplicationDatabaseService;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ApplicationControllerValidatorTest {

	private static ApplicationDatabaseService applicationDatabaseService() {
		return mock(ApplicationDatabaseService.class);
	}

	@Test
	void validate_CreateNewApplication_shouldThrowAPIRequestValidationException_whenDtoIsNull() {
		ApplicationControllerValidator validator = new ApplicationControllerValidator(applicationDatabaseService());

		APIRequestValidationException exception = assertThrows(APIRequestValidationException.class,
				() -> validator.validateCreateNewApplication(null));

		assertEquals("APPLICATION_LABEL_EMPTY", exception.getValidationViolations().getFirst().code());
	}

	@Test
	void validate_CreateNewApplication_shouldThrowAPIRequestValidationException_whenLabelIsNull() {
		ApplicationControllerValidator validator = new ApplicationControllerValidator(applicationDatabaseService());
		ApplicationDefinitionDTO dto = new ApplicationDefinitionDTO();

		APIRequestValidationException exception = assertThrows(APIRequestValidationException.class,
				() -> validator.validateCreateNewApplication(dto));

		assertEquals("APPLICATION_LABEL_EMPTY", exception.getValidationViolations().getFirst().code());
	}

	@Test
	void validate_CreateNewApplication_shouldThrowAPIRequestValidationException_whenLabelIsBlank() {
		ApplicationControllerValidator validator = new ApplicationControllerValidator(applicationDatabaseService());
		ApplicationDefinitionDTO dto = new ApplicationDefinitionDTO().label("  ");

		APIRequestValidationException exception = assertThrows(APIRequestValidationException.class,
				() -> validator.validateCreateNewApplication(dto));

		assertEquals("APPLICATION_LABEL_EMPTY", exception.getValidationViolations().getFirst().code());
	}

	@Test
	void validate_CreateNewApplication_shouldAcceptApplicationDefinition_whenLabelIsPresent() {
		ApplicationControllerValidator validator = new ApplicationControllerValidator(applicationDatabaseService());
		ApplicationDefinitionDTO dto = new ApplicationDefinitionDTO().label("Trails");

		assertDoesNotThrow(() -> validator.validateCreateNewApplication(dto));
	}

	@Test
	void validate_CreateNewApplication_shouldThrowAPIRequestValidationException_whenLabelAlreadyExists() {
		ApplicationDatabaseService databaseService = applicationDatabaseService();
		when(databaseService.existsByLabel("Trails")).thenReturn(true);
		ApplicationControllerValidator validator = new ApplicationControllerValidator(databaseService);
		ApplicationDefinitionDTO dto = new ApplicationDefinitionDTO().label("Trails");

		APIRequestValidationException exception = assertThrows(APIRequestValidationException.class,
				() -> validator.validateCreateNewApplication(dto));

		assertEquals("APPLICATION_LABEL_ALREADY_EXISTS", exception.getValidationViolations().getFirst().code());
	}

	@Test
	void validateListApplications_shouldRejectNegativePage() {
		ApplicationControllerValidator validator = new ApplicationControllerValidator(applicationDatabaseService());

		APIRequestValidationException exception = assertThrows(APIRequestValidationException.class,
				() -> validator.validateListApplications(-1, 20));

		assertEquals("APPLICATION_PAGE_OUT_OF_RANGE", exception.getValidationViolations().getFirst().code());
	}

}
