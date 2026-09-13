package io.github.yaforster.trails.adapter.api.rest.testpath.mapper;

import io.github.yaforster.trails.adapter.api.rest.hateoas.HATEOASMapper;
import io.github.yaforster.trails.adapter.api.rest.model.TestPathResultDTO;
import io.github.yaforster.trails.adapter.api.rest.testpath.model.TestPathResultModel;
import io.github.yaforster.trails.app.GlobalMapperConfig;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@FunctionalInterface
@Mapper(config = GlobalMapperConfig.class)
public interface TestPathResultDTOMapper extends HATEOASMapper {

	@Mapping(target = "links", source = "links")
	TestPathResultDTO toDTO(TestPathResultModel model);

}
