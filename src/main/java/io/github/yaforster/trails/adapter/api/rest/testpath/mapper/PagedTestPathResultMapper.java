package io.github.yaforster.trails.adapter.api.rest.testpath.mapper;

import io.github.yaforster.trails.adapter.api.rest.common.mapper.PageMetadataMapper;
import io.github.yaforster.trails.adapter.api.rest.common.mapper.PagedContentMapper;
import io.github.yaforster.trails.adapter.api.rest.model.PagedTestPathResultDTO;
import io.github.yaforster.trails.adapter.api.rest.model.TestPathResultDTO;
import io.github.yaforster.trails.adapter.api.rest.testpath.model.TestPathResultModel;
import lombok.AllArgsConstructor;
import org.springframework.hateoas.PagedModel;
import org.springframework.stereotype.Component;

@Component
@AllArgsConstructor
public class PagedTestPathResultMapper extends PagedContentMapper {

	private final TestPathResultDTOMapper testPathResultDTOMapper;

	private final PageMetadataMapper pageMetadataMapper;

	public PagedTestPathResultDTO toPagedDto(PagedModel<TestPathResultModel> model) {
		PagedTestPathResultDTO dto = new PagedTestPathResultDTO();
		pageMetadataMapper.map(model, dto);

		dto.setItems(model.getContent().stream().map(testPathResultDTOMapper::toDTO).toList());

		dto.setLinks(mapLinks(model.getLinks()));

		return dto;
	}

	public TestPathResultDTO toTestPathResultDto(TestPathResultModel model) {
		return testPathResultDTOMapper.toDTO(model);
	}

}
