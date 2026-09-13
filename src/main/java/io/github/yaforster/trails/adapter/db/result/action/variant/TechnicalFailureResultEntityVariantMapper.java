package io.github.yaforster.trails.adapter.db.result.action.variant;

import io.github.yaforster.trails.adapter.db.result.action.ResultEntity;
import io.github.yaforster.trails.adapter.db.result.action.TechnicalFailureEntity;
import io.github.yaforster.trails.core.test.Result;
import io.github.yaforster.trails.core.test.result.failure.TechnicalFailure;
import org.springframework.stereotype.Component;

@Component
public class TechnicalFailureResultEntityVariantMapper implements ResultEntityVariantMapper {

	@Override
	public boolean supports(Result result) {
		return result instanceof TechnicalFailure;
	}

	@Override
	public ResultEntity toEntity(Result result) {
		TechnicalFailure technicalFailure = (TechnicalFailure) result;
		TechnicalFailureEntity entity = new TechnicalFailureEntity();
		entity.setExceptionMessageFromAction(technicalFailure.getExceptionMessageFromAction());
		return entity;
	}

}
