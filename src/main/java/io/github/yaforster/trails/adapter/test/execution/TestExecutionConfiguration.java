package io.github.yaforster.trails.adapter.test.execution;

import io.github.yaforster.trails.core.test.Browser;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

@Configuration
@EnableConfigurationProperties(TestExecutionProperties.class)
public class TestExecutionConfiguration {

	@Bean
	@Qualifier("testRunTaskExecutor")
	ThreadPoolTaskExecutor testRunTaskExecutor(TestExecutionProperties properties) {
		return createTaskExecutor(properties.maximumConcurrentRuns(), properties.runQueueCapacity(), "test-run-");
	}

	@Bean
	@Qualifier("testSetTaskExecutor")
	ThreadPoolTaskExecutor testSetTaskExecutor(TestExecutionProperties properties) {
		int queueCapacity = Math.multiplyExact(properties.maximumConcurrentRuns(), Browser.values().length);
		return createTaskExecutor(properties.maximumConcurrentTestSets(), queueCapacity, "test-set-");
	}

	private ThreadPoolTaskExecutor createTaskExecutor(int concurrency, int queueCapacity, String threadNamePrefix) {
		ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
		executor.setCorePoolSize(concurrency);
		executor.setMaxPoolSize(concurrency);
		executor.setQueueCapacity(queueCapacity);
		executor.setThreadNamePrefix(threadNamePrefix);
		executor.setWaitForTasksToCompleteOnShutdown(true);
		return executor;
	}

}
