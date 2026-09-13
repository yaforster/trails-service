package io.github.yaforster.trails.adapter.api;

import io.github.yaforster.trails.core.TrailsException;
import io.github.yaforster.trails.core.ValidationViolation;
import lombok.Getter;
import org.springframework.http.HttpStatus;

import java.util.List;

@Getter
public class APIRequestValidationException extends TrailsException {

	private final HttpStatus statusCode = HttpStatus.BAD_REQUEST;

	private final List<ValidationViolation> validationViolations;

	public APIRequestValidationException(List<ValidationViolation> validationViolations) {
		this.validationViolations = List.copyOf(validationViolations);
	}

	public APIRequestValidationException(ValidationViolation validationViolation) {
		this.validationViolations = List.of(validationViolation);
	}

}
