package io.github.yaforster.trails.adapter.api.rest.action.mapper;

import io.github.yaforster.trails.adapter.api.rest.common.mapper.PositionMapper;
import io.github.yaforster.trails.adapter.api.rest.model.ActionDTO;
import io.github.yaforster.trails.adapter.api.rest.model.ActionDefinitionDTO;
import io.github.yaforster.trails.core.test.Action;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

@AllArgsConstructor
@Component
public class ActionMapper {

	private final PositionMapper positionMapper;

	private final ActionDetailsMapper actionDetailsMapper;

	public ActionDTO toDTO(Action action, Long applicationId, Long stageId) {
		ActionDTO dto = new ActionDTO();
		dto.setActionID(action.getActionID());
		dto.nextActions(action.getNextActions());
		dto.setDetails(actionDetailsMapper.toDTO(action, applicationId, stageId));
		dto.setLabel(action.getLabel());
		dto.setPosition(positionMapper.toDTO(action.getPosition()));
		return dto;
	}

	public Action fromDefinitionDTO(ActionDefinitionDTO dto) {
		return actionDetailsMapper.fromDTO(dto);
	}

}
