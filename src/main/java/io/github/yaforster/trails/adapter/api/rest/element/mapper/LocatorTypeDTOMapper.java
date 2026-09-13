package io.github.yaforster.trails.adapter.api.rest.element.mapper;

import io.github.yaforster.trails.adapter.api.rest.hateoas.HATEOASMapper;
import io.github.yaforster.trails.adapter.api.rest.model.LocatorTypeDTO;
import io.github.yaforster.trails.app.GlobalMapperConfig;
import io.github.yaforster.trails.core.test.action.element.LocatorType;
import org.mapstruct.Mapper;

@FunctionalInterface
@Mapper(config = GlobalMapperConfig.class)
public interface LocatorTypeDTOMapper extends HATEOASMapper {

	LocatorTypeDTO toDTO(LocatorType locatorType);

}
