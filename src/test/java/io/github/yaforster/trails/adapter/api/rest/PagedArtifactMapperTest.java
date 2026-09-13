package io.github.yaforster.trails.adapter.api.rest;

import io.github.yaforster.trails.adapter.api.rest.common.mapper.PageMetadataMapper;
import io.github.yaforster.trails.adapter.api.rest.artifact.mapper.ArtifactDTOMapper;
import io.github.yaforster.trails.adapter.api.rest.artifact.mapper.PagedArtifactMapper;
import io.github.yaforster.trails.adapter.api.rest.artifact.model.ArtifactModel;
import io.github.yaforster.trails.adapter.api.rest.model.ArtifactDTO;
import io.github.yaforster.trails.adapter.api.rest.model.PagedArtifactDTO;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class PagedArtifactMapperTest {

	@Test
	void mapsPagedArtifactModelToDto() {
		ArtifactDTOMapper artifactDTOMapper = mock(ArtifactDTOMapper.class);
		ArtifactModel model = new ArtifactModel(1L, ArtifactModel.Type.SCREENSHOT, "a.png");
		ArtifactDTO itemDto = new ArtifactDTO();
		itemDto.setId(1L);
		when(artifactDTOMapper.toDTO(model)).thenReturn(itemDto);

		PagedArtifactMapper mapper = new PagedArtifactMapper(artifactDTOMapper, new PageMetadataMapper());
		PagedArtifactDTO dto = mapper.toPagedDto(RestMapperTestSupport.paged(List.of(model)));

		assertEquals(1, dto.getItems().size());
		assertSame(itemDto, dto.getItems().getFirst());
		assertEquals("/page/self", dto.getLinks().get("self").getHref());
	}

}
