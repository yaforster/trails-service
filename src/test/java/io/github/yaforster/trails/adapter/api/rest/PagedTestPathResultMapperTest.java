package io.github.yaforster.trails.adapter.api.rest;

import io.github.yaforster.trails.adapter.api.rest.common.mapper.PageMetadataMapper;
import io.github.yaforster.trails.adapter.api.rest.model.PagedTestPathResultDTO;
import io.github.yaforster.trails.adapter.api.rest.model.TestPathResultDTO;
import io.github.yaforster.trails.adapter.api.rest.testpath.mapper.PagedTestPathResultMapper;
import io.github.yaforster.trails.adapter.api.rest.testpath.mapper.TestPathResultDTOMapper;
import io.github.yaforster.trails.adapter.api.rest.testpath.model.TestPathResultModel;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.*;

class PagedTestPathResultMapperTest {

	@Test
	void mapsPagedAndSingleTestPathResultModelToDto() {
		TestPathResultDTOMapper testPathResultDTOMapper = mock(TestPathResultDTOMapper.class);
		TestPathResultModel model = new TestPathResultModel(3L);
		TestPathResultDTO itemDto = new TestPathResultDTO();
		itemDto.setId(3L);
		when(testPathResultDTOMapper.toDTO(model)).thenReturn(itemDto);

		PagedTestPathResultMapper mapper = new PagedTestPathResultMapper(testPathResultDTOMapper,
				new PageMetadataMapper());
		PagedTestPathResultDTO pagedDto = mapper.toPagedDto(RestMapperTestSupport.paged(List.of(model)));

		assertEquals(1, pagedDto.getItems().size());
		assertSame(itemDto, pagedDto.getItems().getFirst());
		assertEquals("/page/self", pagedDto.getLinks().get("self").getHref());
		assertSame(itemDto, mapper.toTestPathResultDto(model));
		verify(testPathResultDTOMapper, times(2)).toDTO(model);
	}

}
