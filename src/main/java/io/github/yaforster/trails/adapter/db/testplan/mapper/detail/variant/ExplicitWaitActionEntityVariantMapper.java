package io.github.yaforster.trails.adapter.db.testplan.mapper.detail.variant;

import io.github.yaforster.trails.adapter.db.testplan.entity.ActionDetailsEntity;
import io.github.yaforster.trails.adapter.db.testplan.entity.ActionEntity;
import io.github.yaforster.trails.adapter.db.testplan.entity.details.ExplicitWaitDetailsEntity;
import io.github.yaforster.trails.adapter.db.testplan.mapper.detail.field.ExplicitWaitDetailsMapper;
import io.github.yaforster.trails.core.test.Action;
import io.github.yaforster.trails.core.test.action.browser.ExplicitWaitAction;
import io.github.yaforster.trails.core.test.action.value.ValueComputationInstruction;
import org.springframework.stereotype.Component;

import java.util.function.UnaryOperator;

@Component
class ExplicitWaitActionEntityVariantMapper
		extends AbstractActionEntityVariantMapper<ExplicitWaitAction, ExplicitWaitDetailsEntity> {

	private final ExplicitWaitDetailsMapper mapper;

	ExplicitWaitActionEntityVariantMapper(ExplicitWaitDetailsMapper mapper) {
		super(ExplicitWaitAction.class, ExplicitWaitDetailsEntity.class);
		this.mapper = mapper;
	}

	@Override
	protected ActionDetailsEntity toVariantEntity(ExplicitWaitAction action) {
		return mapper.toEntity(action);
	}

	@Override
	protected Action fromVariantEntity(ActionEntity entity, ExplicitWaitDetailsEntity details,
			UnaryOperator<ValueComputationInstruction> instructionResolver) {
		return mapper.fromEntity(entity, details);
	}

}
