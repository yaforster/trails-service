package io.github.yaforster.trails.adapter.api.rest.stage.assembler;

import io.github.yaforster.trails.core.persisted.PersistedStage;

public record PersistedStageContext(Long applicationID, PersistedStage persistedStage, boolean hasElements) {

}
