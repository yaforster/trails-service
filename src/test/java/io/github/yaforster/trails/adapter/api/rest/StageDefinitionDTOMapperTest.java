package io.github.yaforster.trails.adapter.api.rest;

import io.github.yaforster.trails.adapter.api.rest.model.StageDefinitionDTO;
import io.github.yaforster.trails.adapter.api.rest.stage.mapper.StageDefinitionDTOMapper;
import io.github.yaforster.trails.core.TrailsTest;
import io.github.yaforster.trails.core.definition.StageDefinition;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import static org.junit.jupiter.api.Assertions.*;

class StageDefinitionDTOMapperTest extends TrailsTest {

	private final StageDefinitionDTOMapper mapper = Mappers.getMapper(StageDefinitionDTOMapper.class);

	@Test
	void mapsStageDefinitionDtoToDomain() {
		StageDefinitionDTO dto = getInstancioOf(StageDefinitionDTO.class).create();

		StageDefinition result = mapper.toDomain(dto);

		assertEquals(dto.getLabel(), result.label());
		assertEquals(dto.getUrl() != null ? dto.getUrl().toString() : null, result.url());
		assertNotNull(result.availableElements());
		assertTrue(result.availableElements().isEmpty());
	}

}
