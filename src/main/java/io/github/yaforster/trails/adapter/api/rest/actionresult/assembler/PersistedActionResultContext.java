package io.github.yaforster.trails.adapter.api.rest.actionresult.assembler;

import io.github.yaforster.trails.core.persisted.PersistedActionResult;

public record PersistedActionResultContext(Long applicationId, Long stageId, Long testRunId, Long testSetResultId,
		Long pathResultId, PersistedActionResult persistedActionResult, boolean hasScreenshot) {

	public static PersistedActionResultContext of(Long applicationId, Long stageId, Long testRunId,
			Long testSetResultId, Long pathResultId, PersistedActionResult persistedActionResult,
			boolean hasScreenshot) {
		return new PersistedActionResultContext(applicationId, stageId, testRunId, testSetResultId, pathResultId,
				persistedActionResult, hasScreenshot);
	}
}
