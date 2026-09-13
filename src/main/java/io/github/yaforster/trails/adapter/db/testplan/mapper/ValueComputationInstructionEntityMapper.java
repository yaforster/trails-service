package io.github.yaforster.trails.adapter.db.testplan.mapper;

import io.github.yaforster.trails.adapter.db.testplan.entity.instruction.*;
import io.github.yaforster.trails.core.test.action.browser.*;
import io.github.yaforster.trails.core.test.action.download.*;
import io.github.yaforster.trails.core.test.action.element.*;
import io.github.yaforster.trails.core.test.action.value.*;
import org.springframework.stereotype.Component;

@Component
public class ValueComputationInstructionEntityMapper {

	public ValueComputationInstructionEntity toEntity(ValueComputationInstruction instruction) {
		return switch (instruction) {
			case FixedValueInstruction fv -> new FixedValueEntity(fv.value());
			case RelativeDateInstruction rdi ->
				new RelativeDateInstructionEntity(rdi.offsetDays(), rdi.formatPattern());
			case RandomValueInstruction randomVal -> new RandomValueInstructionEntity(randomVal.prefix(),
					randomVal.randomStringLength(), randomVal.charPool(), randomVal.suffix());
			case EnvInstruction envInstruction ->
				new EnvInstructionEntity(envInstruction.variableName(), envInstruction.defaultValue());
			case TimeStampInstruction timeStampInstruction ->
				new TimeStampInstructionEntity(timeStampInstruction.formatPattern());
			case TestDataValueInstruction testDataValueInstruction -> new TestDataValueInstructionEntity(
					testDataValueInstruction.testDataId(), testDataValueInstruction.key());
		};
	}

	public ValueComputationInstruction fromEntity(ValueComputationInstructionEntity entity) {
		return switch (entity) {
			case FixedValueEntity fixedValueEntity -> new FixedValueInstruction(fixedValueEntity.getValue());
			case RelativeDateInstructionEntity relativeDateInstructionEntity -> new RelativeDateInstruction(
					relativeDateInstructionEntity.getOffsetDays(), relativeDateInstructionEntity.getFormatPattern());
			case RandomValueInstructionEntity randomValueInstructionEntity -> new RandomValueInstruction(
					randomValueInstructionEntity.getPrefix(), randomValueInstructionEntity.getSuffix(),
					randomValueInstructionEntity.getCharPool(), randomValueInstructionEntity.getRandomStringLength());
			case EnvInstructionEntity envInstructionEntity ->
				new EnvInstruction(envInstructionEntity.getVariableName(), envInstructionEntity.getDefaultValue());
			case TimeStampInstructionEntity timeStampInstructionEntity ->
				new TimeStampInstruction(timeStampInstructionEntity.getFormatPattern());
			case TestDataValueInstructionEntity testDataValueInstructionEntity -> new TestDataValueInstruction(
					testDataValueInstructionEntity.getTestDataId(), testDataValueInstructionEntity.getTestDataKey());
			default -> throw new IllegalArgumentException(
					"Unsupported ValueComputationInstructionEntity: " + entity.getClass().getName());
		};
	}

}
