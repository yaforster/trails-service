package io.github.yaforster.trails.app.services;

import io.github.yaforster.trails.core.test.TestPlanRunDefinition;

import java.util.UUID;

@FunctionalInterface
public interface TestExecutionFacadeService {

	void executeTest(TestExecution testExecution);

	record TestExecution(UUID executionId, TestPlanRunDefinition runDefinition) {
	}

}
