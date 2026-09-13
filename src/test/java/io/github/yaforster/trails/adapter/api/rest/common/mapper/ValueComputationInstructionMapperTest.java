package io.github.yaforster.trails.adapter.api.rest.common.mapper;

import io.github.yaforster.trails.adapter.api.rest.common.mapper.ValueComputationInstructionMapper;
import io.github.yaforster.trails.core.MappingException;
import io.github.yaforster.trails.adapter.api.rest.model.*;
import io.github.yaforster.trails.core.test.action.browser.*;
import io.github.yaforster.trails.core.test.action.download.*;
import io.github.yaforster.trails.core.test.action.element.*;
import io.github.yaforster.trails.core.test.action.storage.*;
import io.github.yaforster.trails.core.test.action.value.*;
import io.github.yaforster.trails.core.test.action.viewport.*;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static org.junit.jupiter.api.Assertions.*;

class ValueComputationInstructionMapperTest {

	private final ValueComputationInstructionMapper mapper = new ValueComputationInstructionMapper();

	@Test
	void toDTO_ShouldMapFixedValueInstruction() {
		FixedValueInstruction instruction = new FixedValueInstruction("fixed");

		ValueComputationInstructionDTO result = mapper.toDTO(instruction);

		assertInstanceOf(FixedValueDTO.class, result);
		assertEquals("fixed", ((FixedValueDTO) result).getValue());
	}

	@Test
	void toDTO_ShouldMapRelativeDateInstruction() {
		RelativeDateInstruction instruction = new RelativeDateInstruction(3, "yyyy-MM-dd");

		ValueComputationInstructionDTO result = mapper.toDTO(instruction);

		assertInstanceOf(RelativeDateInstructionDTO.class, result);
		RelativeDateInstructionDTO dto = (RelativeDateInstructionDTO) result;
		assertEquals(3, dto.getOffsetDays());
		assertEquals("yyyy-MM-dd", dto.getFormatPattern());
	}

	@Test
	void toDTO_ShouldMapRandomValueInstruction() {
		RandomValueInstruction instruction = new RandomValueInstruction("pre-", "-suf", "abc123", 6);

		ValueComputationInstructionDTO result = mapper.toDTO(instruction);

		assertInstanceOf(RandomValueDTO.class, result);
		RandomValueDTO dto = (RandomValueDTO) result;
		assertEquals("pre-", dto.getPrefix());
		assertEquals("abc123", dto.getCharPool());
		assertEquals(6, dto.getRandomStringLength());
		assertEquals("-suf", dto.getSuffix());
	}

	@Test
	void toDTO_ShouldMapEnvInstruction() {
		EnvInstruction instruction = new EnvInstruction("ENV_NAME", "fallback");

		ValueComputationInstructionDTO result = mapper.toDTO(instruction);

		assertInstanceOf(EnvInstructionDTO.class, result);
		EnvInstructionDTO dto = (EnvInstructionDTO) result;
		assertEquals("ENV_NAME", dto.getVariableName());
		assertEquals("fallback", dto.getDefaultValue());
	}

	@Test
	void toDTO_ShouldMapTimeStampInstruction() {
		TimeStampInstruction instruction = new TimeStampInstruction("yyyyMMddHHmmss");

		ValueComputationInstructionDTO result = mapper.toDTO(instruction);

		assertInstanceOf(TimeStampInstructionDTO.class, result);
		assertEquals("yyyyMMddHHmmss", ((TimeStampInstructionDTO) result).getFormatPattern());
	}

	@Test
	void toDTO_ShouldMapTestDataValueInstruction() {
		TestDataValueInstruction instruction = new TestDataValueInstruction(5L, "username");

		ValueComputationInstructionDTO result = mapper.toDTO(instruction);

		assertInstanceOf(TestDataValueInstructionDTO.class, result);
		TestDataValueInstructionDTO dto = (TestDataValueInstructionDTO) result;
		assertEquals(5L, dto.getTestDataId());
		assertEquals("username", dto.getKey());
	}

