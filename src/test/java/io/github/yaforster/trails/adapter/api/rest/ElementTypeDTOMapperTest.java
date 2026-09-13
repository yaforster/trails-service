package io.github.yaforster.trails.adapter.api.rest;

import io.github.yaforster.trails.adapter.api.rest.element.mapper.ElementTypeDTOMapper;
import io.github.yaforster.trails.adapter.api.rest.model.ElementTypeDTO;
import io.github.yaforster.trails.core.data.ElementType;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ElementTypeDTOMapperTest {

	private final ElementTypeDTOMapper mapper = Mappers.getMapper(ElementTypeDTOMapper.class);

	@Test
	void mapsElementTypeToDto() {
		assertEquals(ElementTypeDTO.RANGE, mapper.toDTO(ElementType.RANGE));
	}

}
