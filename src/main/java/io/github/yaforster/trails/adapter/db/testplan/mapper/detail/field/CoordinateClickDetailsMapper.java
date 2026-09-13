package io.github.yaforster.trails.adapter.db.testplan.mapper.detail.field;

import io.github.yaforster.trails.adapter.db.testplan.entity.ActionEntity;
import io.github.yaforster.trails.adapter.db.testplan.entity.details.CoordinateClickDetailsEntity;
import io.github.yaforster.trails.adapter.db.testplan.mapper.PositionEntityMapper;
import io.github.yaforster.trails.core.test.action.browser.CoordinateClickAction;
import io.github.yaforster.trails.core.test.action.browser.ViewportCoordinates;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CoordinateClickDetailsMapper {

	private final PositionEntityMapper positionMapper;

	public CoordinateClickDetailsEntity toEntity(CoordinateClickAction action) {
		ViewportCoordinates coordinates = action.getViewportCoordinates();
		return new CoordinateClickDetailsEntity(coordinates.xCoordinate(), coordinates.yCoordinate());
	}

	public CoordinateClickAction fromEntity(ActionEntity entity, CoordinateClickDetailsEntity details) {
		return CoordinateClickAction.builder()
			.actionID(entity.getId())
			.label(entity.getLabel())
			.nextActions(entity.getNextActions())
			.viewportCoordinates(new ViewportCoordinates(details.getXCoordinate(), details.getYCoordinate()))
			.position(positionMapper.fromEntity(entity.getPosition()))
			.build();
	}

}
