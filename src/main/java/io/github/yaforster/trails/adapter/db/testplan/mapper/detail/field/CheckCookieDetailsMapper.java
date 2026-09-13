package io.github.yaforster.trails.adapter.db.testplan.mapper.detail.field;

import io.github.yaforster.trails.adapter.db.testplan.entity.ActionEntity;
import io.github.yaforster.trails.adapter.db.testplan.entity.details.CookieSearchDetailsEntity;
import io.github.yaforster.trails.adapter.db.testplan.entity.instruction.ValueComputationInstructionEntity;
import io.github.yaforster.trails.adapter.db.testplan.mapper.PositionEntityMapper;
import io.github.yaforster.trails.adapter.db.testplan.mapper.ValueComputationInstructionEntityMapper;
import io.github.yaforster.trails.core.test.action.browser.storage.CheckCookieAction;
import io.github.yaforster.trails.core.test.action.value.ValueComputationInstruction;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.function.UnaryOperator;

@Component
@RequiredArgsConstructor
public class CheckCookieDetailsMapper {

	private final ValueComputationInstructionEntityMapper instructionMapper;

	private final PositionEntityMapper positionMapper;

	public CookieSearchDetailsEntity toEntity(CheckCookieAction action) {
		ValueComputationInstructionEntity instruction = instructionMapper.toEntity(action.getComputationInstruction());
		return new CookieSearchDetailsEntity(action.getCookieName(), instruction);
	}

	public CheckCookieAction fromEntity(ActionEntity entity, CookieSearchDetailsEntity details) {
		return fromEntity(entity, details, UnaryOperator.identity());
	}

	public CheckCookieAction fromEntity(ActionEntity entity, CookieSearchDetailsEntity details,
			UnaryOperator<ValueComputationInstruction> instructionResolver) {
		return CheckCookieAction.builder()
			.actionID(entity.getId())
			.label(entity.getLabel())
			.nextActions(entity.getNextActions())
			.cookieName(details.getCookieName())
			.computationInstruction(
					instructionResolver.apply(instructionMapper.fromEntity(details.getValueComputation())))
			.position(positionMapper.fromEntity(entity.getPosition()))
			.build();
	}

}
