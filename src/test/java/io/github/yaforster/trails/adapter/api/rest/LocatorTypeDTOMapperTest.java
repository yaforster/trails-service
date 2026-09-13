package io.github.yaforster.trails.adapter.api.rest;

import io.github.yaforster.trails.adapter.api.rest.element.mapper.LocatorTypeDTOMapper;
import io.github.yaforster.trails.adapter.api.rest.model.LocatorTypeDTO;
import io.github.yaforster.trails.core.test.action.element.LocatorType;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import static org.junit.jupiter.api.Assertions.assertEquals;

class LocatorTypeDTOMapperTest {

	private final LocatorTypeDTOMapper mapper = Mappers.getMapper(LocatorTypeDTOMapper.class);

	@Test
	void mapsLocatorTypeToDto() {
		assertEquals(LocatorTypeDTO.CSS, mapper.toDTO(LocatorType.CSS));
	}

}
