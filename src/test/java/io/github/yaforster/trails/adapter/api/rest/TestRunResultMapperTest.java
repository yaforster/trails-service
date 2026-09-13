package io.github.yaforster.trails.adapter.api.rest;

import io.github.yaforster.trails.adapter.api.rest.model.PersistedTestRunResultDTO;
import io.github.yaforster.trails.adapter.api.rest.model.ResultIndicatorDTO;
import io.github.yaforster.trails.adapter.api.rest.testrun.mapper.TestRunResultMapper;
import io.github.yaforster.trails.adapter.api.rest.testrun.model.TestRunResultModel;
import io.github.yaforster.trails.core.data.ResultIndicator;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;
import org.springframework.hateoas.Link;

import java.time.OffsetDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;

class TestRunResultMapperTest {

	private final TestRunResultMapper mapper = Mappers.getMapper(TestRunResultMapper.class);

	@Test
	void mapsTestRunResultModelToDto() {
		OffsetDateTime timestamp = OffsetDateTime.parse("2026-01-01T10:15:30Z");
		TestRunResultModel model = new TestRunResultModel(1L, timestamp, ResultIndicator.PARTIAL_SUCCESS, 2L, 3L,
				"run");
		model.add(Link.of("/test-runs/1", "self"));

		PersistedTestRunResultDTO dto = mapper.toDTO(model);

		assertEquals(1L, dto.getId());
		assertEquals(timestamp, dto.getTimestamp());
		assertEquals(ResultIndicatorDTO.PARTIAL_SUCCESS, dto.getIndicator());
		assertEquals(2L, dto.getApplicationId());
		assertEquals(3L, dto.getStageId());
		assertEquals("run", dto.getLabel());
		assertEquals("/test-runs/1", dto.getLinks().get("self").getHref());
	}

}
