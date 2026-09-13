package io.github.yaforster.trails.adapter.api.rest.element;

import io.github.yaforster.trails.adapter.api.rest.model.ElementDefinitionDTO;
import io.github.yaforster.trails.adapter.api.rest.validation.ValidationContext;
import io.github.yaforster.trails.adapter.api.rest.validation.PaginationValidator;
import io.github.yaforster.trails.app.services.ElementDatabaseService;
import io.github.yaforster.trails.core.ValidationViolation;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

@Component
@AllArgsConstructor
public class ElementControllerValidator {

	private final ElementDatabaseService elementDatabaseService;

	public void validateCreateNewElement(Long applicationId, Long stageId, ElementDefinitionDTO dto) {
		ValidationContext validation = ValidationContext.begin();
		validateApplicationAndStage(validation, applicationId, stageId);
		validateElementDefinition(validation, dto);
		validateUniqueCreateFields(validation, applicationId, stageId, dto);
		validation.rejectIfErrors();
	}

	public void validateUpdateElement(Long applicationId, Long stageId, Long elementId, ElementDefinitionDTO dto) {
		ValidationContext validation = ValidationContext.begin();
		validateApplicationStageAndElement(validation, applicationId, stageId, elementId);
		validateElementDefinition(validation, dto);
		validation.rejectIfErrors();
	}

	private void validateElementDefinition(ValidationContext validation, ElementDefinitionDTO dto) {
		if (isElementTypeMissing(dto)) {
			validation
				.addViolation(new ValidationViolation("ELEMENT_TYPE_NULL", "Element type must not be null.", "/type"));
		}
		if (isElementLabelEmpty(dto)) {
			validation.addViolation(
					new ValidationViolation("ELEMENT_LABEL_EMPTY", "Element label must not be empty.", "/label"));
		}
		if (isElementLocatorStringEmpty(dto)) {
			validation.addViolation(new ValidationViolation("ELEMENT_LOCATOR_STRING_EMPTY",
					"Element locator string must not be empty.", "/locatorString"));
		}
		if (isElementLocatorTypeMissing(dto)) {
			validation.addViolation(new ValidationViolation("ELEMENT_LOCATOR_TYPE_NULL",
					"Element locator type must not be null.", "/locatorType"));
		}
	}

	private void validateUniqueCreateFields(ValidationContext validation, Long applicationId, Long stageId,
			ElementDefinitionDTO dto) {
		if (applicationId == null || stageId == null || dto == null) {
			return;
		}
		if (!isElementLabelEmpty(dto) && elementDatabaseService
			.existsByLabel(new ElementDatabaseService.ElementLabel(applicationId, stageId, dto.getLabel()))) {
			validation.addViolation(new ValidationViolation("ELEMENT_LABEL_ALREADY_EXISTS",
					"Element label must be unique within the stage.", "/label"));
		}
		if (!isElementLocatorStringEmpty(dto) && elementDatabaseService.existsByLocatorString(
				new ElementDatabaseService.ElementLocatorText(applicationId, stageId, dto.getLocatorString()))) {
			validation.addViolation(new ValidationViolation("ELEMENT_LOCATOR_STRING_ALREADY_EXISTS",
					"Element locator string must be unique within the stage.", "/locatorString"));
		}
	}

	public void validateGetElement(Long applicationId, Long stageId, Long elementId) {
		ValidationContext validation = ValidationContext.begin();
		validateApplicationStageAndElement(validation, applicationId, stageId, elementId);
		validation.rejectIfErrors();
	}

	public void validateListElements(Long applicationId, Long stageId, Integer page, Integer size) {
		ValidationContext validation = ValidationContext.begin();
		validateApplicationAndStage(validation, applicationId, stageId);
		PaginationValidator.validate(validation, page, size, "ELEMENT");
		validation.rejectIfErrors();
	}

	public void validateGetElementScreenshot(Long applicationId, Long stageId, Long elementId) {
		ValidationContext validation = ValidationContext.begin();
		validateApplicationStageAndElement(validation, applicationId, stageId, elementId);
		validation.rejectIfErrors();
	}

	public void validateUploadElementScreenshot(Long applicationId, Long stageId, Long elementId, MultipartFile file) {
		ValidationContext validation = ValidationContext.begin();
		validateApplicationStageAndElement(validation, applicationId, stageId, elementId);
		if (file == null) {
			validation.addViolation(new ValidationViolation("ELEMENT_SCREENSHOT_FILE_NULL",
					"Element screenshot file must not be null.", "/file"));
		}
		validation.rejectIfErrors();
	}

	public void validateDeleteElement(Long applicationId, Long stageId, Long elementId) {
		ValidationContext validation = ValidationContext.begin();
		validateApplicationStageAndElement(validation, applicationId, stageId, elementId);
		validation.rejectIfErrors();
	}

	public void validateDeleteElementScreenshot(Long applicationId, Long stageId, Long elementId) {
		ValidationContext validation = ValidationContext.begin();
		validateApplicationStageAndElement(validation, applicationId, stageId, elementId);
		validation.rejectIfErrors();
	}

	public void validatePromoteElement(Long applicationId, Long sourceStageId, Long elementId, Long targetStageId) {
		ValidationContext validation = ValidationContext.begin();
		validateApplicationStageAndElement(validation, applicationId, sourceStageId, elementId);
		if (targetStageId == null) {
			validation.addViolation(new ValidationViolation("ELEMENT_TARGET_STAGE_ID_NULL",
					"Target stage id must not be null.", "/targetStageId"));
		}
		if (sourceStageId != null && sourceStageId.equals(targetStageId)) {
			validation.addViolation(new ValidationViolation("ELEMENT_PROMOTE_TARGET_STAGE_EQUALS_SOURCE_STAGE",
					"Target stage must be different from source stage.", "/targetStageId"));
		}
		validation.rejectIfErrors();
	}

	private void validateApplicationStageAndElement(ValidationContext validation, Long applicationId, Long stageId,
			Long elementId) {
		validateApplicationAndStage(validation, applicationId, stageId);
		if (elementId == null) {
			validation
				.addViolation(new ValidationViolation("ELEMENT_ID_NULL", "Element id must not be null.", "/elementId"));
		}
	}

	private void validateApplicationAndStage(ValidationContext validation, Long applicationId, Long stageId) {
		if (applicationId == null) {
			validation.addViolation(new ValidationViolation("ELEMENT_APPLICATION_ID_NULL",
					"Application id must not be null.", "/applicationId"));
		}
		if (stageId == null) {
			validation.addViolation(
					new ValidationViolation("ELEMENT_STAGE_ID_NULL", "Stage id must not be null.", "/stageId"));
		}
	}

	private boolean isElementTypeMissing(ElementDefinitionDTO dto) {
		return dto == null || dto.getType() == null;
	}

	private boolean isElementLabelEmpty(ElementDefinitionDTO dto) {
		return dto == null || dto.getLabel() == null || dto.getLabel().isBlank();
	}

	private boolean isElementLocatorStringEmpty(ElementDefinitionDTO dto) {
		return dto == null || dto.getLocatorString() == null || dto.getLocatorString().isBlank();
	}

	private boolean isElementLocatorTypeMissing(ElementDefinitionDTO dto) {
		return dto == null || dto.getLocatorType() == null;
	}

}
