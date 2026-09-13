package io.github.yaforster.trails.adapter.api.rest.artifact.assembler;

import io.github.yaforster.trails.core.persisted.PersistedArtifact;

public record PersistedArtifactContext(Long applicationId, Long stageId, Long testRunId, Long testSetResultId,
		Long pathResultId, PersistedArtifact persistedArtifact) {

}
