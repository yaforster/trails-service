package io.github.yaforster.trails.adapter.api.rest.stage.mapper;

import io.github.yaforster.trails.adapter.api.rest.hateoas.HATEOASMapper;
import io.github.yaforster.trails.adapter.api.rest.model.StageDTO;
import io.github.yaforster.trails.adapter.api.rest.stage.model.StageModel;
import io.github.yaforster.trails.app.GlobalMapperConfig;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@FunctionalInterface
@Mapper(config = GlobalMapperConfig.class)
public interface StageDTOMapper extends HATEOASMapper {

	@BeanMapping(ignoreByDefault = true)
	@Mapping(target = "id", source = "id")
	@Mapping(target = "label", source = "label")
	@Mapping(target = "url", source = "url")
	@Mapping(target = "retired", source = "retired")
	@Mapping(target = "links", source = "links")
	StageDTO toDTO(StageModel model);

}
