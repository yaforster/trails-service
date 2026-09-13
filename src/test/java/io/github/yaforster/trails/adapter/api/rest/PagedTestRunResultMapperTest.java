package io.github.yaforster.trails.adapter.api.rest;

import io.github.yaforster.trails.adapter.api.rest.common.mapper.PageMetadataMapper;
import io.github.yaforster.trails.adapter.api.rest.model.PagedTestRunDTO;
import io.github.yaforster.trails.adapter.api.rest.model.PersistedTestRunResultDTO;
import io.github.yaforster.trails.adapter.api.rest.testrun.mapper.PagedTestRunResultMapper;
import io.github.yaforster.trails.adapter.api.rest.testrun.mapper.TestRunResultMapper;
import io.github.yaforster.trails.adapter.api.rest.testrun.model.TestRunResultModel;
import org.junit.jupiter.api.Test;

import java.time.OffsetDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.*;

class PagedTestRunResultMapperTest {

	@Test
	void mapsPagedAndSingleTestRunResultModelToDto() {
		TestRunResultMapper testRunResultMapper = mock(TestRunResultMapper.class);
		TestRunResultModel model = new TestRunResultModel(6L, OffsetDateTime.now(), null, 1L, 2L, "run");
		PersistedTestRunResultDTO itemDto = new PersistedTestRunResultDTO();
		itemDto.setId(6L);
		when(testRunResultMapper.toDTO(model)).thenReturn(itemDto);

		PagedTestRunResultMapper mapper = new PagedTestRunResultMapper(testRunResultMapper, new PageMetadataMapper());
		PagedTestRunDTO pagedDto = mapper.toPagedDto(RestMapperTestSupport.paged(List.of(model)));

		assertEquals(1, pagedDto.getItems().size());
		assertSame(itemDto, pagedDto.getItems().getFirst());
		assertEquals("/page/self", pagedDto.getLinks().get("self").getHref());
		assertSame(itemDto, mapper.toPersistedTestRunResultDto(model));
		verify(testRunResultMapper, times(2)).toDTO(model);
	}

}
