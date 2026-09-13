package io.github.yaforster.trails.adapter.db.testplan.mapper.detail.field;

import io.github.yaforster.trails.adapter.db.testplan.entity.ActionEntity;
import io.github.yaforster.trails.adapter.db.testplan.entity.details.SwitchWebsiteDetailsEntity;
import io.github.yaforster.trails.adapter.db.testplan.mapper.PositionEntityMapper;
import io.github.yaforster.trails.adapter.db.testplan.mapper.ValueComputationInstructionEntityMapper;
import io.github.yaforster.trails.core.test.action.browser.SwitchWebsiteAction;
import io.github.yaforster.trails.core.test.action.value.ValueComputationInstruction;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.function.UnaryOperator;

@Component
@RequiredArgsConstructor
public class SwitchWebsiteDetailsMapper {

	private final ValueComputationInstructionEntityMapper instructionMapper;

	private final PositionEntityMapper positionMapper;

	public SwitchWebsiteDetailsEntity toEntity(SwitchWebsiteAction action) {
		return new SwitchWebsiteDetailsEntity(instructionMapper.toEntity(action.getComputationInstruction()));
	}

	public SwitchWebsiteAction fromEntity(ActionEntity entity, SwitchWebsiteDetailsEntity details) {
		return fromEntity(entity, details, UnaryOperator.identity());
	}

	public SwitchWebsiteAction fromEntity(ActionEntity entity, SwitchWebsiteDetailsEntity details,
			UnaryOperator<ValueComputationInstruction> instructionResolver) {
		return SwitchWebsiteAction.builder()
			.actionID(entity.getId())
			.label(entity.getLabel())
			.nextActions(entity.getNextActions())
			.computationInstruction(
					instructionResolver.apply(instructionMapper.fromEntity(details.getValueComputation())))
			.position(positionMapper.fromEntity(entity.getPosition()))
			.build();
	}

}
