package io.github.yaforster.trails.adapter.api.rest;

import io.github.yaforster.trails.adapter.api.rest.element.mapper.ElementDTOMapper;
import io.github.yaforster.trails.adapter.api.rest.element.model.ElementModel;
import io.github.yaforster.trails.adapter.api.rest.model.ElementDTO;
import io.github.yaforster.trails.adapter.api.rest.model.ElementTypeDTO;
import io.github.yaforster.trails.adapter.api.rest.model.LocatorTypeDTO;
import io.github.yaforster.trails.core.data.ElementType;
import io.github.yaforster.trails.core.test.action.element.LocatorType;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;
import org.springframework.hateoas.Link;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ElementDTOMapperTest {

	private final ElementDTOMapper mapper = Mappers.getMapper(ElementDTOMapper.class);

	@Test
	void mapsElementModelToDto() {
		ElementModel model = new ElementModel(3L, ElementType.INPUT, "field", "#id", LocatorType.CSS);
		model.add(Link.of("/elements/3", "self"));

		ElementDTO dto = mapper.toDTO(model);

		assertEquals(3L, dto.getId());
		assertEquals(ElementTypeDTO.INPUT, dto.getType());
		assertEquals("field", dto.getLabel());
		assertEquals("#id", dto.getLocatorString());
		assertEquals(LocatorTypeDTO.CSS, dto.getLocatorType());
		assertEquals("/elements/3", dto.getLinks().get("self").getHref());
	}

}
