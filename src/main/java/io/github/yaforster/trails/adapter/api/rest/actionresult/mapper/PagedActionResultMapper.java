package io.github.yaforster.trails.adapter.api.rest.actionresult.mapper;

import io.github.yaforster.trails.adapter.api.rest.actionresult.model.ActionResultModel;
import io.github.yaforster.trails.adapter.api.rest.common.mapper.PageMetadataMapper;
import io.github.yaforster.trails.adapter.api.rest.common.mapper.PagedContentMapper;
import io.github.yaforster.trails.adapter.api.rest.model.ActionResultDTO;
import io.github.yaforster.trails.adapter.api.rest.model.PagedActionResultDTO;
import lombok.AllArgsConstructor;
import org.springframework.hateoas.PagedModel;
import org.springframework.stereotype.Component;

@Component
@AllArgsConstructor
public class PagedActionResultMapper extends PagedContentMapper {

	private final ActionResultDTOMapper actionResultDTOMapper;

	private final PageMetadataMapper pageMetadataMapper;

	public PagedActionResultDTO toPagedDto(PagedModel<ActionResultModel> model) {
		PagedActionResultDTO dto = new PagedActionResultDTO();
		pageMetadataMapper.map(model, dto);

		dto.setItems(model.getContent().stream().map(actionResultDTOMapper::toDTO).toList());

		dto.setLinks(mapLinks(model.getLinks()));

		return dto;
	}

	public ActionResultDTO toActionResultDto(ActionResultModel model) {
		return actionResultDTOMapper.toDTO(model);
	}

}
