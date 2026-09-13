package io.github.yaforster.trails.adapter.api;

import io.github.yaforster.trails.adapter.api.rest.model.ValidationErrorDTO;
import io.github.yaforster.trails.core.EntityMissingException;
import io.github.yaforster.trails.core.ValidationViolation;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class APIExceptionMapperTest {

	@Test
	void toErrorResponse_shouldUseTrailsExceptionStatus_whenExceptionExtendsTrailsException() {
		APIExceptionMapper mapper = mapper();

		ResponseEntity<List<ValidationErrorDTO>> response = mapper
			.toErrorResponse(new EntityMissingException("missing"));

		assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
	}

	@Test
	void toErrorResponse_shouldUseInternalServerError_whenExceptionIsUnexpected() {
		APIExceptionMapper mapper = mapper();

		ResponseEntity<List<ValidationErrorDTO>> response = mapper.toErrorResponse(new IllegalStateException("broken"));

		assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
	}

	@Test
	void toErrorResponse_shouldMapValidationErrors_whenExceptionIsAPIRequestValidationException() {
		APIExceptionMapper mapper = mapper();
		List<ValidationViolation> validationViolations = List
			.of(new ValidationViolation("APPLICATION_LABEL_EMPTY", "Application label must not be empty.", "/label"));

		ResponseEntity<List<ValidationErrorDTO>> response = mapper
			.toErrorResponse(new APIRequestValidationException(validationViolations));

		assertEquals("APPLICATION_LABEL_EMPTY", response.getBody().getFirst().getCode());
	}

	@Test
	void toErrorResponse_shouldMapScreenshotValidationExceptionToBadRequest() {
		APIExceptionMapper mapper = mapper();
		APIRequestValidationException exception = new APIRequestValidationException(
				new ValidationViolation("ELEMENT_SCREENSHOT_FILE_TYPE_UNSUPPORTED",
						"Element screenshot file must be a PNG, JPEG, GIF, BMP, or WebP image.", "/file"));

		ResponseEntity<List<ValidationErrorDTO>> response = mapper.toErrorResponse(exception);

		assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
	}

	@Test
	void toErrorResponse_shouldMapScreenshotValidationExceptionCode() {
		APIExceptionMapper mapper = mapper();
		APIRequestValidationException exception = new APIRequestValidationException(
				new ValidationViolation("ELEMENT_SCREENSHOT_FILE_TYPE_UNSUPPORTED",
						"Element screenshot file must be a PNG, JPEG, GIF, BMP, or WebP image.", "/file"));

		ResponseEntity<List<ValidationErrorDTO>> response = mapper.toErrorResponse(exception);

		assertEquals("ELEMENT_SCREENSHOT_FILE_TYPE_UNSUPPORTED", response.getBody().getFirst().getCode());
	}

	@Test
	void toErrorResponse_shouldNotExposeUnexpectedExceptionMessage_whenMessageIsPresent() {
		APIExceptionMapper mapper = mapper();

		ResponseEntity<List<ValidationErrorDTO>> response = mapper.toErrorResponse(new IllegalStateException("broken"));

		assertEquals("An unexpected error occurred.", response.getBody().getFirst().getMessage());
	}

	@Test
	void toErrorResponse_shouldUseDefaultMessage_whenUnexpectedExceptionMessageIsNull() {
		APIExceptionMapper mapper = mapper();

		ResponseEntity<List<ValidationErrorDTO>> response = mapper.toErrorResponse(new IllegalStateException());

		assertEquals("An unexpected error occurred.", response.getBody().getFirst().getMessage());
	}

	@Test
	void toErrorResponse_shouldUseDefaultMessage_whenUnexpectedExceptionMessageIsBlank() {
		APIExceptionMapper mapper = mapper();

		ResponseEntity<List<ValidationErrorDTO>> response = mapper.toErrorResponse(new IllegalStateException("  "));

		assertEquals("An unexpected error occurred.", response.getBody().getFirst().getMessage());
	}

	private APIExceptionMapper mapper() {
		return new APIExceptionMapper(Mappers.getMapper(ValidationErrorDTOMapper.class));
	}

}
