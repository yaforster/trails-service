package io.github.yaforster.trails.adapter.api.rest.testset.mapper;

import io.github.yaforster.trails.adapter.api.rest.hateoas.HATEOASMapper;
import io.github.yaforster.trails.adapter.api.rest.model.TestCaseResultDTO;
import io.github.yaforster.trails.adapter.api.rest.model.TestSetResultDTO;
import io.github.yaforster.trails.adapter.api.rest.testset.model.TestCaseResultModel;
import io.github.yaforster.trails.adapter.api.rest.testset.model.TestSetResultModel;
import io.github.yaforster.trails.app.GlobalMapperConfig;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.Optional;

@Mapper(config = GlobalMapperConfig.class)
public interface TestSetResultDTOMapper extends HATEOASMapper {

	@BeanMapping(ignoreByDefault = true)
	@Mapping(target = "id", source = "id")
	@Mapping(target = "totalRunTime", source = "totalRunTime")
	@Mapping(target = "browserToRunIn", source = "browserToRunIn")
	@Mapping(target = "testPlanLabel", source = "testPlanLabel")
	@Mapping(target = "testCaseResult", source = "testCaseResult")
	@Mapping(target = "links", source = "links")
	TestSetResultDTO toDTO(TestSetResultModel model);

	@Mapping(target = "links", ignore = true)
	TestCaseResultDTO toDTO(TestCaseResultModel model);

	default TestCaseResultDTO map(Optional<TestCaseResultModel> model) {
		return model.map(this::toDTO).orElse(null);
	}

}
