package io.github.yaforster.trails.adapter.api.rest.testrun.assembler;

public record TestRunStatisticsContext(Long applicationId, Long stageId, Long testPlanId, long totalRuns,
		long successfulRuns, long partialSuccessRuns, long failedRuns) {

}
