package io.github.yaforster.trails.adapter.db.testplan.mapper.detail.variant;

import io.github.yaforster.trails.adapter.db.testplan.entity.ActionDetailsEntity;
import io.github.yaforster.trails.adapter.db.testplan.entity.ActionEntity;
import io.github.yaforster.trails.core.test.Action;
import io.github.yaforster.trails.core.test.action.value.ValueComputationInstruction;

import java.util.function.UnaryOperator;

abstract class AbstractActionEntityVariantMapper<A extends Action, D extends ActionDetailsEntity>
		implements ActionEntityVariantMapper {

	private final Class<A> actionType;

	private final Class<D> detailsType;

	protected AbstractActionEntityVariantMapper(Class<A> actionType, Class<D> detailsType) {
		this.actionType = actionType;
		this.detailsType = detailsType;
	}

	@Override
	public Class<? extends Action> actionType() {
		return actionType;
	}

	@Override
	public Class<? extends ActionDetailsEntity> detailsType() {
		return detailsType;
	}

	@Override
	public ActionDetailsEntity toEntity(Action action, Long applicationId, Long stageId) {
		return toVariantEntity(actionType.cast(action), applicationId, stageId);
	}

	@Override
	public Action fromEntity(ActionEntity entity, ActionDetailsEntity details,
			UnaryOperator<ValueComputationInstruction> instructionResolver) {
		return fromVariantEntity(entity, detailsType.cast(details), instructionResolver);
	}

	protected ActionDetailsEntity toVariantEntity(A action, Long applicationId, Long stageId) {
		return toVariantEntity(action);
	}

	protected ActionDetailsEntity toVariantEntity(A action) {
		throw new UnsupportedOperationException(
				"Action type " + actionType.getName() + " requires application and stage context");
	}

	protected abstract Action fromVariantEntity(ActionEntity entity, D details,
			UnaryOperator<ValueComputationInstruction> instructionResolver);

}
