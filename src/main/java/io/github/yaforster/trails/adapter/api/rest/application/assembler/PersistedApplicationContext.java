package io.github.yaforster.trails.adapter.api.rest.application.assembler;

import io.github.yaforster.trails.core.persisted.PersistedApplication;

public record PersistedApplicationContext(PersistedApplication persistedApplication, boolean hasStages) {

}
