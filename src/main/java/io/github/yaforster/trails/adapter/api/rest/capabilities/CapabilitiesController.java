package io.github.yaforster.trails.adapter.api.rest.capabilities;

import io.github.yaforster.trails.adapter.api.rest.capabilities.mapper.CapabilitiesMapper;
import io.github.yaforster.trails.adapter.api.rest.model.CapabilityDTO;
import io.github.yaforster.trails.app.services.CapabilitiesService;
import lombok.AllArgsConstructor;
import org.openapitools.api.CapabilitiesApi;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@AllArgsConstructor
@RestController
public class CapabilitiesController implements CapabilitiesApi {

	private final CapabilitiesMapper mapper;

	private final CapabilitiesService service;

	@Override
	public ResponseEntity<List<CapabilityDTO>> listCapabilities() {
		return ResponseEntity.ok(mapper.toDTOs(service.getCapabilities()));
	}

}
