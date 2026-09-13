package io.github.yaforster.trails.adapter.api.rest;

import io.github.yaforster.trails.adapter.api.rest.artifact.mapper.ArtifactDTOMapper;
import io.github.yaforster.trails.adapter.api.rest.artifact.model.ArtifactModel;
import io.github.yaforster.trails.adapter.api.rest.model.ArtifactDTO;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;
import org.springframework.hateoas.Link;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ArtifactDTOMapperTest {

	private final ArtifactDTOMapper mapper = Mappers.getMapper(ArtifactDTOMapper.class);

	@Test
	void mapsArtifactModelToDto() {
		ArtifactModel model = new ArtifactModel(5L, ArtifactModel.Type.PDF, "artifact.pdf");
		model.add(Link.of("/artifacts/5", "self"));

		ArtifactDTO dto = mapper.toDTO(model);

		assertEquals(5L, dto.getId());
		assertEquals(ArtifactDTO.TypeEnum.PDF, dto.getType());
		assertEquals("artifact.pdf", dto.getFilename());
		assertEquals("/artifacts/5", dto.getLinks().get("self").getHref());
	}

}
