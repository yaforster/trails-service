package io.github.yaforster.trails.adapter.api.rest.testpath.assembler;

import io.github.yaforster.trails.core.persisted.PersistedTestPathResult;

public record PersistedTestPathResultContext(Long applicationId, Long stageId, Long testRunId, Long testSetResultId,
		PersistedTestPathResult persistedTestPathResult) {

	public static PersistedTestPathResultContext of(Long applicationId, Long stageId, Long testRunId,
			Long testSetResultId, PersistedTestPathResult persistedTestPathResult) {
		return new PersistedTestPathResultContext(applicationId, stageId, testRunId, testSetResultId,
				persistedTestPathResult);
	}
}
