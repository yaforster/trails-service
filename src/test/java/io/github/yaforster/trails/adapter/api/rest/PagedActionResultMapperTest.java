package io.github.yaforster.trails.adapter.api.rest;

import io.github.yaforster.trails.adapter.api.rest.common.mapper.PageMetadataMapper;
import io.github.yaforster.trails.adapter.api.rest.actionresult.mapper.ActionResultDTOMapper;
import io.github.yaforster.trails.adapter.api.rest.actionresult.mapper.PagedActionResultMapper;
import io.github.yaforster.trails.adapter.api.rest.actionresult.model.ActionResultModel;
import io.github.yaforster.trails.adapter.api.rest.model.ActionResultDTO;
import io.github.yaforster.trails.adapter.api.rest.model.PagedActionResultDTO;
import io.github.yaforster.trails.core.persisted.ActionResultType;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.*;

class PagedActionResultMapperTest {

	@Test
	void mapsPagedAndSingleActionResultModelToDto() {
		ActionResultDTOMapper actionResultDTOMapper = mock(ActionResultDTOMapper.class);
		ActionResultModel model = new ActionResultModel(1L, 2L, "label", "result", ActionResultType.SUCCESS, null);
		ActionResultDTO itemDto = new ActionResultDTO();
		itemDto.setId(1L);
		when(actionResultDTOMapper.toDTO(model)).thenReturn(itemDto);

		PagedActionResultMapper mapper = new PagedActionResultMapper(actionResultDTOMapper, new PageMetadataMapper());
		PagedActionResultDTO pagedDto = mapper.toPagedDto(RestMapperTestSupport.paged(List.of(model)));

		assertEquals(1, pagedDto.getItems().size());
		assertSame(itemDto, pagedDto.getItems().getFirst());
		assertEquals("/page/self", pagedDto.getLinks().get("self").getHref());
		assertSame(itemDto, mapper.toActionResultDto(model));
		verify(actionResultDTOMapper, times(2)).toDTO(model);
	}

}
