package io.github.yaforster.trails.adapter.api.rest.stage;

import io.github.yaforster.trails.adapter.api.rest.model.StageDefinitionDTO;
import io.github.yaforster.trails.adapter.api.rest.validation.ValidationContext;
import io.github.yaforster.trails.adapter.api.rest.validation.PaginationValidator;
import io.github.yaforster.trails.app.services.StageDatabaseService;
import io.github.yaforster.trails.core.ValidationViolation;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@AllArgsConstructor
public class StageControllerValidator {

	private final StageDatabaseService stageDatabaseService;

	public void validateDeleteStage(Long applicationId, Long stageId) {
		ValidationContext validation = ValidationContext.begin();
		if (applicationId == null) {
			validation.addViolation(new ValidationViolation("STAGE_APPLICATION_ID_NULL",
					"Application id must not be null.", "/applicationId"));
		}
		if (stageId == null) {
			validation.addViolation(new ValidationViolation("STAGE_ID_NULL", "Stage id must not be null.", "/stageId"));
		}
		validation.rejectIfErrors();
	}

	public void validate(Long applicationId, StageDefinitionDTO dto) {
		ValidationContext validation = ValidationContext.begin();
		if (applicationId == null) {
			validation.addViolation(new ValidationViolation("STAGE_APPLICATION_ID_NULL",
					"Application id must not be null.", "/applicationId"));
		}
		if (isStageLabelEmpty(dto)) {
			validation
				.addViolation(new ValidationViolation("STAGE_LABEL_EMPTY", "Stage label must not be empty.", "/label"));
		}
		if (isStageUrlMissing(dto)) {
			validation.addViolation(new ValidationViolation("STAGE_URL_EMPTY", "Stage url must not be empty.", "/url"));
		}
		if (applicationId != null && !isStageLabelEmpty(dto) && stageDatabaseService
			.existsByLabel(new StageDatabaseService.StageLabel(applicationId, dto.getLabel()))) {
			validation.addViolation(new ValidationViolation("STAGE_LABEL_ALREADY_EXISTS",
					"Stage label must be unique within the application.", "/label"));
		}
		if (applicationId != null && !isStageUrlMissing(dto) && stageDatabaseService
			.existsByUrl(new StageDatabaseService.StageUrl(applicationId, dto.getUrl().toString()))) {
			validation.addViolation(new ValidationViolation("STAGE_URL_ALREADY_EXISTS",
					"Stage url must be unique within the application.", "/url"));
		}
		validation.rejectIfErrors();
	}

	public void validateListStages(Long applicationId, Integer page, Integer size) {
		ValidationContext validation = ValidationContext.begin();
		if (applicationId == null) {
			validation.addViolation(new ValidationViolation("STAGE_APPLICATION_ID_NULL",
					"Application id must not be null.", "/applicationId"));
		}
		PaginationValidator.validate(validation, page, size, "STAGE");
		validation.rejectIfErrors();
	}

	private boolean isStageLabelEmpty(StageDefinitionDTO dto) {
		return dto == null || dto.getLabel() == null || dto.getLabel().isBlank();
	}

	private boolean isStageUrlMissing(StageDefinitionDTO dto) {
		return dto == null || dto.getUrl() == null;
	}

}
