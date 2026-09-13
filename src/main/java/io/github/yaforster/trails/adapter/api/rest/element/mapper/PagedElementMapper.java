package io.github.yaforster.trails.adapter.api.rest.element.mapper;

import io.github.yaforster.trails.adapter.api.rest.common.mapper.PageMetadataMapper;
import io.github.yaforster.trails.adapter.api.rest.common.mapper.PagedContentMapper;
import io.github.yaforster.trails.adapter.api.rest.element.model.ElementModel;
import io.github.yaforster.trails.adapter.api.rest.model.ElementDTO;
import io.github.yaforster.trails.adapter.api.rest.model.PagedElementDTO;
import lombok.AllArgsConstructor;
import org.springframework.hateoas.PagedModel;
import org.springframework.stereotype.Component;

@Component
@AllArgsConstructor
public class PagedElementMapper extends PagedContentMapper {

	private final PageMetadataMapper pageMetadataMapper;

	public PagedElementDTO toPagedDto(PagedModel<ElementModel> model) {
		PagedElementDTO dto = new PagedElementDTO();
		pageMetadataMapper.map(model, dto);

		dto.setItems(model.getContent().stream().map(this::toElementDto).toList());

		dto.setLinks(mapLinks(model.getLinks()));

		return dto;
	}

	private ElementDTO toElementDto(ElementModel model) {
		ElementDTO dto = new ElementDTO();
		dto.setId(model.getId());
		dto.setLabel(model.getLabel());
		dto.setLinks(mapLinks(model.getLinks()));
		return dto;
	}

}
