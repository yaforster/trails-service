package io.github.yaforster.trails.adapter.api.rest.testdata;

import io.github.yaforster.trails.adapter.api.rest.model.TestDataSetDefinitionDTO;
import io.github.yaforster.trails.adapter.api.rest.validation.ValidationContext;
import io.github.yaforster.trails.adapter.api.rest.validation.PaginationValidator;
import io.github.yaforster.trails.core.ValidationViolation;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.Set;

@Component
public class TestDataControllerValidator {

	public void validateCreateNewTestData(TestDataSetDefinitionDTO dto) {
		ValidationContext validation = ValidationContext.begin();
		validateTestDataDefinition(validation, dto);
		validation.rejectIfErrors();
	}

	public void validateUpdateTestData(Long testDataId, TestDataSetDefinitionDTO dto) {
		ValidationContext validation = ValidationContext.begin();
		if (testDataId == null) {
			validation.addViolation(
					new ValidationViolation("TEST_DATA_ID_NULL", "Test data id must not be null.", "/testDataId"));
		}
		validateTestDataDefinition(validation, dto);
		validation.rejectIfErrors();
	}

	public void validateDeleteTestData(Long testDataId) {
		validateGetTestData(testDataId);
	}

	public void validateRestoreTestData(Long testDataId) {
		validateGetTestData(testDataId);
	}

	public void validateGetTestData(Long testDataId) {
		ValidationContext validation = ValidationContext.begin();
		if (testDataId == null) {
			validation.addViolation(
					new ValidationViolation("TEST_DATA_ID_NULL", "Test data id must not be null.", "/testDataId"));
		}
		validation.rejectIfErrors();
	}

	public void validateListTestData(Integer page, Integer size) {
		ValidationContext validation = ValidationContext.begin();
		PaginationValidator.validate(validation, page, size, "TEST_DATA");
		validation.rejectIfErrors();
	}

	private void validateTestDataDefinition(ValidationContext validation, TestDataSetDefinitionDTO dto) {
		if (isTestDataLabelEmpty(dto)) {
			validation.addViolation(
					new ValidationViolation("TEST_DATA_LABEL_EMPTY", "Test data label must not be empty.", "/label"));
		}
		if (areTestDataValuesMissing(dto)) {
			validation.addViolation(
					new ValidationViolation("TEST_DATA_VALUES_NULL", "Test data values must not be null.", "/values"));
		}
		if (dto != null && dto.getValues() != null) {
			validateTestDataEntries(validation, dto);
		}
	}

	private void validateTestDataEntries(ValidationContext validation, TestDataSetDefinitionDTO dto) {
		Set<String> keys = new HashSet<>();
		for (int index = 0; index < dto.getValues().size(); index++) {
			String path = "/values/" + index;
			if (isTestDataKeyEmpty(dto, index)) {
				validation.addViolation(new ValidationViolation("TEST_DATA_KEY_EMPTY",
						"Test data key must not be empty.", path + "/key"));
			}
			if (isTestDataValueMissing(dto, index)) {
				validation.addViolation(new ValidationViolation("TEST_DATA_VALUE_NULL",
						"Test data value must not be null.", path + "/value"));
			}
			String key = dto.getValues().get(index).getKey();
			if (key != null && !keys.add(key)) {
				validation.addViolation(new ValidationViolation("TEST_DATA_KEY_DUPLICATE",
						"Test data keys must be unique within a data set.", path + "/key"));
			}
		}
	}

	private boolean isTestDataLabelEmpty(TestDataSetDefinitionDTO dto) {
		return dto == null || dto.getLabel() == null || dto.getLabel().isBlank();
	}

	private boolean areTestDataValuesMissing(TestDataSetDefinitionDTO dto) {
		return dto == null || dto.getValues() == null;
	}

	private boolean isTestDataKeyEmpty(TestDataSetDefinitionDTO dto, int index) {
		String key = dto.getValues().get(index).getKey();
		return key == null || key.isBlank();
	}

	private boolean isTestDataValueMissing(TestDataSetDefinitionDTO dto, int index) {
		return dto.getValues().get(index).getValue() == null;
	}

}
