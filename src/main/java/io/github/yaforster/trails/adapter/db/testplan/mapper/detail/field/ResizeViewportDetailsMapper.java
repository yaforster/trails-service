package io.github.yaforster.trails.adapter.db.testplan.mapper.detail.field;

import io.github.yaforster.trails.adapter.db.testplan.entity.ActionEntity;
import io.github.yaforster.trails.adapter.db.testplan.entity.details.ResizeViewportDetailsEntity;
import io.github.yaforster.trails.adapter.db.testplan.mapper.PositionEntityMapper;
import io.github.yaforster.trails.core.test.action.browser.ResizeViewportAction;
import io.github.yaforster.trails.core.test.action.browser.viewport.ViewportDimensions;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ResizeViewportDetailsMapper {

	private final PositionEntityMapper positionMapper;

	public ResizeViewportDetailsEntity toEntity(ResizeViewportAction action) {
		ViewportDimensions dimensions = action.getViewportDimensions();
		return new ResizeViewportDetailsEntity(dimensions.width(), dimensions.height());
	}

	public ResizeViewportAction fromEntity(ActionEntity entity, ResizeViewportDetailsEntity details) {
		return ResizeViewportAction.builder()
			.actionID(entity.getId())
			.label(entity.getLabel())
			.nextActions(entity.getNextActions())
			.viewportDimensions(new ViewportDimensions(details.getViewportWidth(), details.getViewportHeight()))
			.position(positionMapper.fromEntity(entity.getPosition()))
			.build();
	}

}
