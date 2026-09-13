package io.github.yaforster.trails.adapter.db.testplan.promotion;

import io.github.yaforster.trails.adapter.db.testplan.entity.instruction.ValueComputationInstructionEntity;

public interface ValueComputationInstructionCopyVariant {

	Class<? extends ValueComputationInstructionEntity> instructionType();

	ValueComputationInstructionEntity copy(ValueComputationInstructionEntity instruction);

}
