package io.github.yaforster.trails.app.services;

import io.github.yaforster.trails.core.persisted.PersistedTestRunResult;
import io.github.yaforster.trails.core.test.TestPlanRunDefinition;

import java.util.Objects;
import java.util.function.Consumer;

@FunctionalInterface
public interface TestService {

	void runTest(TestRunExecution testRunExecution);

	record TestRunExecution(TestPlanRunDefinition runDefinition, TestRunLifecycle lifecycle) {

		public TestRunExecution {
			Objects.requireNonNull(lifecycle);
		}

	}

	record TestRunLifecycle(Runnable onStart, Consumer<PersistedTestRunResult> onSuccess,
			Consumer<Exception> onFailure) {

		public TestRunLifecycle {
			Objects.requireNonNull(onStart);
			Objects.requireNonNull(onSuccess);
			Objects.requireNonNull(onFailure);
		}

		public static TestRunLifecycle noOp() {
			return new TestRunLifecycle(() -> {
			}, result -> {
			}, exception -> {
			});
		}

	}

}
