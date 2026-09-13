package io.github.yaforster.trails.adapter.api.rest.testplan;

import io.github.yaforster.trails.adapter.api.rest.model.TestPlanDefinitionDTO;

public record CreateTestPlanRequest(Long applicationId, Long stageId, TestPlanDefinitionDTO definition) {

}
