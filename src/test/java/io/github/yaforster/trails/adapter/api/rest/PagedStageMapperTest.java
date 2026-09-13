package io.github.yaforster.trails.adapter.api.rest;

import io.github.yaforster.trails.adapter.api.rest.common.mapper.PageMetadataMapper;
import io.github.yaforster.trails.adapter.api.rest.model.PagedStageDTO;
import io.github.yaforster.trails.adapter.api.rest.stage.mapper.PagedStageMapper;
import io.github.yaforster.trails.adapter.api.rest.stage.model.StageModel;
import org.junit.jupiter.api.Test;
import org.springframework.hateoas.Link;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class PagedStageMapperTest {

	@Test
	void mapsPagedStageModelToDto() {
		StageModel model = new StageModel(7L, "stage", "https://example.org");
		model.add(Link.of("/stages/7", "self"));

		PagedStageMapper mapper = new PagedStageMapper(new PageMetadataMapper());
		PagedStageDTO dto = mapper.toPagedDto(RestMapperTestSupport.paged(List.of(model)));

		assertEquals(1, dto.getItems().size());
		assertEquals(7L, dto.getItems().getFirst().getId());
		assertEquals("stage", dto.getItems().getFirst().getLabel());
		assertEquals("/stages/7", dto.getItems().getFirst().getLinks().get("self").getHref());
		assertEquals("/page/self", dto.getLinks().get("self").getHref());
	}

}
