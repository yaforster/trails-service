package io.github.yaforster.trails.adapter.api.rest.testplan.mapper;

import io.github.yaforster.trails.adapter.api.rest.common.mapper.PageMetadataMapper;
import io.github.yaforster.trails.adapter.api.rest.common.mapper.PagedContentMapper;
import io.github.yaforster.trails.adapter.api.rest.model.PagedTestPlansDTO;
import io.github.yaforster.trails.adapter.api.rest.model.TestPlanDTO;
import lombok.AllArgsConstructor;
import org.springframework.hateoas.PagedModel;
import org.springframework.stereotype.Component;

@Component
@AllArgsConstructor
public class PagedTestPlanMapper extends PagedContentMapper {

	private final PageMetadataMapper pageMetadataMapper;

	public PagedTestPlansDTO toPagedDto(PagedModel<TestPlanModel> model) {
		PagedTestPlansDTO dto = new PagedTestPlansDTO();
		pageMetadataMapper.map(model, dto);

		dto.setItems(model.getContent().stream().map(this::toTestPlanDto).toList());

		dto.setLinks(mapLinks(model.getLinks()));

		return dto;
	}

	private TestPlanDTO toTestPlanDto(TestPlanModel model) {
		TestPlanDTO dto = new TestPlanDTO();
		dto.setId(model.getId());
		dto.setLabel(model.getLabel());
		dto.setLinks(mapLinks(model.getLinks()));
		return dto;
	}

}
