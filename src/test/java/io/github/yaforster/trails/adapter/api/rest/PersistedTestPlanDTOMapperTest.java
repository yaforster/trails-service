package io.github.yaforster.trails.adapter.api.rest;

import io.github.yaforster.trails.adapter.api.rest.model.PersistedTestPlanDTO;
import io.github.yaforster.trails.adapter.api.rest.testplan.mapper.PersistedTestPlanDTOMapper;
import io.github.yaforster.trails.adapter.api.rest.testplan.model.PersistedTestPlanModel;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;
import org.springframework.hateoas.Link;

import static org.junit.jupiter.api.Assertions.assertEquals;

class PersistedTestPlanDTOMapperTest {

	private final PersistedTestPlanDTOMapper mapper = Mappers.getMapper(PersistedTestPlanDTOMapper.class);

	@Test
	void mapsPersistedTestPlanModelToDto() {
		PersistedTestPlanModel model = new PersistedTestPlanModel(1L, 2L, 3L, "plan");
		model.add(Link.of("/test-plans/1", "self"));

		PersistedTestPlanDTO dto = mapper.toDTO(model);

		assertEquals(1L, dto.getId());
		assertEquals("/test-plans/1", dto.getLinks().get("self").getHref());
	}

}
