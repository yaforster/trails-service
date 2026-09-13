package io.github.yaforster.trails.adapter.api.rest.element.assembler;

import io.github.yaforster.trails.core.persisted.PersistedElement;

import java.util.Optional;

public record PersistedElementContext(Long applicationID, Long stageID, PersistedElement persistedElement,
		Optional<Long> screenshotID) {

}
