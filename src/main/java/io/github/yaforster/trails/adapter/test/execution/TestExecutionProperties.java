package io.github.yaforster.trails.adapter.test.execution;

import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Validated
@ConfigurationProperties("service.test-execution")
public record TestExecutionProperties(@Positive int maximumConcurrentRuns, @PositiveOrZero int runQueueCapacity,
		@Positive int maximumConcurrentTestSets) {

}
