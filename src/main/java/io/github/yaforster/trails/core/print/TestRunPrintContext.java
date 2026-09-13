package io.github.yaforster.trails.core.print;

import io.github.yaforster.trails.core.data.ResultIndicator;

import java.sql.Timestamp;
import java.util.List;

public record TestRunPrintContext(Long id, Long applicationId, Long stageId, Long testPlanId, Timestamp timestamp,
		ResultIndicator status, String label, List<TestSetPrintContext> testSetPrintContexts) {
}
