package io.github.yaforster.trails.adapter.api.rest.artifact.mapper;

import io.github.yaforster.trails.adapter.api.rest.artifact.model.ArtifactModel;
import io.github.yaforster.trails.adapter.api.rest.common.mapper.PageMetadataMapper;
import io.github.yaforster.trails.adapter.api.rest.common.mapper.PagedContentMapper;
import io.github.yaforster.trails.adapter.api.rest.model.PagedArtifactDTO;
import lombok.AllArgsConstructor;
import org.springframework.hateoas.PagedModel;
import org.springframework.stereotype.Component;

@Component
@AllArgsConstructor
public class PagedArtifactMapper extends PagedContentMapper {

	private final ArtifactDTOMapper artifactDTOMapper;

	private final PageMetadataMapper pageMetadataMapper;

	public PagedArtifactDTO toPagedDto(PagedModel<ArtifactModel> model) {
		PagedArtifactDTO dto = new PagedArtifactDTO();
		pageMetadataMapper.map(model, dto);

		dto.setItems(model.getContent().stream().map(artifactDTOMapper::toDTO).toList());

		dto.setLinks(mapLinks(model.getLinks()));

		return dto;
	}

}
