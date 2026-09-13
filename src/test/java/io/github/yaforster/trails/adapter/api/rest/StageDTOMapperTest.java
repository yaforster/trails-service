package io.github.yaforster.trails.adapter.api.rest;

import io.github.yaforster.trails.adapter.api.rest.model.StageDTO;
import io.github.yaforster.trails.adapter.api.rest.stage.mapper.StageDTOMapper;
import io.github.yaforster.trails.adapter.api.rest.stage.model.StageModel;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;
import org.springframework.hateoas.Link;

import static org.junit.jupiter.api.Assertions.assertEquals;

class StageDTOMapperTest {

	private final StageDTOMapper mapper = Mappers.getMapper(StageDTOMapper.class);

	@Test
	void mapsStageModelToDto() {
		StageModel model = new StageModel(8L, "stage", "https://example.org");
		model.add(Link.of("/stages/8", "self"));

		StageDTO dto = mapper.toDTO(model);

		assertEquals(8L, dto.getId());
		assertEquals("stage", dto.getLabel());
		assertEquals("https://example.org", dto.getUrl());
		assertEquals("/stages/8", dto.getLinks().get("self").getHref());
	}

}
