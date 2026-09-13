package io.github.yaforster.trails.adapter.db.testplan.mapper.detail.field;

import io.github.yaforster.trails.adapter.db.testplan.entity.ActionEntity;
import io.github.yaforster.trails.adapter.db.testplan.entity.details.ViewportMoveDetailsEntity;
import io.github.yaforster.trails.adapter.db.testplan.mapper.PositionEntityMapper;
import io.github.yaforster.trails.core.test.action.browser.viewport.MoveViewportAction;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ViewportMoveDetailsMapper {

	private final PositionEntityMapper positionMapper;

	public ViewportMoveDetailsEntity toEntity(MoveViewportAction action) {
		return new ViewportMoveDetailsEntity(action.getMovement(), action.getDirection(),
				Math.max(0D, action.getAmount()), action.getUnit(), Math.max(0L, action.getDelayAfterMoveMillis()));
	}

	public MoveViewportAction fromEntity(ActionEntity entity, ViewportMoveDetailsEntity details) {
		return MoveViewportAction.builder()
			.actionID(entity.getId())
			.label(entity.getLabel())
			.nextActions(entity.getNextActions())
			.movement(details.getMovement())
			.direction(details.getDirection())
			.amount(Math.max(0D, details.getAmount()))
			.unit(details.getUnit())
			.delayAfterMoveMillis(Math.max(0L, details.getDelayAfterMoveMillis()))
			.position(positionMapper.fromEntity(entity.getPosition()))
			.build();
	}

}
