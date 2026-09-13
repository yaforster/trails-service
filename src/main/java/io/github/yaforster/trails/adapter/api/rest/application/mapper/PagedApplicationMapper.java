package io.github.yaforster.trails.adapter.api.rest.application.mapper;

import io.github.yaforster.trails.adapter.api.rest.application.model.ApplicationModel;
import io.github.yaforster.trails.adapter.api.rest.common.mapper.PageMetadataMapper;
import io.github.yaforster.trails.adapter.api.rest.common.mapper.PagedContentMapper;
import io.github.yaforster.trails.adapter.api.rest.model.ApplicationDTO;
import io.github.yaforster.trails.adapter.api.rest.model.PagedApplicationDTO;
import lombok.AllArgsConstructor;
import org.springframework.hateoas.PagedModel;
import org.springframework.stereotype.Component;

@Component
@AllArgsConstructor
public class PagedApplicationMapper extends PagedContentMapper {

	private final PageMetadataMapper pageMetadataMapper;

	public PagedApplicationDTO toPagedDto(PagedModel<ApplicationModel> model) {
		PagedApplicationDTO dto = new PagedApplicationDTO();
		pageMetadataMapper.map(model, dto);

		dto.setItems(model.getContent().stream().map(this::toApplicationDto).toList());

		dto.setLinks(mapLinks(model.getLinks()));

		return dto;
	}

	private ApplicationDTO toApplicationDto(ApplicationModel model) {
		ApplicationDTO dto = new ApplicationDTO();
		dto.setId(model.getId());
		dto.setLabel(model.getLabel());
		dto.setLinks(mapLinks(model.getLinks()));
		return dto;
	}

}
