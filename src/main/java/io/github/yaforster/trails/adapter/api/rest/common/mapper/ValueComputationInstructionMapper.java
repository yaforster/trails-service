package io.github.yaforster.trails.adapter.api.rest.common.mapper;

import io.github.yaforster.trails.adapter.api.rest.model.*;
import io.github.yaforster.trails.core.MappingException;
import io.github.yaforster.trails.core.test.action.browser.*;
import io.github.yaforster.trails.core.test.action.download.*;
import io.github.yaforster.trails.core.test.action.element.*;
import io.github.yaforster.trails.core.test.action.value.*;
import org.springframework.stereotype.Component;

@Component
public class ValueComputationInstructionMapper {

	public ValueComputationInstructionDTO toDTO(ValueComputationInstruction instruction) {
		return switch (instruction) {
			case FixedValueInstruction fixed -> new FixedValueDTO(ValueComputationTypeDTO.FIXED, fixed.computeValue());
			case RelativeDateInstruction date -> new RelativeDateInstructionDTO(ValueComputationTypeDTO.RELATIVE_DATE,
					date.offsetDays(), date.formatPattern());
			case RandomValueInstruction randomVal -> new RandomValueDTO(ValueComputationTypeDTO.RANDOM,
					randomVal.prefix(), randomVal.charPool(), randomVal.randomStringLength(), randomVal.suffix());
			case EnvInstruction env ->
				new EnvInstructionDTO(ValueComputationTypeDTO.SYSTEM_VAR, env.variableName(), env.defaultValue());
			case TimeStampInstruction timestamp ->
				new TimeStampInstructionDTO(ValueComputationTypeDTO.TIMESTAMP_NOW, timestamp.formatPattern());
			case TestDataValueInstruction testData -> new TestDataValueInstructionDTO(ValueComputationTypeDTO.TEST_DATA,
					testData.testDataId(), testData.key());
		};
	}

	public ValueComputationInstruction fromDTO(ValueComputationInstructionDTO dto) {
		if (dto == null) {
			throw new IllegalArgumentException("ValueComputationInstructionDTO cannot be null");
		}
		return switch (dto) {
			case FixedValueDTO fixed -> new FixedValueInstruction(fixed.getValue());
			case RelativeDateInstructionDTO date ->
				new RelativeDateInstruction(date.getOffsetDays(), date.getFormatPattern());
			case RandomValueDTO randomVal -> new RandomValueInstruction(randomVal.getPrefix(), randomVal.getSuffix(),
					randomVal.getCharPool(), randomVal.getRandomStringLength());
			case EnvInstructionDTO env -> new EnvInstruction(env.getVariableName(), env.getDefaultValue());
			case TimeStampInstructionDTO timestamp -> new TimeStampInstruction(timestamp.getFormatPattern());
			case TestDataValueInstructionDTO testData ->
				new TestDataValueInstruction(testData.getTestDataId(), testData.getKey());
			default -> throw new MappingException("Unsupported ValueComputationInstructionDTO: " + dto.getClass());
		};
	}

}
