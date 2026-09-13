package io.github.yaforster.trails.adapter.db.testplan.mapper.detail.field;

import io.github.yaforster.trails.adapter.db.testplan.entity.ActionEntity;
import io.github.yaforster.trails.adapter.db.testplan.entity.details.TextCheckDetailsEntity;
import io.github.yaforster.trails.adapter.db.testplan.mapper.PositionEntityMapper;
import io.github.yaforster.trails.adapter.db.testplan.mapper.ValueComputationInstructionEntityMapper;
import io.github.yaforster.trails.app.services.ElementDatabaseService;
import io.github.yaforster.trails.core.test.action.element.Locator;
import io.github.yaforster.trails.core.test.action.element.TextCheckAction;
import io.github.yaforster.trails.core.test.action.value.ValueComputationInstruction;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.function.UnaryOperator;

@Component
@RequiredArgsConstructor
public class TextCheckDetailsMapper {

	private final ElementDatabaseService elementDatabaseService;

	private final ValueComputationInstructionEntityMapper instructionMapper;

	private final PositionEntityMapper positionMapper;

	public TextCheckDetailsEntity toEntity(TextCheckAction action, Long applicationId, Long stageId) {
		Long elementID = elementDatabaseService.getElementIDByLocator(new ElementDatabaseService.ElementLocator(
				applicationId, stageId, action.getLocatorForElementToActOn().locatorString()));
		return new TextCheckDetailsEntity(elementID, instructionMapper.toEntity(action.getComputationInstruction()));
	}

	public TextCheckAction fromEntity(ActionEntity entity, TextCheckDetailsEntity details) {
		return fromEntity(entity, details, UnaryOperator.identity());
	}

	public TextCheckAction fromEntity(ActionEntity entity, TextCheckDetailsEntity details,
			UnaryOperator<ValueComputationInstruction> instructionResolver) {
		Locator locator = elementDatabaseService.getElementLocator(details.getElementID());
		return TextCheckAction.builder()
			.actionID(entity.getId())
			.label(entity.getLabel())
			.nextActions(entity.getNextActions())
			.locatorForElementToActOn(locator)
			.computationInstruction(
					instructionResolver.apply(instructionMapper.fromEntity(details.getValueComputation())))
			.position(positionMapper.fromEntity(entity.getPosition()))
			.build();
	}

}
