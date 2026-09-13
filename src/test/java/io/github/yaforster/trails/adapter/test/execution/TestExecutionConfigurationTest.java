package io.github.yaforster.trails.adapter.test.execution;

import org.junit.jupiter.api.Test;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import static org.junit.jupiter.api.Assertions.assertEquals;

class TestExecutionConfigurationTest {

	@Test
	void testRunTaskExecutor_shouldApplyConfiguredConcurrencyAndQueueCapacity() {
		TestExecutionProperties properties = new TestExecutionProperties(16, 64, 12);
		TestExecutionConfiguration configuration = new TestExecutionConfiguration();

		ThreadPoolTaskExecutor executor = configuration.testRunTaskExecutor(properties);
		executor.initialize();
		try {
			assertEquals(16, executor.getCorePoolSize());
			assertEquals(64, executor.getThreadPoolExecutor().getQueue().remainingCapacity());
		}
		finally {
			executor.shutdown();
		}
	}

	@Test
	void testSetTaskExecutor_shouldApplyConfiguredGlobalConcurrencyAndBoundedQueue() {
		TestExecutionProperties properties = new TestExecutionProperties(16, 64, 12);
		TestExecutionConfiguration configuration = new TestExecutionConfiguration();

		ThreadPoolTaskExecutor executor = configuration.testSetTaskExecutor(properties);
		executor.initialize();
		try {
			assertEquals(12, executor.getCorePoolSize());
			assertEquals(48, executor.getThreadPoolExecutor().getQueue().remainingCapacity());
		}
		finally {
			executor.shutdown();
		}
	}

}
