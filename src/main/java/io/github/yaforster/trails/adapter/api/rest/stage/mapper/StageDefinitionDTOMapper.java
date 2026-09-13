package io.github.yaforster.trails.adapter.api.rest.stage.mapper;

import io.github.yaforster.trails.adapter.api.rest.model.StageDefinitionDTO;
import io.github.yaforster.trails.app.GlobalMapperConfig;
import io.github.yaforster.trails.core.definition.StageDefinition;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.net.URI;
import java.util.Collections;

@FunctionalInterface
@Mapper(config = GlobalMapperConfig.class, imports = Collections.class)
public interface StageDefinitionDTOMapper {

	@Mapping(target = "availableElements", expression = "java(Collections.emptyList())")
	StageDefinition toDomain(StageDefinitionDTO dto);

	default String map(URI uri) {
		return uri == null ? null : uri.toString();
	}

}
