package io.github.yaforster.trails.adapter.api.rest.application.mapper;

import io.github.yaforster.trails.adapter.api.rest.application.model.ApplicationModel;
import io.github.yaforster.trails.adapter.api.rest.hateoas.HATEOASMapper;
import io.github.yaforster.trails.adapter.api.rest.model.ApplicationDTO;
import io.github.yaforster.trails.app.GlobalMapperConfig;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@FunctionalInterface
@Mapper(config = GlobalMapperConfig.class)
public interface ApplicationDTOMapper extends HATEOASMapper {

	@BeanMapping(ignoreByDefault = true)
	@Mapping(target = "id", source = "id")
	@Mapping(target = "label", source = "label")
	@Mapping(target = "retired", source = "retired")
	@Mapping(target = "links", source = "links")
	ApplicationDTO toDTO(ApplicationModel model);

}
