package io.github.yaforster.trails.adapter.api.rest.validation;

import io.github.yaforster.trails.core.ValidationViolation;

public final class PaginationValidator {

	private PaginationValidator() {
	}

	public static void validate(ValidationContext validation, Integer page, Integer size, String violationPrefix) {
		if (page == null) {
			validation.addViolation(
					new ValidationViolation(violationPrefix + "_PAGE_NULL", "Page must not be null.", "/page"));
		}
		else if (page < 0) {
			validation.addViolation(new ValidationViolation(violationPrefix + "_PAGE_OUT_OF_RANGE",
					"Page must be greater than or equal to 0.", "/page"));
		}

		if (size == null) {
			validation.addViolation(
					new ValidationViolation(violationPrefix + "_SIZE_NULL", "Size must not be null.", "/size"));
		}
		else if (size < 1 || size > 100) {
			validation.addViolation(new ValidationViolation(violationPrefix + "_SIZE_OUT_OF_RANGE",
					"Size must be between 1 and 100.", "/size"));
		}
	}

	public static void validateWithGenericCodes(ValidationContext validation, Integer page, Integer size) {
		if (page == null || page < 0) {
			validation.addViolation(
					new ValidationViolation("PAGE_INVALID", "Page must be greater than or equal to 0.", "/page"));
		}

		if (size == null || size < 1 || size > 100) {
			validation
				.addViolation(new ValidationViolation("SIZE_INVALID", "Size must be between 1 and 100.", "/size"));
		}
	}

}
