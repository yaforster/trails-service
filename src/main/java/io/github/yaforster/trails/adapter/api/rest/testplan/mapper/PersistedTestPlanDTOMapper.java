package io.github.yaforster.trails.adapter.api.rest.testplan.mapper;

import io.github.yaforster.trails.adapter.api.rest.hateoas.HATEOASMapper;
import io.github.yaforster.trails.adapter.api.rest.model.PersistedTestPlanDTO;
import io.github.yaforster.trails.adapter.api.rest.testplan.model.PersistedTestPlanModel;
import io.github.yaforster.trails.app.GlobalMapperConfig;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@FunctionalInterface
@Mapper(config = GlobalMapperConfig.class)
public interface PersistedTestPlanDTOMapper extends HATEOASMapper {

	@Mapping(target = "links", source = "links")
	PersistedTestPlanDTO toDTO(PersistedTestPlanModel model);

}
