package io.github.yaforster.trails.adapter.db.testplan.mapper.detail.field;

import io.github.yaforster.trails.adapter.db.testplan.entity.ActionEntity;
import io.github.yaforster.trails.adapter.db.testplan.entity.details.SessionStorageSearchDetailsEntity;
import io.github.yaforster.trails.adapter.db.testplan.mapper.PositionEntityMapper;
import io.github.yaforster.trails.adapter.db.testplan.mapper.ValueComputationInstructionEntityMapper;
import io.github.yaforster.trails.core.test.action.browser.storage.CheckSessionStorageAction;
import io.github.yaforster.trails.core.test.action.value.ValueComputationInstruction;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.function.UnaryOperator;

@Component
@RequiredArgsConstructor
public class CheckSessionStorageDetailsMapper {

	private final ValueComputationInstructionEntityMapper instructionMapper;

	private final PositionEntityMapper positionMapper;

	public SessionStorageSearchDetailsEntity toEntity(CheckSessionStorageAction action) {
		return new SessionStorageSearchDetailsEntity(action.getSessionStorageItemKey(),
				instructionMapper.toEntity(action.getComputationInstruction()));
	}

	public CheckSessionStorageAction fromEntity(ActionEntity entity, SessionStorageSearchDetailsEntity details) {
		return fromEntity(entity, details, UnaryOperator.identity());
	}

	public CheckSessionStorageAction fromEntity(ActionEntity entity, SessionStorageSearchDetailsEntity details,
			UnaryOperator<ValueComputationInstruction> instructionResolver) {
		return CheckSessionStorageAction.builder()
			.actionID(entity.getId())
			.label(entity.getLabel())
			.nextActions(entity.getNextActions())
			.sessionStorageItemKey(details.getStorageKey())
			.computationInstruction(
					instructionResolver.apply(instructionMapper.fromEntity(details.getValueComputation())))
			.position(positionMapper.fromEntity(entity.getPosition()))
			.build();
	}

}
