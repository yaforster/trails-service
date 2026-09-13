package io.github.yaforster.trails.adapter.api.rest;

import io.github.yaforster.trails.adapter.api.rest.common.mapper.PageMetadataMapper;
import io.github.yaforster.trails.adapter.api.rest.model.PagedTestSetResultDTO;
import io.github.yaforster.trails.adapter.api.rest.model.TestSetResultDTO;
import io.github.yaforster.trails.adapter.api.rest.testset.mapper.PagedTestSetResultMapper;
import io.github.yaforster.trails.adapter.api.rest.testset.mapper.TestSetResultDTOMapper;
import io.github.yaforster.trails.adapter.api.rest.testset.model.TestSetResultModel;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.*;

class PagedTestSetResultMapperTest {

	@Test
	void mapsPagedAndSingleTestSetResultModelToDto() {
		TestSetResultDTOMapper testSetResultDTOMapper = mock(TestSetResultDTOMapper.class);
		TestSetResultModel model = new TestSetResultModel(8L, 10, null, "label", Optional.empty());
		TestSetResultDTO itemDto = new TestSetResultDTO();
		itemDto.setId(8L);
		when(testSetResultDTOMapper.toDTO(model)).thenReturn(itemDto);

		PagedTestSetResultMapper mapper = new PagedTestSetResultMapper(testSetResultDTOMapper,
				new PageMetadataMapper());
		PagedTestSetResultDTO pagedDto = mapper.toPagedDto(RestMapperTestSupport.paged(List.of(model)));

		assertEquals(1, pagedDto.getItems().size());
		assertSame(itemDto, pagedDto.getItems().getFirst());
		assertEquals("/page/self", pagedDto.getLinks().get("self").getHref());
		assertSame(itemDto, mapper.toTestSetResultDto(model));
		verify(testSetResultDTOMapper, times(2)).toDTO(model);
	}

}
