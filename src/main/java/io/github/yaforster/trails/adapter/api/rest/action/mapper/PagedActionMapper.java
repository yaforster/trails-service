package io.github.yaforster.trails.adapter.api.rest.action.mapper;

import io.github.yaforster.trails.adapter.api.rest.action.model.ActionModel;
import io.github.yaforster.trails.adapter.api.rest.common.mapper.PageMetadataMapper;
import io.github.yaforster.trails.adapter.api.rest.common.mapper.PagedContentMapper;
import io.github.yaforster.trails.adapter.api.rest.model.PagedActionDTO;
import lombok.AllArgsConstructor;
import org.springframework.hateoas.PagedModel;
import org.springframework.stereotype.Component;

@Component
@AllArgsConstructor
public class PagedActionMapper extends PagedContentMapper {

	private final ActionDTOMapper actionDTOMapper;

	private final PageMetadataMapper pageMetadataMapper;

	public PagedActionDTO toPagedDto(PagedModel<ActionModel> model) {
		PagedActionDTO dto = new PagedActionDTO();
		pageMetadataMapper.map(model, dto);

		dto.setItems(model.getContent().stream().map(actionDTOMapper::toDTO).toList());

		dto.setLinks(mapLinks(model.getLinks()));

		return dto;
	}

}
