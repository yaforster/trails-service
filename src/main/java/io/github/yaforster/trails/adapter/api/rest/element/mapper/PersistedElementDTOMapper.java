package io.github.yaforster.trails.adapter.api.rest.element.mapper;

import io.github.yaforster.trails.adapter.api.rest.model.PersistedElementDTO;
import io.github.yaforster.trails.app.GlobalMapperConfig;
import io.github.yaforster.trails.core.persisted.PersistedElement;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@FunctionalInterface
@Mapper(config = GlobalMapperConfig.class)
public interface PersistedElementDTOMapper {

	@Mapping(target = "links", ignore = true)
	@Mapping(target = "locatorString", source = "locator")
	PersistedElementDTO toDTO(PersistedElement persistedElement);

}
