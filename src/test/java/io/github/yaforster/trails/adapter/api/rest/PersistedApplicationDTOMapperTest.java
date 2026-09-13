package io.github.yaforster.trails.adapter.api.rest;

import io.github.yaforster.trails.adapter.api.rest.application.mapper.PersistedApplicationDTOMapper;
import io.github.yaforster.trails.adapter.api.rest.model.PersistedApplicationDTO;
import io.github.yaforster.trails.core.persisted.PersistedApplication;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import static org.junit.jupiter.api.Assertions.assertEquals;

class PersistedApplicationDTOMapperTest {

	private final PersistedApplicationDTOMapper mapper = Mappers.getMapper(PersistedApplicationDTOMapper.class);

	@Test
	void mapsPersistedApplicationToDto() {
		PersistedApplication model = new PersistedApplication(1L, "app");

		PersistedApplicationDTO dto = mapper.toDTO(model);

		assertEquals(1L, dto.getId());
		assertEquals("app", dto.getLabel());
	}

}
