package io.github.yaforster.trails.adapter.testdata;

import io.github.yaforster.trails.app.services.TestDataDatabaseService;
import io.github.yaforster.trails.core.EntityMissingException;
import io.github.yaforster.trails.core.test.action.value.FixedValueInstruction;
import io.github.yaforster.trails.core.test.action.value.TestDataValueInstruction;
import io.github.yaforster.trails.core.test.action.value.ValueComputationInstruction;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class TestDataValueResolutionServiceTest {

	@Test
	void resolveTestDataReference_shouldReplaceTestDataInstructionWithFixedValueInstruction() {
		TestDataDatabaseService databaseService = Mockito.mock(TestDataDatabaseService.class);
		Mockito.when(databaseService.getValue(new TestDataDatabaseService.TestDataValue(4L, "username")))
			.thenReturn(Optional.of("alice"));
		TestDataValueResolutionServiceImpl service = new TestDataValueResolutionServiceImpl(databaseService);

		ValueComputationInstruction result = service
			.resolveTestDataReference(new TestDataValueInstruction(4L, "username"));

		assertThat(result).isEqualTo(new FixedValueInstruction("alice"));
	}

	@Test
	void resolveTestDataReference_shouldRaiseEntityMissingException_whenValueDoesNotExist() {
		TestDataDatabaseService databaseService = Mockito.mock(TestDataDatabaseService.class);
		Mockito.when(databaseService.getValue(new TestDataDatabaseService.TestDataValue(4L, "username")))
			.thenReturn(Optional.empty());
		TestDataValueResolutionServiceImpl service = new TestDataValueResolutionServiceImpl(databaseService);

		assertThatThrownBy(() -> service.resolveTestDataReference(new TestDataValueInstruction(4L, "username")))
			.isInstanceOf(EntityMissingException.class);
	}

	@Test
	void resolveTestDataReference_shouldKeepOriginalInstruction_whenInstructionDoesNotNeedResolution() {
		TestDataDatabaseService databaseService = Mockito.mock(TestDataDatabaseService.class);
		TestDataValueResolutionServiceImpl service = new TestDataValueResolutionServiceImpl(databaseService);
		FixedValueInstruction instruction = new FixedValueInstruction("alice");

		ValueComputationInstruction result = service.resolveTestDataReference(instruction);

		assertThat(result).isSameAs(instruction);
	}

}
