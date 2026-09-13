package io.github.yaforster.trails.adapter.api.rest.application;

import io.github.yaforster.trails.adapter.api.rest.model.ApplicationDefinitionDTO;
import io.github.yaforster.trails.adapter.api.rest.validation.ValidationContext;
import io.github.yaforster.trails.adapter.api.rest.validation.PaginationValidator;
import io.github.yaforster.trails.app.services.ApplicationDatabaseService;
import io.github.yaforster.trails.core.ValidationViolation;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@AllArgsConstructor
public class ApplicationControllerValidator {

	private final ApplicationDatabaseService applicationDatabaseService;

	public void validateDeleteApplication(Long applicationId) {
		ValidationContext validation = ValidationContext.begin();
		if (applicationId == null) {
			validation.addViolation(new ValidationViolation("APPLICATION_ID_NULL", "Application ID must not be null.",
					"/applicationId"));
		}
		validation.rejectIfErrors();
	}

	public void validateCreateNewApplication(ApplicationDefinitionDTO dto) {
		ValidationContext validation = ValidationContext.begin();
		if (isApplicationLabelEmpty(dto)) {
			validation.addViolation(new ValidationViolation("APPLICATION_LABEL_EMPTY",
					"Application label must not be empty.", "/label"));
		}
		if (!isApplicationLabelEmpty(dto) && applicationDatabaseService.existsByLabel(dto.getLabel())) {
			validation.addViolation(new ValidationViolation("APPLICATION_LABEL_ALREADY_EXISTS",
					"Application label must be unique.", "/label"));
		}
		validation.rejectIfErrors();
	}

	public void validateListApplications(Integer page, Integer size) {
		ValidationContext validation = ValidationContext.begin();
		PaginationValidator.validate(validation, page, size, "APPLICATION");
		validation.rejectIfErrors();
	}

	private boolean isApplicationLabelEmpty(ApplicationDefinitionDTO dto) {
		return dto == null || dto.getLabel() == null || dto.getLabel().isBlank();
	}

}
