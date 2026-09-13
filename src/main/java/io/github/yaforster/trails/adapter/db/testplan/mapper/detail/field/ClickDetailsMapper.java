package io.github.yaforster.trails.adapter.db.testplan.mapper.detail.field;

import io.github.yaforster.trails.adapter.db.testplan.entity.ActionEntity;
import io.github.yaforster.trails.adapter.db.testplan.entity.details.ClickActionDetailsEntity;
import io.github.yaforster.trails.adapter.db.testplan.mapper.PositionEntityMapper;
import io.github.yaforster.trails.app.services.ElementDatabaseService;
import io.github.yaforster.trails.core.test.action.element.ClickAction;
import io.github.yaforster.trails.core.test.action.element.Locator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ClickDetailsMapper {

	private final ElementDatabaseService elementDatabaseService;

	private final PositionEntityMapper positionMapper;

	public ClickActionDetailsEntity toEntity(ClickAction action, Long applicationId, Long stageId) {
		Long elementID = elementDatabaseService.getElementIDByLocator(new ElementDatabaseService.ElementLocator(
				applicationId, stageId, action.getLocatorForElementToActOn().locatorString()));
		return new ClickActionDetailsEntity(elementID);
	}

	public ClickAction fromEntity(ActionEntity entity, ClickActionDetailsEntity details) {
		Locator locator = elementDatabaseService.getElementLocator(details.getElementID());
		return ClickAction.builder()
			.actionID(entity.getId())
			.label(entity.getLabel())
			.nextActions(entity.getNextActions())
			.locatorForElementToActOn(locator)
			.position(positionMapper.fromEntity(entity.getPosition()))
			.build();
	}

}
