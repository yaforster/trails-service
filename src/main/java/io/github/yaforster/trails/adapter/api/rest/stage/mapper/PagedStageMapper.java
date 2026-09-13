package io.github.yaforster.trails.adapter.api.rest.stage.mapper;

import io.github.yaforster.trails.adapter.api.rest.common.mapper.PageMetadataMapper;
import io.github.yaforster.trails.adapter.api.rest.common.mapper.PagedContentMapper;
import io.github.yaforster.trails.adapter.api.rest.model.PagedStageDTO;
import io.github.yaforster.trails.adapter.api.rest.model.StageDTO;
import io.github.yaforster.trails.adapter.api.rest.stage.model.StageModel;
import lombok.AllArgsConstructor;
import org.springframework.hateoas.PagedModel;
import org.springframework.stereotype.Component;

@Component
@AllArgsConstructor
public class PagedStageMapper extends PagedContentMapper {

	private final PageMetadataMapper pageMetadataMapper;

	public PagedStageDTO toPagedDto(PagedModel<StageModel> model) {
		PagedStageDTO dto = new PagedStageDTO();
		pageMetadataMapper.map(model, dto);

		dto.setItems(model.getContent().stream().map(this::toStageDto).toList());

		dto.setLinks(mapLinks(model.getLinks()));

		return dto;
	}

	private StageDTO toStageDto(StageModel model) {
		StageDTO dto = new StageDTO();
		dto.setId(model.getId());
		dto.setLabel(model.getLabel());
		dto.setLinks(mapLinks(model.getLinks()));
		return dto;
	}

}
