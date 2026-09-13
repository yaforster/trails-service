package io.github.yaforster.trails.adapter.testdata;

import io.github.yaforster.trails.app.services.TestDataDatabaseService;
import io.github.yaforster.trails.app.services.TestDataValueResolutionService;
import io.github.yaforster.trails.core.EntityMissingException;
import io.github.yaforster.trails.core.test.action.value.FixedValueInstruction;
import io.github.yaforster.trails.core.test.action.value.TestDataValueInstruction;
import io.github.yaforster.trails.core.test.action.value.ValueComputationInstruction;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

@AllArgsConstructor
@Service
public class TestDataValueResolutionServiceImpl implements TestDataValueResolutionService {

	private final TestDataDatabaseService testDataDatabaseService;

	@Override
	public ValueComputationInstruction resolveTestDataReference(ValueComputationInstruction instruction) {
		if (!(instruction instanceof TestDataValueInstruction(Long testDataId, String key))) {
			return instruction;
		}
		String value = testDataDatabaseService.getValue(new TestDataDatabaseService.TestDataValue(testDataId, key))
			.orElseThrow(() -> new EntityMissingException(
					"Test data value with data set id " + testDataId + " and key " + key + " could not be found."));
		return new FixedValueInstruction(value);
	}

}
