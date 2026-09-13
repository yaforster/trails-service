package io.github.yaforster.trails.adapter.api.rest.testrun.assembler;

import io.github.yaforster.trails.core.data.ResultIndicator;

import java.time.OffsetDateTime;

public record TestRunResultContext(Long applicationId, Long stageId, Long resultId, OffsetDateTime timestamp,
		ResultIndicator indicator, String label) {

}
