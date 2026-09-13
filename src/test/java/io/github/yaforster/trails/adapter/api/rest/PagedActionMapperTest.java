package io.github.yaforster.trails.adapter.api.rest;

import io.github.yaforster.trails.adapter.api.rest.common.mapper.PageMetadataMapper;
import io.github.yaforster.trails.adapter.api.rest.action.mapper.ActionDTOMapper;
import io.github.yaforster.trails.adapter.api.rest.action.mapper.PagedActionMapper;
import io.github.yaforster.trails.adapter.api.rest.action.model.ActionModel;
import io.github.yaforster.trails.adapter.api.rest.model.ActionDTO;
import io.github.yaforster.trails.adapter.api.rest.model.PagedActionDTO;
import io.github.yaforster.trails.core.test.Action;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class PagedActionMapperTest {

	@Test
	void mapsPagedActionModelToDto() {
		ActionDTOMapper actionDTOMapper = mock(ActionDTOMapper.class);
		ActionModel actionModel = new ActionModel(2L, mock(Action.class));
		ActionDTO actionDTO = new ActionDTO();
		actionDTO.setActionID(1L);
		when(actionDTOMapper.toDTO(actionModel)).thenReturn(actionDTO);

		PagedActionMapper mapper = new PagedActionMapper(actionDTOMapper, new PageMetadataMapper());
		PagedActionDTO dto = mapper.toPagedDto(RestMapperTestSupport.paged(List.of(actionModel)));

		assertEquals(1, dto.getPage());
		assertEquals(2, dto.getSize());
		assertEquals(5, dto.getTotalElements());
		assertEquals(3, dto.getTotalPages());
		assertEquals(1, dto.getItems().size());
		assertSame(actionDTO, dto.getItems().getFirst());
		assertEquals("/page/self", dto.getLinks().get("self").getHref());
	}

}
