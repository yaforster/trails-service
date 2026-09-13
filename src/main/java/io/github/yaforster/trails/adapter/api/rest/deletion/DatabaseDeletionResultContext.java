package io.github.yaforster.trails.adapter.api.rest.deletion;

import io.github.yaforster.trails.core.deletion.DatabaseDeletionResult;

public record DatabaseDeletionResultContext(Long applicationId, Long stageId, DatabaseDeletionResult deletionResult) {
}
