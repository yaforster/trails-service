package io.github.yaforster.trails.adapter.api.rest.action.mapper;

import io.github.yaforster.trails.adapter.api.rest.common.mapper.PositionMapper;
import io.github.yaforster.trails.adapter.api.rest.common.mapper.ValueComputationInstructionMapper;
import io.github.yaforster.trails.adapter.api.rest.model.ActionDefinitionDTO;
import io.github.yaforster.trails.adapter.api.rest.model.ValueComputationInstructionDTO;
import io.github.yaforster.trails.app.services.ElementDatabaseService;
import io.github.yaforster.trails.core.test.Position;
import io.github.yaforster.trails.core.test.action.element.Locator;
import io.github.yaforster.trails.core.test.action.value.ValueComputationInstruction;

public record ActionDetailsMappingContext(ElementDatabaseService elementDatabaseService, PositionMapper positionMapper,
		ValueComputationInstructionMapper instructionMapper, Long applicationId, Long stageId) {

	Long elementId(Locator locator) {
		return elementDatabaseService.getElementIDByLocator(
				new ElementDatabaseService.ElementLocator(applicationId, stageId, locator.locatorString()));
	}

	ActionDetailsMappingContext scoped(Long applicationId, Long stageId) {
		return new ActionDetailsMappingContext(elementDatabaseService, positionMapper, instructionMapper, applicationId,
				stageId);
	}

	Locator elementLocator(Long elementId) {
		return elementDatabaseService.getElementLocator(elementId);
	}

	Position position(ActionDefinitionDTO dto) {
		return positionMapper.fromDTO(dto.getPosition());
	}

	ValueComputationInstructionDTO toDTO(ValueComputationInstruction instruction) {
		return instructionMapper.toDTO(instruction);
	}

	ValueComputationInstruction fromDTO(ValueComputationInstructionDTO dto) {
		return instructionMapper.fromDTO(dto);
	}

	long nonNegativeOrZero(Long value) {
		return value == null ? 0L : Math.max(0L, value);
	}
}
