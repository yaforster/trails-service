package io.github.yaforster.trails.adapter.api.rest;

import io.github.yaforster.trails.adapter.api.rest.model.PersistedStageDTO;
import io.github.yaforster.trails.adapter.api.rest.stage.mapper.PersistedStageDTOMapper;
import io.github.yaforster.trails.core.persisted.PersistedStage;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import java.net.URI;

import static org.junit.jupiter.api.Assertions.assertEquals;

class PersistedStageDTOMapperTest {

	private final PersistedStageDTOMapper mapper = Mappers.getMapper(PersistedStageDTOMapper.class);

	@Test
	void mapsPersistedStageToDto() {
		PersistedStage model = new PersistedStage(11L, 22L, "stage", "https://example.org");

		PersistedStageDTO dto = mapper.toDTO(model);

		assertEquals(11L, dto.getId());
		assertEquals("stage", dto.getLabel());
		assertEquals(URI.create("https://example.org"), dto.getUrl());
	}

}
