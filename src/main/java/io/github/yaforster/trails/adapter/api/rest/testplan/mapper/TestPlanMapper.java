package io.github.yaforster.trails.adapter.api.rest.testplan.mapper;

import io.github.yaforster.trails.adapter.api.rest.hateoas.HATEOASMapper;
import io.github.yaforster.trails.adapter.api.rest.model.TestPlanDTO;
import io.github.yaforster.trails.app.GlobalMapperConfig;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@FunctionalInterface
@Mapper(config = GlobalMapperConfig.class)
public interface TestPlanMapper extends HATEOASMapper {

	@BeanMapping(ignoreByDefault = true)
	@Mapping(target = "id", source = "id")
	@Mapping(target = "label", source = "label")
	@Mapping(target = "retired", source = "retired")
	@Mapping(target = "groups", source = "groups")
	@Mapping(target = "links", source = "links")
	TestPlanDTO toDTO(TestPlanModel testPlanModel);

}
