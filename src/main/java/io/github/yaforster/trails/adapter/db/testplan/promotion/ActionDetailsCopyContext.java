package io.github.yaforster.trails.adapter.db.testplan.promotion;

import io.github.yaforster.trails.adapter.db.testplan.entity.instruction.ValueComputationInstructionEntity;
import io.github.yaforster.trails.app.services.ElementDatabaseService;

import java.util.Optional;

public record ActionDetailsCopyContext(ElementDatabaseService elementDatabaseService,
		ValueComputationInstructionCopyService instructionCopyService, Long applicationId, Long targetStageId) {

	public Optional<Long> targetElementId(Long sourceElementId) {
		return elementDatabaseService.findPromotionTargetElementId(
				new ElementDatabaseService.ElementPromotionTarget(applicationId, sourceElementId, targetStageId));
	}

	public ValueComputationInstructionEntity copyValueComputation(ValueComputationInstructionEntity instruction) {
		return instructionCopyService.copy(instruction);
	}

}
