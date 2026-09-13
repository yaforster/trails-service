package io.github.yaforster.trails.adapter.api.rest.testrun.mapper;

import io.github.yaforster.trails.adapter.api.rest.model.TestPlanRunDefinitionDTO;
import io.github.yaforster.trails.app.GlobalMapperConfig;
import io.github.yaforster.trails.core.test.TestPlanRunDefinition;
import org.mapstruct.Mapper;

import java.util.Collections;

@FunctionalInterface
@Mapper(config = GlobalMapperConfig.class, imports = Collections.class)
public interface TestRunDefinitionMapper {

	TestPlanRunDefinition fromDTO(TestPlanRunDefinitionDTO dto);

}
