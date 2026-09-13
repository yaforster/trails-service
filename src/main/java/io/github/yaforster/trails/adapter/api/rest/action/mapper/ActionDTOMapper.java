package io.github.yaforster.trails.adapter.api.rest.action.mapper;

import io.github.yaforster.trails.adapter.api.rest.action.model.ActionModel;
import io.github.yaforster.trails.adapter.api.rest.model.ActionDTO;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@AllArgsConstructor
public class ActionDTOMapper {

	private final ActionMapper actionMapper;

	public ActionDTO toDTO(ActionModel model) {
		ActionDTO dto = actionMapper.toDTO(model.getAction(), model.getApplicationId(), model.getStageId());
		dto.setReferenceID(model.getReferenceID());
		return dto;
	}

}
