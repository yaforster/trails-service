package io.github.yaforster.trails.adapter.db.result.action.variant;

import io.github.yaforster.trails.adapter.db.result.action.ResultEntity;
import io.github.yaforster.trails.adapter.db.result.action.SkippedResultEntity;
import io.github.yaforster.trails.core.test.Result;
import io.github.yaforster.trails.core.test.result.failure.SkippedResult;
import org.springframework.stereotype.Component;

@Component
public class SkippedResultEntityVariantMapper implements ResultEntityVariantMapper {

	@Override
	public boolean supports(Result result) {
		return result instanceof SkippedResult;
	}

	@Override
	public ResultEntity toEntity(Result result) {
		return new SkippedResultEntity();
	}

}
