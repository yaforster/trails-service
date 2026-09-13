package io.github.yaforster.trails.adapter.api.rest.testset.assembler;

import io.github.yaforster.trails.core.persisted.PersistedTestSetResult;

public record PersistedTestSetResultContext(Long applicationId, Long stageId, Long testRunId,
		PersistedTestSetResult entity) {

}
