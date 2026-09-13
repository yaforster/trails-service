package io.github.yaforster.trails.adapter.api.rest.testplan.assembler;

import io.github.yaforster.trails.core.persisted.PersistedTestPlan;

public record PersistedTestPlanContext(Long applicationId, Long stageId, PersistedTestPlan persistedTestPlan,
		boolean hasTestSteps) {

}
