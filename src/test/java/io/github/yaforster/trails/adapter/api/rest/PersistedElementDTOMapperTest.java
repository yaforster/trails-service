package io.github.yaforster.trails.adapter.api.rest;

import io.github.yaforster.trails.adapter.api.rest.element.mapper.PersistedElementDTOMapper;
import io.github.yaforster.trails.adapter.api.rest.model.ElementTypeDTO;
import io.github.yaforster.trails.adapter.api.rest.model.PersistedElementDTO;
import io.github.yaforster.trails.adapter.api.rest.model.LocatorTypeDTO;
import io.github.yaforster.trails.core.data.ElementType;
import io.github.yaforster.trails.core.persisted.PersistedElement;
import io.github.yaforster.trails.core.test.action.element.LocatorType;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import static org.junit.jupiter.api.Assertions.assertEquals;

class PersistedElementDTOMapperTest {

	private final PersistedElementDTOMapper mapper = Mappers.getMapper(PersistedElementDTOMapper.class);

	@Test
	void mapsPersistedElementToDto() {
		PersistedElement model = new PersistedElement(1L, "label", LocatorType.XPATH, 2L, 3L, "//div",
				ElementType.TEXT);

		PersistedElementDTO dto = mapper.toDTO(model);

		assertEquals(1L, dto.getId());
		assertEquals("label", dto.getLabel());
		assertEquals(LocatorTypeDTO.XPATH, dto.getLocatorType());
		assertEquals("//div", dto.getLocator());
		assertEquals(ElementTypeDTO.TEXT, dto.getType());
	}

}
