package io.github.yaforster.trails.adapter.api.rest.capabilities.mapper;

import io.github.yaforster.trails.adapter.api.rest.model.CapabilityDTO;
import io.github.yaforster.trails.app.GlobalMapperConfig;
import io.github.yaforster.trails.core.Capability;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(config = GlobalMapperConfig.class)
public interface CapabilitiesMapper {

	List<CapabilityDTO> toDTOs(List<Capability> capabilities);

	@Mapping(target = "name", source = "label")
	CapabilityDTO toDTO(Capability capability);

}
