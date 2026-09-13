package io.github.yaforster.trails.adapter.api.rest.element.mapper;

import io.github.yaforster.trails.adapter.api.rest.element.model.ElementModel;
import io.github.yaforster.trails.adapter.api.rest.hateoas.HATEOASMapper;
import io.github.yaforster.trails.adapter.api.rest.model.ElementDTO;
import io.github.yaforster.trails.app.GlobalMapperConfig;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@FunctionalInterface
@Mapper(config = GlobalMapperConfig.class)
public interface ElementDTOMapper extends HATEOASMapper {

	@BeanMapping(ignoreByDefault = true)
	@Mapping(target = "links", source = "links")
	@Mapping(target = "id", source = "id")
	@Mapping(target = "type", source = "type")
	@Mapping(target = "label", source = "label")
	@Mapping(target = "locatorString", source = "locatorString")
	@Mapping(target = "locatorType", source = "locatorType")
	@Mapping(target = "retired", source = "retired")
	ElementDTO toDTO(ElementModel model);

}
