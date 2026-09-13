package io.github.yaforster.trails.adapter.api.rest;

import io.github.yaforster.trails.adapter.api.rest.application.mapper.ApplicationDefinitionDTOMapper;
import io.github.yaforster.trails.adapter.api.rest.model.ApplicationDefinitionDTO;
import io.github.yaforster.trails.core.TrailsTest;
import io.github.yaforster.trails.core.definition.ApplicationDefinition;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ApplicationDefinitionDTOMapperTest extends TrailsTest {

	private final ApplicationDefinitionDTOMapper mapper = Mappers.getMapper(ApplicationDefinitionDTOMapper.class);

	@Test
	void mapsApplicationDefinitionDtoToDomain() {
		ApplicationDefinitionDTO dto = getInstancioOf(ApplicationDefinitionDTO.class).create();

		ApplicationDefinition result = mapper.toDomain(dto);

		assertEquals(dto.getLabel(), result.label());
	}

}
