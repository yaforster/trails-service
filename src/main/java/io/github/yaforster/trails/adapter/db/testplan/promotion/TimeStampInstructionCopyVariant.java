package io.github.yaforster.trails.adapter.db.testplan.promotion;

import io.github.yaforster.trails.adapter.db.testplan.entity.instruction.TimeStampInstructionEntity;
import io.github.yaforster.trails.adapter.db.testplan.entity.instruction.ValueComputationInstructionEntity;
import org.springframework.stereotype.Component;

@Component
class TimeStampInstructionCopyVariant
		extends AbstractValueComputationInstructionCopyVariant<TimeStampInstructionEntity> {

	TimeStampInstructionCopyVariant() {
		super(TimeStampInstructionEntity.class);
	}

	@Override
	protected ValueComputationInstructionEntity copyInstruction(TimeStampInstructionEntity instruction) {
		return new TimeStampInstructionEntity(instruction.getFormatPattern());
	}

}
