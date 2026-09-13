package io.github.yaforster.trails.adapter.api.rest.application.mapper;

import io.github.yaforster.trails.adapter.api.rest.model.PersistedApplicationDTO;
import io.github.yaforster.trails.app.GlobalMapperConfig;
import io.github.yaforster.trails.core.persisted.PersistedApplication;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@FunctionalInterface
@Mapper(config = GlobalMapperConfig.class)
public interface PersistedApplicationDTOMapper {

	@Mapping(target = "links", ignore = true)
	PersistedApplicationDTO toDTO(PersistedApplication model);

}
