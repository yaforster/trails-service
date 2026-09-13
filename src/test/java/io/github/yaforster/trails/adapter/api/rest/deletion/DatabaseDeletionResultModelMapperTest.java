package io.github.yaforster.trails.adapter.api.rest.deletion;

import io.github.yaforster.trails.adapter.api.rest.deletion.DatabaseDeletionResultModel;
import io.github.yaforster.trails.adapter.api.rest.deletion.DatabaseDeletionResultModelMapper;
import io.github.yaforster.trails.adapter.api.rest.deletion.DeletionFailureModel;
import io.github.yaforster.trails.adapter.api.rest.deletion.DeletionNotFoundModel;
import io.github.yaforster.trails.adapter.api.rest.deletion.DeletionSuccessModel;
import io.github.yaforster.trails.core.deletion.*;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertInstanceOf;

class DatabaseDeletionResultModelMapperTest {

	private final DatabaseDeletionResultModelMapper mapper = new DatabaseDeletionResultModelMapper();

	@Test
	void mapsDatabaseDeletionResultToModel() {
		DatabaseDeletionResultModel success = mapper.toModel(new DeletionSuccess(11L));
		DatabaseDeletionResultModel notFound = mapper.toModel(new DeletionNotFound(12L));
		DatabaseDeletionResultModel conflict = mapper.toModel(new DeletionFailure(13L, "conflict", null));
		ErrorDetails error = new ErrorDetails("IllegalStateException", "failure", "stack");
		DatabaseDeletionResultModel failure = mapper.toModel(new DeletionFailure(14L, "failure", error));

		assertInstanceOf(DeletionSuccessModel.class, success);
		assertInstanceOf(DeletionNotFoundModel.class, notFound);
		assertInstanceOf(DeletionFailureModel.class, conflict);
		assertInstanceOf(DeletionFailureModel.class, failure);
	}

}
