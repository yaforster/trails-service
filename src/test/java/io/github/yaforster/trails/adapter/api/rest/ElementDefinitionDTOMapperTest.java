package io.github.yaforster.trails.adapter.api.rest;

import io.github.yaforster.trails.adapter.api.rest.element.mapper.ElementDefinitionDTOMapper;
import io.github.yaforster.trails.adapter.api.rest.model.ElementDefinitionDTO;
import io.github.yaforster.trails.core.TrailsTest;
import io.github.yaforster.trails.core.data.ElementType;
import io.github.yaforster.trails.core.definition.ElementDefinition;
import io.github.yaforster.trails.core.test.action.element.LocatorType;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ElementDefinitionDTOMapperTest extends TrailsTest {

	private final ElementDefinitionDTOMapper mapper = Mappers.getMapper(ElementDefinitionDTOMapper.class);

	@Test
	void mapsElementDefinitionDtoToDomain() {
		ElementDefinitionDTO dto = getInstancioOf(ElementDefinitionDTO.class).create();

		ElementDefinition result = mapper.toDomain(dto);

		assertEquals(dto.getLabel(), result.label());
		assertEquals(dto.getLocatorString(), result.locatorString());
		assertEquals(dto.getType() == null ? null : ElementType.valueOf(dto.getType().name()), result.type());
		assertEquals(dto.getLocatorType() == null ? null : LocatorType.valueOf(dto.getLocatorType().name()),
				result.locatorType());
	}

}
