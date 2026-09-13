package io.github.yaforster.trails.adapter.api.rest.testrun.mapper;

import io.github.yaforster.trails.adapter.api.rest.hateoas.HATEOASMapper;
import io.github.yaforster.trails.adapter.api.rest.model.TestRunStatisticsDTO;
import io.github.yaforster.trails.adapter.api.rest.testrun.model.TestRunStatisticsModel;
import io.github.yaforster.trails.app.GlobalMapperConfig;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@FunctionalInterface
@Mapper(config = GlobalMapperConfig.class)
public interface TestRunStatisticsMapper extends HATEOASMapper {

	@Mapping(target = "links", source = "links")
	TestRunStatisticsDTO toDTO(TestRunStatisticsModel model);

}
