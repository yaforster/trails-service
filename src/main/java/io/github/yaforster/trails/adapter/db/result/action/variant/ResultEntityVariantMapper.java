package io.github.yaforster.trails.adapter.db.result.action.variant;

import io.github.yaforster.trails.adapter.db.result.action.ResultEntity;
import io.github.yaforster.trails.core.test.Result;

public interface ResultEntityVariantMapper {

	boolean supports(Result result);

	ResultEntity toEntity(Result result);

}
