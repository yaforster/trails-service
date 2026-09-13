package io.github.yaforster.trails.adapter.api.rest.element;

import io.github.yaforster.trails.adapter.api.APIRequestValidationException;
import io.github.yaforster.trails.adapter.api.rest.model.ElementDefinitionDTO;
import io.github.yaforster.trails.adapter.api.rest.model.ElementTypeDTO;
import io.github.yaforster.trails.adapter.api.rest.model.LocatorTypeDTO;
import io.github.yaforster.trails.app.services.ElementDatabaseService;
import io.github.yaforster.trails.core.TrailsTest;
import io.github.yaforster.trails.core.ValidationViolation;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ElementControllerValidatorTest extends TrailsTest {

	private static ElementControllerValidator validator() {
		return new ElementControllerValidator(mock(ElementDatabaseService.class));
	}

	private static ElementDefinitionDTO validDefinition() {
		return new ElementDefinitionDTO().type(ElementTypeDTO.BUTTON)
			.label("Login Button")
			.locatorString("#login")
			.locatorType(LocatorTypeDTO.CSS);
	}

	private static List<ValidationViolation> validateAndGetErrors(ValidationCall validationCall) {
		APIRequestValidationException exception = assertThrows(APIRequestValidationException.class,
				validationCall::validate);
		return exception.getValidationViolations();
	}

	@Test
	void validateCreateNewElement_shouldThrowAPIRequestValidationExceptionWithAllErrors_whenAllArgumentsAreNull() {
		ElementControllerValidator validator = validator();

		List<ValidationViolation> validationViolations = validateAndGetErrors(
				() -> validator.validateCreateNewElement(null, null, null));

		assertThat(validationViolations).hasSize(6);
	}

	@Test
	void validateCreateNewElement_shouldThrowAPIRequestValidationException_whenApplicationIdIsNull() {
		ElementControllerValidator validator = validator();

		List<ValidationViolation> validationViolations = validateAndGetErrors(
				() -> validator.validateCreateNewElement(null, 2L, validDefinition()));

		assertThat(validationViolations.getFirst()).isEqualTo(new ValidationViolation("ELEMENT_APPLICATION_ID_NULL",
				"Application id must not be null.", "/applicationId"));
	}

	@Test
	void validateCreateNewElement_shouldThrowAPIRequestValidationException_whenStageIdIsNull() {
		ElementControllerValidator validator = validator();

		List<ValidationViolation> validationViolations = validateAndGetErrors(
				() -> validator.validateCreateNewElement(1L, null, validDefinition()));

		assertThat(validationViolations.getFirst())
			.isEqualTo(new ValidationViolation("ELEMENT_STAGE_ID_NULL", "Stage id must not be null.", "/stageId"));
	}

	@Test
	void validateCreateNewElement_shouldThrowAPIRequestValidationException_whenTypeIsNull() {
		ElementControllerValidator validator = validator();
		ElementDefinitionDTO dto = validDefinition().type(null);

		List<ValidationViolation> validationViolations = validateAndGetErrors(
				() -> validator.validateCreateNewElement(1L, 2L, dto));

		assertThat(validationViolations.getFirst())
			.isEqualTo(new ValidationViolation("ELEMENT_TYPE_NULL", "Element type must not be null.", "/type"));
	}

	@Test
	void validateCreateNewElement_shouldThrowAPIRequestValidationException_whenLabelIsBlank() {
		ElementControllerValidator validator = validator();
		ElementDefinitionDTO dto = validDefinition().label("  ");

		List<ValidationViolation> validationViolations = validateAndGetErrors(
				() -> validator.validateCreateNewElement(1L, 2L, dto));

		assertThat(validationViolations.getFirst())
			.isEqualTo(new ValidationViolation("ELEMENT_LABEL_EMPTY", "Element label must not be empty.", "/label"));
	}

	@Test
	void validateCreateNewElement_shouldThrowAPIRequestValidationException_whenLocatorStringIsBlank() {
		ElementControllerValidator validator = validator();
		ElementDefinitionDTO dto = validDefinition().locatorString("  ");

		List<ValidationViolation> validationViolations = validateAndGetErrors(
				() -> validator.validateCreateNewElement(1L, 2L, dto));

		assertThat(validationViolations.getFirst()).isEqualTo(new ValidationViolation("ELEMENT_LOCATOR_STRING_EMPTY",
				"Element locator string must not be empty.", "/locatorString"));
	}

	@Test
	void validateCreateNewElement_shouldThrowAPIRequestValidationException_whenLocatorTypeIsNull() {
		ElementControllerValidator validator = validator();
		ElementDefinitionDTO dto = validDefinition().locatorType(null);

		List<ValidationViolation> validationViolations = validateAndGetErrors(
				() -> validator.validateCreateNewElement(1L, 2L, dto));

		assertThat(validationViolations.getFirst()).isEqualTo(new ValidationViolation("ELEMENT_LOCATOR_TYPE_NULL",
				"Element locator type must not be null.", "/locatorType"));
	}

	@Test
	void validateCreateNewElement_shouldThrowAPIRequestValidationException_whenLabelAlreadyExists() {
		ElementDatabaseService databaseService = mock(ElementDatabaseService.class);
		when(databaseService.existsByLabel(new ElementDatabaseService.ElementLabel(1L, 2L, "Login Button")))
			.thenReturn(true);
		ElementControllerValidator validator = new ElementControllerValidator(databaseService);

		List<ValidationViolation> validationViolations = validateAndGetErrors(
				() -> validator.validateCreateNewElement(1L, 2L, validDefinition()));

		assertThat(validationViolations.getFirst()).isEqualTo(new ValidationViolation("ELEMENT_LABEL_ALREADY_EXISTS",
				"Element label must be unique within the stage.", "/label"));
	}

	@Test
	void validateCreateNewElement_shouldThrowAPIRequestValidationException_whenLocatorStringAlreadyExists() {
		ElementDatabaseService databaseService = mock(ElementDatabaseService.class);
		when(databaseService.existsByLocatorString(new ElementDatabaseService.ElementLocatorText(1L, 2L, "#login")))
			.thenReturn(true);
		ElementControllerValidator validator = new ElementControllerValidator(databaseService);

		List<ValidationViolation> validationViolations = validateAndGetErrors(
				() -> validator.validateCreateNewElement(1L, 2L, validDefinition()));

		assertThat(validationViolations.getFirst())
			.isEqualTo(new ValidationViolation("ELEMENT_LOCATOR_STRING_ALREADY_EXISTS",
					"Element locator string must be unique within the stage.", "/locatorString"));
	}

	@Test
	void validateGetElement_shouldThrowAPIRequestValidationException_whenElementIdIsNull() {
		ElementControllerValidator validator = validator();

		List<ValidationViolation> validationViolations = validateAndGetErrors(
				() -> validator.validateGetElement(1L, 2L, null));

		assertThat(validationViolations.getFirst())
			.isEqualTo(new ValidationViolation("ELEMENT_ID_NULL", "Element id must not be null.", "/elementId"));
	}

	@Test
	void validateDeleteElement_shouldThrowAPIRequestValidationException_whenElementIdIsNull() {
		ElementControllerValidator validator = validator();

		List<ValidationViolation> validationViolations = validateAndGetErrors(
				() -> validator.validateDeleteElement(1L, 2L, null));

		assertThat(validationViolations.getFirst())
			.isEqualTo(new ValidationViolation("ELEMENT_ID_NULL", "Element id must not be null.", "/elementId"));
	}

	@Test
	void validateDeleteElementScreenshot_shouldThrowAPIRequestValidationException_whenElementIdIsNull() {
		ElementControllerValidator validator = validator();

		List<ValidationViolation> validationViolations = validateAndGetErrors(
				() -> validator.validateDeleteElementScreenshot(1L, 2L, null));

		assertThat(validationViolations.getFirst())
			.isEqualTo(new ValidationViolation("ELEMENT_ID_NULL", "Element id must not be null.", "/elementId"));
	}

	@Test
	void validateListElements_shouldThrowAPIRequestValidationException_whenPageIsNull() {
		ElementControllerValidator validator = validator();

		List<ValidationViolation> validationViolations = validateAndGetErrors(
				() -> validator.validateListElements(1L, 2L, null, 20));

		assertThat(validationViolations.getFirst())
			.isEqualTo(new ValidationViolation("ELEMENT_PAGE_NULL", "Page must not be null.", "/page"));
	}

	@Test
	void validateListElements_shouldThrowAPIRequestValidationException_whenSizeIsNull() {
		ElementControllerValidator validator = validator();

		List<ValidationViolation> validationViolations = validateAndGetErrors(
				() -> validator.validateListElements(1L, 2L, 0, null));

		assertThat(validationViolations.getFirst())
			.isEqualTo(new ValidationViolation("ELEMENT_SIZE_NULL", "Size must not be null.", "/size"));
	}

	@Test
	void validateGetElementScreenshot_shouldThrowAPIRequestValidationException_whenElementIdIsNull() {
		ElementControllerValidator validator = validator();

		List<ValidationViolation> validationViolations = validateAndGetErrors(
				() -> validator.validateGetElementScreenshot(1L, 2L, null));

		assertThat(validationViolations.getFirst())
			.isEqualTo(new ValidationViolation("ELEMENT_ID_NULL", "Element id must not be null.", "/elementId"));
	}

	@Test
	void validateUploadElementScreenshot_shouldThrowAPIRequestValidationException_whenFileIsNull() {
		ElementControllerValidator validator = validator();

		List<ValidationViolation> validationViolations = validateAndGetErrors(
				() -> validator.validateUploadElementScreenshot(1L, 2L, 3L, null));

		assertThat(validationViolations.getFirst()).isEqualTo(new ValidationViolation("ELEMENT_SCREENSHOT_FILE_NULL",
				"Element screenshot file must not be null.", "/file"));
	}

	@Test
	void validateCreateNewElement_shouldAcceptArguments_whenAllArgumentsArePresent() {
		ElementControllerValidator validator = validator();

		assertThatCode(() -> validator.validateCreateNewElement(1L, 2L, validDefinition())).doesNotThrowAnyException();
	}

	@Test
	void validateGetElement_shouldAcceptArguments_whenAllArgumentsArePresent() {
		ElementControllerValidator validator = validator();

		assertThatCode(() -> validator.validateGetElement(1L, 2L, 3L)).doesNotThrowAnyException();
	}

	@Test
	void validateDeleteElement_shouldAcceptArguments_whenAllArgumentsArePresent() {
		ElementControllerValidator validator = validator();

		assertThatCode(() -> validator.validateDeleteElement(1L, 2L, 3L)).doesNotThrowAnyException();
	}

	@Test
	void validateDeleteElementScreenshot_shouldAcceptArguments_whenAllArgumentsArePresent() {
		ElementControllerValidator validator = validator();

		assertThatCode(() -> validator.validateDeleteElementScreenshot(1L, 2L, 3L)).doesNotThrowAnyException();
	}

	@Test
	void validateListElements_shouldAcceptArguments_whenAllArgumentsArePresent() {
		ElementControllerValidator validator = validator();

		assertThatCode(() -> validator.validateListElements(1L, 2L, 0, 20)).doesNotThrowAnyException();
	}

	@Test
	void validateGetElementScreenshot_shouldAcceptArguments_whenAllArgumentsArePresent() {
		ElementControllerValidator validator = validator();

		assertThatCode(() -> validator.validateGetElementScreenshot(1L, 2L, 3L)).doesNotThrowAnyException();
	}

	@Test
	void validateUploadElementScreenshot_shouldAcceptArguments_whenAllArgumentsArePresent() {
		ElementControllerValidator validator = validator();
		MockMultipartFile file = new MockMultipartFile("file", "element.jpg", "image/jpeg", pngBytes());

		assertThatCode(() -> validator.validateUploadElementScreenshot(1L, 2L, 3L, file)).doesNotThrowAnyException();
	}

	@Test
	void validateUploadElementScreenshot_shouldAcceptArguments_whenFileIsEmpty() {
		ElementControllerValidator validator = validator();
		MockMultipartFile file = new MockMultipartFile("file", "element.png", "image/png", new byte[0]);

		assertThatCode(() -> validator.validateUploadElementScreenshot(1L, 2L, 3L, file)).doesNotThrowAnyException();
	}

	@Test
	void validateUploadElementScreenshot_shouldAcceptArguments_whenFileIsNotSupportedImage() {
		ElementControllerValidator validator = validator();
		MockMultipartFile file = new MockMultipartFile("file", "notes.txt", "text/plain", "not an image".getBytes());

		assertThatCode(() -> validator.validateUploadElementScreenshot(1L, 2L, 3L, file)).doesNotThrowAnyException();
	}

	@FunctionalInterface
	private interface ValidationCall {

		void validate();

	}

}
