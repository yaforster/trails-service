package io.github.yaforster.trails.adapter.api.rest.application.mapper;

import io.github.yaforster.trails.adapter.api.rest.model.ApplicationDefinitionDTO;
import io.github.yaforster.trails.app.GlobalMapperConfig;
import io.github.yaforster.trails.core.definition.ApplicationDefinition;
import org.mapstruct.Mapper;

@FunctionalInterface
@Mapper(config = GlobalMapperConfig.class)
public interface ApplicationDefinitionDTOMapper {

	ApplicationDefinition toDomain(ApplicationDefinitionDTO dto);

}
