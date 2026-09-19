package io.github.yaforster.trails.adapter.api;

import com.google.common.annotations.VisibleForTesting;
import io.github.yaforster.trails.adapter.api.rest.model.ValidationErrorDTO;
import io.github.yaforster.trails.core.ErrorClassification;
import io.github.yaforster.trails.core.TrailsException;
import io.github.yaforster.trails.core.ValidationViolation;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import java.util.List;

@AllArgsConstructor
@Component
@Slf4j
public class APIExceptionMapper {

	private static final String DEFAULT_ERROR_MESSAGE = "An unexpected error occurred.";

	private static final String GENERIC_TRAILS_ERROR = "TRAILS_ERROR";

	private static final String INTERNAL_SERVER_ERROR = "INTERNAL_SERVER_ERROR";

	private static final String GENERIC_ERROR_PATH = "/";

	private final ValidationErrorDTOMapper validationErrorDTOMapper;

	public ResponseEntity<List<ValidationErrorDTO>> toErrorResponse(Exception exception) {
		return switch (exception) {
			case APIRequestValidationException validationException -> handleValidationError(validationException);
			case TrailsException genericTrailsException -> handleMiscellaneousTrailsException(genericTrailsException);
			default -> handleUnexpectedException(exception);
		};
	}

	@VisibleForTesting
	protected ResponseEntity<List<ValidationErrorDTO>> handleValidationError(APIRequestValidationException exception) {
		return ResponseEntity.status(exception.getStatusCode())
			.body(validationErrorDTOMapper.toDTOs(exception.getValidationViolations()));
	}

	@VisibleForTesting
	protected ResponseEntity<List<ValidationErrorDTO>> handleMiscellaneousTrailsException(TrailsException exception) {
		log.error("TrailsException occurred:", exception);
		ValidationViolation validationViolation = new ValidationViolation(GENERIC_TRAILS_ERROR, exception.userMessage(),
				GENERIC_ERROR_PATH);
		return ResponseEntity.status(httpStatus(exception.errorClassification()))
			.body(validationErrorDTOMapper.toDTOs(List.of(validationViolation)));
	}

	private int httpStatus(ErrorClassification errorClassification) {
		return switch (errorClassification) {
			case INTERNAL_ERROR -> 500;
			case NOT_FOUND -> 404;
		};
	}

	@VisibleForTesting
	protected ResponseEntity<List<ValidationErrorDTO>> handleUnexpectedException(Exception exception) {
		log.error("Unexpected error occurred that is not a TrailsException:", exception);
		ValidationViolation validationViolation = new ValidationViolation(INTERNAL_SERVER_ERROR, DEFAULT_ERROR_MESSAGE,
				GENERIC_ERROR_PATH);
		return ResponseEntity.internalServerError().body(validationErrorDTOMapper.toDTOs(List.of(validationViolation)));
	}

}
