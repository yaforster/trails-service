package io.github.yaforster.trails.adapter.db.testplan.mapper.detail.field;

import io.github.yaforster.trails.adapter.db.testplan.entity.ActionEntity;
import io.github.yaforster.trails.adapter.db.testplan.entity.details.ElementValueCheckDetailsEntity;
import io.github.yaforster.trails.adapter.db.testplan.mapper.PositionEntityMapper;
import io.github.yaforster.trails.adapter.db.testplan.mapper.ValueComputationInstructionEntityMapper;
import io.github.yaforster.trails.app.services.ElementDatabaseService;
import io.github.yaforster.trails.core.test.action.element.ElementValueCheckAction;
import io.github.yaforster.trails.core.test.action.element.Locator;
import io.github.yaforster.trails.core.test.action.value.ValueComputationInstruction;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.function.UnaryOperator;

@Component
@RequiredArgsConstructor
public class ElementValueCheckDetailsMapper {

	private final ElementDatabaseService elementDatabaseService;

	private final ValueComputationInstructionEntityMapper instructionMapper;

	private final PositionEntityMapper positionMapper;

	public ElementValueCheckDetailsEntity toEntity(ElementValueCheckAction action, Long applicationId, Long stageId) {
		Long elementID = elementDatabaseService.getElementIDByLocator(new ElementDatabaseService.ElementLocator(
				applicationId, stageId, action.getLocatorForElementToActOn().locatorString()));
		return new ElementValueCheckDetailsEntity(elementID, action.getValueSource(), action.getValueName(),
				instructionMapper.toEntity(action.getComputationInstruction()));
	}

	public ElementValueCheckAction fromEntity(ActionEntity entity, ElementValueCheckDetailsEntity details) {
		return fromEntity(entity, details, UnaryOperator.identity());
	}

	public ElementValueCheckAction fromEntity(ActionEntity entity, ElementValueCheckDetailsEntity details,
			UnaryOperator<ValueComputationInstruction> instructionResolver) {
		Locator locator = elementDatabaseService.getElementLocator(details.getElementID());
		return ElementValueCheckAction.builder()
			.actionID(entity.getId())
			.label(entity.getLabel())
			.nextActions(entity.getNextActions())
			.locatorForElementToActOn(locator)
			.valueSource(details.getValueSource())
			.valueName(details.getValueName())
			.computationInstruction(
					instructionResolver.apply(instructionMapper.fromEntity(details.getValueComputation())))
			.position(positionMapper.fromEntity(entity.getPosition()))
			.build();
	}

}
