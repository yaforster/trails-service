package io.github.yaforster.trails.adapter.api.rest.element.mapper;

import io.github.yaforster.trails.adapter.api.rest.model.ElementDefinitionDTO;
import io.github.yaforster.trails.app.GlobalMapperConfig;
import io.github.yaforster.trails.core.definition.ElementDefinition;
import org.mapstruct.Mapper;

import java.util.Collections;

@FunctionalInterface
@Mapper(config = GlobalMapperConfig.class, imports = Collections.class)
public interface ElementDefinitionDTOMapper {

	ElementDefinition toDomain(ElementDefinitionDTO dto);

}
