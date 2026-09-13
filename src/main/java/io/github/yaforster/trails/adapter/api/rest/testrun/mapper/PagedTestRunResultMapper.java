package io.github.yaforster.trails.adapter.api.rest.testrun.mapper;

import io.github.yaforster.trails.adapter.api.rest.common.mapper.PageMetadataMapper;
import io.github.yaforster.trails.adapter.api.rest.common.mapper.PagedContentMapper;
import io.github.yaforster.trails.adapter.api.rest.model.PagedTestRunDTO;
import io.github.yaforster.trails.adapter.api.rest.model.PersistedTestRunResultDTO;
import io.github.yaforster.trails.adapter.api.rest.testrun.model.TestRunResultModel;
import lombok.AllArgsConstructor;
import org.springframework.hateoas.PagedModel;
import org.springframework.stereotype.Component;

@Component
@AllArgsConstructor
public class PagedTestRunResultMapper extends PagedContentMapper {

	private final TestRunResultMapper testRunResultMapper;

	private final PageMetadataMapper pageMetadataMapper;

	public PagedTestRunDTO toPagedDto(PagedModel<TestRunResultModel> model) {
		PagedTestRunDTO dto = new PagedTestRunDTO();
		pageMetadataMapper.map(model, dto);

		dto.setItems(model.getContent().stream().map(testRunResultMapper::toDTO).toList());

		dto.setLinks(mapLinks(model.getLinks()));

		return dto;
	}

	public PersistedTestRunResultDTO toPersistedTestRunResultDto(TestRunResultModel model) {
		return testRunResultMapper.toDTO(model);
	}

}
