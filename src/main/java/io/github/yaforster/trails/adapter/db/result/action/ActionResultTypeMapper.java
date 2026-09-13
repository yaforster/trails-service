package io.github.yaforster.trails.adapter.db.result.action;

import io.github.yaforster.trails.core.persisted.ActionResultType;
import org.springframework.stereotype.Component;

@Component
public class ActionResultTypeMapper {

	public ActionResultType fromEntity(ResultEntity entity) {
		return switch (entity) {
			case SuccessEntity _ -> ActionResultType.SUCCESS;
			case SkippedResultEntity _ -> ActionResultType.SKIPPED;
			case ValidationFailureEntity _ -> ActionResultType.VALIDATION_FAILURE;
			case TechnicalFailureEntity _ -> ActionResultType.TECHNICAL_FAILURE;
			case null -> throw new IllegalArgumentException("Result entity must not be null.");
			default -> throw new IllegalStateException("Unknown result entity type: " + entity.getClass().getName());
		};
	}

}
