package io.github.yaforster.trails.adapter.api.rest;

import io.github.yaforster.trails.adapter.api.rest.model.BrowserDTO;
import io.github.yaforster.trails.adapter.api.rest.model.TestPlanRunDefinitionDTO;
import io.github.yaforster.trails.adapter.api.rest.testrun.mapper.TestRunDefinitionMapper;
import io.github.yaforster.trails.core.TrailsTest;
import io.github.yaforster.trails.core.test.Browser;
import io.github.yaforster.trails.core.test.TestPlanRunDefinition;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class TestRunDefinitionMapperTest extends TrailsTest {

	private final TestRunDefinitionMapper mapper = Mappers.getMapper(TestRunDefinitionMapper.class);

	@Test
	void mapsTestPlanRunDefinitionDtoToDomain() {
		getInstancioOf(Long.class).stream().limit(TEST_REPETITIONS).forEach(testPlanId -> {
			TestPlanRunDefinitionDTO dto = new TestPlanRunDefinitionDTO();
			dto.setTestPlanID(testPlanId);
			dto.setBrowsersToTest(List.of(BrowserDTO.CHROME, BrowserDTO.FIREFOX));

			TestPlanRunDefinition result = mapper.fromDTO(dto);

			assertEquals(testPlanId, result.testPlanID());
			assertEquals(List.of(Browser.CHROME, Browser.FIREFOX), result.browsersToTest());
		});
	}

	@Test
	void fromDTO_ShouldReturnNullWhenInputIsNull() {
		assertNull(mapper.fromDTO(null));
	}

}
