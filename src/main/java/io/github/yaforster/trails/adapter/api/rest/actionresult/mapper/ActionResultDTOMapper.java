package io.github.yaforster.trails.adapter.api.rest.actionresult.mapper;

import io.github.yaforster.trails.adapter.api.rest.actionresult.model.ActionResultModel;
import io.github.yaforster.trails.adapter.api.rest.hateoas.HATEOASMapper;
import io.github.yaforster.trails.adapter.api.rest.model.ActionResultDTO;
import io.github.yaforster.trails.app.GlobalMapperConfig;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@FunctionalInterface
@Mapper(config = GlobalMapperConfig.class)
public interface ActionResultDTOMapper extends HATEOASMapper {

	@BeanMapping(ignoreByDefault = true)
	@Mapping(target = "id", source = "id")
	@Mapping(target = "actionID", source = "actionID")
	@Mapping(target = "label", source = "label")
	@Mapping(target = "resultMessage", source = "resultMessage")
	@Mapping(target = "resultType", source = "resultType")
	@Mapping(target = "exceptionMessageFromAction", source = "exceptionMessageFromAction")
	@Mapping(target = "links", source = "links")
	ActionResultDTO toDTO(ActionResultModel model);

}
