package io.github.yaforster.trails.adapter.api.rest.testrun.mapper;

import io.github.yaforster.trails.adapter.api.rest.hateoas.HATEOASMapper;
import io.github.yaforster.trails.adapter.api.rest.model.PersistedTestRunResultDTO;
import io.github.yaforster.trails.adapter.api.rest.testrun.model.TestRunResultModel;
import io.github.yaforster.trails.app.GlobalMapperConfig;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@FunctionalInterface
@Mapper(config = GlobalMapperConfig.class)
public interface TestRunResultMapper extends HATEOASMapper {

	@BeanMapping(ignoreByDefault = true)
	@Mapping(target = "id", source = "id")
	@Mapping(target = "timestamp", source = "timestamp")
	@Mapping(target = "indicator", source = "indicator")
	@Mapping(target = "applicationId", source = "applicationId")
	@Mapping(target = "stageId", source = "stageId")
	@Mapping(target = "label", source = "label")
	@Mapping(target = "links", source = "links")
	PersistedTestRunResultDTO toDTO(TestRunResultModel result);

}
