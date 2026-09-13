package io.github.yaforster.trails.adapter.api.rest.stage.mapper;

import io.github.yaforster.trails.adapter.api.rest.model.PersistedStageDTO;
import io.github.yaforster.trails.app.GlobalMapperConfig;
import io.github.yaforster.trails.core.persisted.PersistedStage;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.net.URI;

@FunctionalInterface
@Mapper(config = GlobalMapperConfig.class)
public interface PersistedStageDTOMapper {

	@Mapping(target = "links", ignore = true)
	PersistedStageDTO toDTO(PersistedStage model);

	default URI map(String url) {
		return url == null ? null : URI.create(url);
	}

}
