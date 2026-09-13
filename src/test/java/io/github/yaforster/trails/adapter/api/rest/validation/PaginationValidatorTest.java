package io.github.yaforster.trails.adapter.api.rest.validation;

import io.github.yaforster.trails.adapter.api.APIRequestValidationException;
import io.github.yaforster.trails.core.ValidationViolation;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.junit.jupiter.api.Assertions.assertThrows;

class PaginationValidatorTest {

	@Test
	void validate_shouldRejectNullPage() {
		ValidationViolation violation = validationViolationFor(null, 20);

		assertThat(violation)
			.isEqualTo(new ValidationViolation("RESOURCE_PAGE_NULL", "Page must not be null.", "/page"));
	}

	@Test
	void validate_shouldRejectNegativePage() {
		ValidationViolation violation = validationViolationFor(-1, 20);

		assertThat(violation).isEqualTo(new ValidationViolation("RESOURCE_PAGE_OUT_OF_RANGE",
				"Page must be greater than or equal to 0.", "/page"));
	}

	@Test
	void validate_shouldRejectNullSize() {
		ValidationViolation violation = validationViolationFor(0, null);

		assertThat(violation)
			.isEqualTo(new ValidationViolation("RESOURCE_SIZE_NULL", "Size must not be null.", "/size"));
	}

	@Test
	void validate_shouldRejectSizeBelowMinimum() {
		ValidationViolation violation = validationViolationFor(0, 0);

		assertThat(violation).isEqualTo(
				new ValidationViolation("RESOURCE_SIZE_OUT_OF_RANGE", "Size must be between 1 and 100.", "/size"));
	}

	@Test
	void validate_shouldRejectSizeAboveMaximum() {
		ValidationViolation violation = validationViolationFor(0, 101);

		assertThat(violation).isEqualTo(
				new ValidationViolation("RESOURCE_SIZE_OUT_OF_RANGE", "Size must be between 1 and 100.", "/size"));
	}

	@Test
	void validate_shouldAcceptContractBounds() {
		assertThatCode(() -> validate(0, 1)).doesNotThrowAnyException();
	}

	@Test
	void validate_shouldAcceptMaximumSize() {
		assertThatCode(() -> validate(Integer.MAX_VALUE, 100)).doesNotThrowAnyException();
	}

	@Test
	void validateWithGenericCodes_shouldUseGenericCodes() {
		ValidationContext validation = ValidationContext.begin();
		PaginationValidator.validateWithGenericCodes(validation, -1, 101);
		APIRequestValidationException exception = assertThrows(APIRequestValidationException.class,
				validation::rejectIfErrors);

		assertThat(exception.getValidationViolations()).containsExactly(
				new ValidationViolation("PAGE_INVALID", "Page must be greater than or equal to 0.", "/page"),
				new ValidationViolation("SIZE_INVALID", "Size must be between 1 and 100.", "/size"));
	}

	private ValidationViolation validationViolationFor(Integer page, Integer size) {
		ValidationContext validation = ValidationContext.begin();
		PaginationValidator.validate(validation, page, size, "RESOURCE");
		APIRequestValidationException exception = assertThrows(APIRequestValidationException.class,
				validation::rejectIfErrors);
		return exception.getValidationViolations().getFirst();
	}

	private void validate(Integer page, Integer size) {
		ValidationContext validation = ValidationContext.begin();
		PaginationValidator.validate(validation, page, size, "RESOURCE");
		validation.rejectIfErrors();
	}

}
