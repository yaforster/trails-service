package io.github.yaforster.trails.adapter.api.rest.action.mapper;

import io.github.yaforster.trails.adapter.api.rest.action.mapper.ActionMapper;
import io.github.yaforster.trails.adapter.api.rest.action.mapper.ActionDTOMapper;
import io.github.yaforster.trails.adapter.api.rest.action.model.ActionModel;
import io.github.yaforster.trails.adapter.api.rest.model.ActionDTO;
import io.github.yaforster.trails.core.test.Action;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ActionDTOMapperTest {

	private final ActionMapper actionMapper = mock(ActionMapper.class);

	private final ActionDTOMapper mapper = new ActionDTOMapper(actionMapper);

	@Test
	void mapsActionModelToDto() {
		Action action = mock(Action.class);
		ActionModel model = new ActionModel(2L, 10L, 20L, action);
		ActionDTO mappedAction = new ActionDTO();
		mappedAction.setActionID(1L);
		mappedAction.setLabel("label");
		when(actionMapper.toDTO(action, 10L, 20L)).thenReturn(mappedAction);

		ActionDTO dto = mapper.toDTO(model);

		assertSame(mappedAction, dto);
		assertEquals(2L, dto.getReferenceID());
	}

}
