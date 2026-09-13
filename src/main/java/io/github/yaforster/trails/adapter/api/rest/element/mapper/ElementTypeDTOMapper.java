package io.github.yaforster.trails.adapter.api.rest.element.mapper;

import io.github.yaforster.trails.adapter.api.rest.hateoas.HATEOASMapper;
import io.github.yaforster.trails.adapter.api.rest.model.ElementTypeDTO;
import io.github.yaforster.trails.app.GlobalMapperConfig;
import io.github.yaforster.trails.core.data.ElementType;
import org.mapstruct.Mapper;

@FunctionalInterface
@Mapper(config = GlobalMapperConfig.class)
public interface ElementTypeDTOMapper extends HATEOASMapper {

	ElementTypeDTO toDTO(ElementType type);

}