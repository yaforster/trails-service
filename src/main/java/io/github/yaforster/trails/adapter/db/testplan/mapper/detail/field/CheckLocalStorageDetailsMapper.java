package io.github.yaforster.trails.adapter.db.testplan.mapper.detail.field;

import io.github.yaforster.trails.adapter.db.testplan.entity.ActionEntity;
import io.github.yaforster.trails.adapter.db.testplan.entity.details.LocalStorageSearchDetailsEntity;
import io.github.yaforster.trails.adapter.db.testplan.mapper.PositionEntityMapper;
import io.github.yaforster.trails.adapter.db.testplan.mapper.ValueComputationInstructionEntityMapper;
import io.github.yaforster.trails.core.test.action.browser.storage.CheckLocalStorageAction;
import io.github.yaforster.trails.core.test.action.value.ValueComputationInstruction;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.function.UnaryOperator;

@Component
@RequiredArgsConstructor
public class CheckLocalStorageDetailsMapper {

	private final ValueComputationInstructionEntityMapper instructionMapper;

	private final PositionEntityMapper positionMapper;

	public LocalStorageSearchDetailsEntity toEntity(CheckLocalStorageAction action) {
		return new LocalStorageSearchDetailsEntity(action.getLocalStorageItemKey(),
				instructionMapper.toEntity(action.getComputationInstruction()));
	}

	public CheckLocalStorageAction fromEntity(ActionEntity entity, LocalStorageSearchDetailsEntity details) {
		return fromEntity(entity, details, UnaryOperator.identity());
	}

	public CheckLocalStorageAction fromEntity(ActionEntity entity, LocalStorageSearchDetailsEntity details,
			UnaryOperator<ValueComputationInstruction> instructionResolver) {
		return CheckLocalStorageAction.builder()
			.actionID(entity.getId())
			.label(entity.getLabel())
			.nextActions(entity.getNextActions())
			.localStorageItemKey(details.getStorageKey())
			.computationInstruction(
					instructionResolver.apply(instructionMapper.fromEntity(details.getValueComputation())))
			.position(positionMapper.fromEntity(entity.getPosition()))
			.build();
	}

}
