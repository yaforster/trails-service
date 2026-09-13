package io.github.yaforster.trails.adapter.db.testplan.mapper.detail.field;

import io.github.yaforster.trails.adapter.db.testplan.entity.ActionEntity;
import io.github.yaforster.trails.adapter.db.testplan.entity.details.ExplicitWaitDetailsEntity;
import io.github.yaforster.trails.adapter.db.testplan.mapper.PositionEntityMapper;
import io.github.yaforster.trails.core.test.action.browser.ExplicitWaitAction;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ExplicitWaitDetailsMapper {

	private final PositionEntityMapper positionMapper;

	public ExplicitWaitDetailsEntity toEntity(ExplicitWaitAction action) {
		return new ExplicitWaitDetailsEntity(Math.max(0L, action.getDelayMillis()));
	}

	public ExplicitWaitAction fromEntity(ActionEntity entity, ExplicitWaitDetailsEntity details) {
		return ExplicitWaitAction.builder()
			.actionID(entity.getId())
			.label(entity.getLabel())
			.nextActions(entity.getNextActions())
			.delayMillis(Math.max(0L, details.getDelayMillis()))
			.position(positionMapper.fromEntity(entity.getPosition()))
			.build();
	}

}
