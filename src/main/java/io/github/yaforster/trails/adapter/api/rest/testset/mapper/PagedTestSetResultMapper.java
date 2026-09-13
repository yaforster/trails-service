package io.github.yaforster.trails.adapter.api.rest.testset.mapper;

import io.github.yaforster.trails.adapter.api.rest.common.mapper.PageMetadataMapper;
import io.github.yaforster.trails.adapter.api.rest.common.mapper.PagedContentMapper;
import io.github.yaforster.trails.adapter.api.rest.model.PagedTestSetResultDTO;
import io.github.yaforster.trails.adapter.api.rest.model.TestSetResultDTO;
import io.github.yaforster.trails.adapter.api.rest.testset.model.TestSetResultModel;
import lombok.AllArgsConstructor;
import org.springframework.hateoas.PagedModel;
import org.springframework.stereotype.Component;

@Component
@AllArgsConstructor
public class PagedTestSetResultMapper extends PagedContentMapper {

	private final TestSetResultDTOMapper testSetResultDTOMapper;

	private final PageMetadataMapper pageMetadataMapper;

	public PagedTestSetResultDTO toPagedDto(PagedModel<TestSetResultModel> model) {
		PagedTestSetResultDTO dto = new PagedTestSetResultDTO();
		pageMetadataMapper.map(model, dto);

		dto.setItems(model.getContent().stream().map(testSetResultDTOMapper::toDTO).toList());

		dto.setLinks(mapLinks(model.getLinks()));

		return dto;
	}

	public TestSetResultDTO toTestSetResultDto(TestSetResultModel model) {
		return testSetResultDTOMapper.toDTO(model);
	}

}
