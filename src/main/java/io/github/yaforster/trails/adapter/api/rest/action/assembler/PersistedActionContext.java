package io.github.yaforster.trails.adapter.api.rest.action.assembler;

import io.github.yaforster.trails.core.persisted.PersistedAction;

public record PersistedActionContext(Long applicationId, Long stageId, Long testPlanId,
		PersistedAction persistedAction) {

}
