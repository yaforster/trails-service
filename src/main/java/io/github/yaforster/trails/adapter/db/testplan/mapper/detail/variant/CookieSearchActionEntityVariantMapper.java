package io.github.yaforster.trails.adapter.db.testplan.mapper.detail.variant;

import io.github.yaforster.trails.adapter.db.testplan.entity.ActionDetailsEntity;
import io.github.yaforster.trails.adapter.db.testplan.entity.ActionEntity;
import io.github.yaforster.trails.adapter.db.testplan.entity.details.CookieSearchDetailsEntity;
import io.github.yaforster.trails.adapter.db.testplan.mapper.detail.field.CheckCookieDetailsMapper;
import io.github.yaforster.trails.core.test.Action;
import io.github.yaforster.trails.core.test.action.browser.storage.CheckCookieAction;
import io.github.yaforster.trails.core.test.action.value.ValueComputationInstruction;
import org.springframework.stereotype.Component;

import java.util.function.UnaryOperator;

@Component
class CookieSearchActionEntityVariantMapper
		extends AbstractActionEntityVariantMapper<CheckCookieAction, CookieSearchDetailsEntity> {

	private final CheckCookieDetailsMapper mapper;

	CookieSearchActionEntityVariantMapper(CheckCookieDetailsMapper mapper) {
		super(CheckCookieAction.class, CookieSearchDetailsEntity.class);
		this.mapper = mapper;
	}

	@Override
	protected ActionDetailsEntity toVariantEntity(CheckCookieAction action) {
		return mapper.toEntity(action);
	}

	@Override
	protected Action fromVariantEntity(ActionEntity entity, CookieSearchDetailsEntity details,
			UnaryOperator<ValueComputationInstruction> instructionResolver) {
		return mapper.fromEntity(entity, details, instructionResolver);
	}

}
