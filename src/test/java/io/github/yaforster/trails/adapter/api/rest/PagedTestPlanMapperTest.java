package io.github.yaforster.trails.adapter.api.rest;

import io.github.yaforster.trails.adapter.api.rest.common.mapper.PageMetadataMapper;
import io.github.yaforster.trails.adapter.api.rest.model.PagedTestPlansDTO;
import io.github.yaforster.trails.adapter.api.rest.testplan.mapper.PagedTestPlanMapper;
import io.github.yaforster.trails.adapter.api.rest.testplan.mapper.TestPlanModel;
import org.junit.jupiter.api.Test;
import org.springframework.hateoas.Link;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class PagedTestPlanMapperTest {

	@Test
	void mapsPagedTestPlanModelToDto() {
		TestPlanModel model = new TestPlanModel(4L, "plan");
		model.add(Link.of("/test-plans/4", "self"));

		PagedTestPlanMapper mapper = new PagedTestPlanMapper(new PageMetadataMapper());
		PagedTestPlansDTO dto = mapper.toPagedDto(RestMapperTestSupport.paged(List.of(model)));

		assertEquals(1, dto.getItems().size());
		assertEquals(4L, dto.getItems().getFirst().getId());
		assertEquals("plan", dto.getItems().getFirst().getLabel());
		assertEquals("/test-plans/4", dto.getItems().getFirst().getLinks().get("self").getHref());
		assertEquals("/page/self", dto.getLinks().get("self").getHref());
	}

}
