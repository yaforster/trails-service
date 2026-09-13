package io.github.yaforster.trails.core.test;

import io.github.yaforster.trails.core.data.ResultIndicator;

import java.sql.Timestamp;
import java.util.List;

public record TestRunResult(List<TestSetResult> testResults, Timestamp timestamp, ResultIndicator indicator,
		Long applicationID, Long stageID, Long testPlanID, String label) {

}
