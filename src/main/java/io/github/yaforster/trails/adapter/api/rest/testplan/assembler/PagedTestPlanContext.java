package io.github.yaforster.trails.adapter.api.rest.testplan.assembler;

import org.springframework.data.domain.Page;

public record PagedTestPlanContext(Long applicationId, Long stageId, Page<PersistedTestPlanContext> contexts) {

}
