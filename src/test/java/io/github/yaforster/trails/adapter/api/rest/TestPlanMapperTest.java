package io.github.yaforster.trails.adapter.api.rest;

import io.github.yaforster.trails.adapter.api.rest.model.TestPlanDTO;
import io.github.yaforster.trails.adapter.api.rest.testplan.mapper.TestPlanMapper;
import io.github.yaforster.trails.adapter.api.rest.testplan.mapper.TestPlanModel;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;
import org.springframework.hateoas.Link;

import static org.junit.jupiter.api.Assertions.assertEquals;

class TestPlanMapperTest {

	private final TestPlanMapper mapper = Mappers.getMapper(TestPlanMapper.class);

	@Test
	void mapsTestPlanModelToDto() {
		TestPlanModel model = new TestPlanModel(55L, "label");
		model.add(Link.of("/test-plans/55", "self"));

		TestPlanDTO dto = mapper.toDTO(model);

		assertEquals(55L, dto.getId());
		assertEquals("label", dto.getLabel());
		assertEquals("/test-plans/55", dto.getLinks().get("self").getHref());
	}

}
