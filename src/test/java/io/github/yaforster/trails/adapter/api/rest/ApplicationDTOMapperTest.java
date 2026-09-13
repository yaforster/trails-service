package io.github.yaforster.trails.adapter.api.rest;

import io.github.yaforster.trails.adapter.api.rest.application.mapper.ApplicationDTOMapper;
import io.github.yaforster.trails.adapter.api.rest.application.model.ApplicationModel;
import io.github.yaforster.trails.adapter.api.rest.model.ApplicationDTO;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;
import org.springframework.hateoas.Link;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ApplicationDTOMapperTest {

	private final ApplicationDTOMapper mapper = Mappers.getMapper(ApplicationDTOMapper.class);

	@Test
	void mapsApplicationModelToDto() {
		ApplicationModel model = new ApplicationModel(1L, "app");
		model.add(Link.of("/applications/1", "self"));

		ApplicationDTO dto = mapper.toDTO(model);

		assertEquals(1L, dto.getId());
		assertEquals("app", dto.getLabel());
		assertEquals("/applications/1", dto.getLinks().get("self").getHref());
	}

}