	@Test
	void toDTO_ShouldThrowForNullInstruction() {
		assertThrows(NullPointerException.class, () -> mapper.toDTO(null));
	}

	@Test
	void fromDTO_ShouldMapFixedValueDTO() {
		FixedValueDTO dto = new FixedValueDTO(ValueComputationTypeDTO.FIXED, "fixed");

		ValueComputationInstruction result = mapper.fromDTO(dto);

		assertInstanceOf(FixedValueInstruction.class, result);
		assertEquals("fixed", ((FixedValueInstruction) result).value());
	}

	@Test
	void fromDTO_ShouldMapRelativeDateInstructionDTO() {
		RelativeDateInstructionDTO dto = new RelativeDateInstructionDTO(ValueComputationTypeDTO.RELATIVE_DATE, 5,
				"dd.MM.yyyy");

		ValueComputationInstruction result = mapper.fromDTO(dto);

		assertInstanceOf(RelativeDateInstruction.class, result);
		RelativeDateInstruction instruction = (RelativeDateInstruction) result;
		assertEquals(5, instruction.offsetDays());
		assertEquals("dd.MM.yyyy", instruction.formatPattern());
	}

	@Test
	void fromDTO_ShouldMapRandomValueDTO() {
		RandomValueDTO dto = new RandomValueDTO(ValueComputationTypeDTO.RANDOM, "pre-", "xyz", 8, "-suf");

		ValueComputationInstruction result = mapper.fromDTO(dto);

		assertInstanceOf(RandomValueInstruction.class, result);
		RandomValueInstruction instruction = (RandomValueInstruction) result;
		assertEquals("pre-", instruction.prefix());
		assertEquals("-suf", instruction.suffix());
		assertEquals("xyz", instruction.charPool());
		assertEquals(8, instruction.randomStringLength());
	}

	@Test
	void fromDTO_ShouldMapEnvInstructionDTO() {
		EnvInstructionDTO dto = new EnvInstructionDTO(ValueComputationTypeDTO.SYSTEM_VAR, "ENV_KEY", "default");

		ValueComputationInstruction result = mapper.fromDTO(dto);

		assertInstanceOf(EnvInstruction.class, result);
		EnvInstruction instruction = (EnvInstruction) result;
		assertEquals("ENV_KEY", instruction.variableName());
		assertEquals("default", instruction.defaultValue());
	}

	@Test
	void fromDTO_ShouldMapTimeStampInstructionDTO() {
		TimeStampInstructionDTO dto = new TimeStampInstructionDTO(ValueComputationTypeDTO.TIMESTAMP_NOW, "HH:mm:ss");

		ValueComputationInstruction result = mapper.fromDTO(dto);

		assertInstanceOf(TimeStampInstruction.class, result);
		assertEquals("HH:mm:ss", ((TimeStampInstruction) result).formatPattern());
	}

	@Test
	void fromDTO_ShouldMapTestDataValueInstructionDTO() {
		TestDataValueInstructionDTO dto = new TestDataValueInstructionDTO(ValueComputationTypeDTO.TEST_DATA, 8L,
				"password");

		ValueComputationInstruction result = mapper.fromDTO(dto);

		assertInstanceOf(TestDataValueInstruction.class, result);
		TestDataValueInstruction instruction = (TestDataValueInstruction) result;
		assertEquals(8L, instruction.testDataId());
		assertEquals("password", instruction.key());
	}

	@Test
	void fromDTO_ShouldThrowForNullDTO() {
		IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> mapper.fromDTO(null));
		assertEquals("ValueComputationInstructionDTO cannot be null", exception.getMessage());
	}

	@Test
	void fromDTO_ShouldThrowForUnsupportedDTOType() {
		ValueComputationInstructionDTO unsupported = Mockito.mock(ValueComputationInstructionDTO.class);

		assertThrows(MappingException.class, () -> mapper.fromDTO(unsupported));
	}

}
