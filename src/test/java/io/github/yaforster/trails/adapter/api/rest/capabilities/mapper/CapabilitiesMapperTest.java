package io.github.yaforster.trails.adapter.api.rest.capabilities.mapper;

import io.github.yaforster.trails.adapter.api.rest.model.CapabilityDTO;
import io.github.yaforster.trails.adapter.api.rest.model.CapabilityNameDTO;
import io.github.yaforster.trails.core.Capability;
import io.github.yaforster.trails.core.CapabilityLabel;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CapabilitiesMapperTest {

	private final CapabilitiesMapper mapper = Mappers.getMapper(CapabilitiesMapper.class);

	@Test
	void toDTO_mapsCapabilityFields() {
		Capability capability = new Capability(CapabilityLabel.FILE_DOWNLOAD_TIMEOUT_MILLIS,
				"Download timeout in milliseconds", "5000");

		CapabilityDTO dto = mapper.toDTO(capability);

		assertEquals(CapabilityNameDTO.FILE_DOWNLOAD_TIMEOUT_MILLIS, dto.getName());
		assertEquals("Download timeout in milliseconds", dto.getDescription());
		assertEquals("5000", dto.getValue());
	}

	@Test
	void toDTO_usesDefaultCapabilityDescription_whenCapabilityWasCreatedFromLabelAndValue() {
		Capability capability = new Capability(CapabilityLabel.USER_FEATURES_ACTIVE, "true");

		CapabilityDTO dto = mapper.toDTO(capability);

		assertEquals(CapabilityNameDTO.USER_FEATURES_ACTIVE, dto.getName());
		assertEquals(CapabilityLabel.USER_FEATURES_ACTIVE.getDescription(), dto.getDescription());
		assertEquals("true", dto.getValue());
	}

	@Test
	void toDTOs_mapsCapabilitiesInOrder() {
		List<Capability> capabilities = List.of(new Capability(CapabilityLabel.USER_IMAGE_MAX_WIDTH, "1920"),
				new Capability(CapabilityLabel.SUPPORTED_IMAGE_FORMATS, "png,jpg"));

		List<CapabilityDTO> result = mapper.toDTOs(capabilities);

		assertEquals(2, result.size());
		assertEquals(CapabilityNameDTO.USER_IMAGE_MAX_WIDTH, result.getFirst().getName());
		assertEquals("1920", result.getFirst().getValue());
		assertEquals(CapabilityNameDTO.SUPPORTED_IMAGE_FORMATS, result.get(1).getName());
		assertEquals("png,jpg", result.get(1).getValue());
	}

	@Test
	void toDTOs_mapsEmptyListToEmptyList() {
		assertTrue(mapper.toDTOs(List.of()).isEmpty());
	}

	@Test
	void toDTO_returnsNull_whenCapabilityIsNull() {
		assertNull(mapper.toDTO(null));
	}

	@Test
	void toDTOs_returnsNull_whenCapabilitiesAreNull() {
		assertNull(mapper.toDTOs(null));
	}

}
