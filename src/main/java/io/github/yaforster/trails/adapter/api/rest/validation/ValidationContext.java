package io.github.yaforster.trails.adapter.api.rest.validation;

import io.github.yaforster.trails.adapter.api.APIRequestValidationException;
import io.github.yaforster.trails.core.ValidationViolation;

import java.util.ArrayList;
import java.util.List;

public final class ValidationContext {

	private final List<ValidationViolation> validationViolations = new ArrayList<>();

	private ValidationContext() {
	}

	public static ValidationContext begin() {
		return new ValidationContext();
	}

	public void addViolation(ValidationViolation validationViolation) {
		validationViolations.add(validationViolation);
	}

	public void rejectIfErrors() {
		if (validationViolations.isEmpty()) {
			return;
		}
		throw new APIRequestValidationException(validationViolations);
	}

}
