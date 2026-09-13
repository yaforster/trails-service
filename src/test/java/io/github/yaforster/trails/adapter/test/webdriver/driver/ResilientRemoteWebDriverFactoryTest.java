package io.github.yaforster.trails.adapter.test.webdriver.driver;

import io.github.resilience4j.bulkhead.Bulkhead;
import io.github.resilience4j.bulkhead.BulkheadConfig;
import io.github.resilience4j.bulkhead.BulkheadFullException;
import io.github.resilience4j.bulkhead.BulkheadRegistry;
import io.github.resilience4j.circuitbreaker.CallNotPermittedException;
import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import io.github.resilience4j.retry.RetryConfig;
import io.github.resilience4j.retry.RetryRegistry;
import io.github.yaforster.trails.core.test.Browser;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.openqa.selenium.remote.RemoteWebDriver;

import java.time.Duration;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ResilientRemoteWebDriverFactoryTest {

	@Test
	void createDriver_shouldRetryTransientSessionCreationFailure() {
		RemoteWebDriver expectedDriver = Mockito.mock(RemoteWebDriver.class);
		AtomicInteger attempts = new AtomicInteger();
		ResilientRemoteWebDriverFactory factory = new ResilientRemoteWebDriverFactory(
				CircuitBreakerRegistry.ofDefaults(), retryRegistryWithTwoChromeAttempts(),
				BulkheadRegistry.ofDefaults());

		RemoteWebDriver result = factory.createDriver(Browser.CHROME, retryOnceSupplier(attempts, expectedDriver));

		assertSame(expectedDriver, result);
	}

	@Test
	void createDriver_shouldOpenCircuit_whenRepeatedSessionCreationFails() {
		ResilientRemoteWebDriverFactory factory = new ResilientRemoteWebDriverFactory(
				circuitBreakerRegistryWithTwoFirefoxFailures(), retryRegistryWithSingleFirefoxAttempt(),
				BulkheadRegistry.ofDefaults());
		Supplier<RemoteWebDriver> failingSupplier = () -> {
			throw new RuntimeException("Grid unavailable");
		};
		ignoreFailure(() -> factory.createDriver(Browser.FIREFOX, failingSupplier));
		ignoreFailure(() -> factory.createDriver(Browser.FIREFOX, failingSupplier));

		assertThrows(CallNotPermittedException.class, () -> factory.createDriver(Browser.FIREFOX, failingSupplier));
	}

	@Test
	void createDriver_shouldRejectSessionCreation_whenBrowserBulkheadIsFull() {
		BulkheadConfig bulkheadConfig = BulkheadConfig.custom()
			.maxConcurrentCalls(1)
			.maxWaitDuration(Duration.ZERO)
			.build();
		BulkheadRegistry bulkheadRegistry = BulkheadRegistry.ofDefaults();
		Bulkhead bulkhead = bulkheadRegistry.bulkhead("seleniumEdge", bulkheadConfig);
		bulkhead.acquirePermission();
		ResilientRemoteWebDriverFactory factory = new ResilientRemoteWebDriverFactory(
				CircuitBreakerRegistry.ofDefaults(), RetryRegistry.ofDefaults(), bulkheadRegistry);

		assertThrows(BulkheadFullException.class,
				() -> factory.createDriver(Browser.EDGE, () -> Mockito.mock(RemoteWebDriver.class)));
	}

	private RetryRegistry retryRegistryWithTwoChromeAttempts() {
		RetryRegistry retryRegistry = RetryRegistry.ofDefaults();
		RetryConfig retryConfig = RetryConfig.custom().maxAttempts(2).waitDuration(Duration.ZERO).build();
		retryRegistry.retry("seleniumChrome", retryConfig);
		return retryRegistry;
	}

	private RetryRegistry retryRegistryWithSingleFirefoxAttempt() {
		RetryRegistry retryRegistry = RetryRegistry.ofDefaults();
		RetryConfig retryConfig = RetryConfig.custom().maxAttempts(1).waitDuration(Duration.ZERO).build();
		retryRegistry.retry("seleniumFirefox", retryConfig);
		return retryRegistry;
	}

	private CircuitBreakerRegistry circuitBreakerRegistryWithTwoFirefoxFailures() {
		CircuitBreakerRegistry circuitBreakerRegistry = CircuitBreakerRegistry.ofDefaults();
		CircuitBreakerConfig circuitBreakerConfig = CircuitBreakerConfig.custom()
			.slidingWindowType(CircuitBreakerConfig.SlidingWindowType.COUNT_BASED)
			.slidingWindowSize(2)
			.minimumNumberOfCalls(2)
			.failureRateThreshold(50)
			.waitDurationInOpenState(Duration.ofSeconds(30))
			.build();
		circuitBreakerRegistry.circuitBreaker("seleniumFirefox", circuitBreakerConfig);
		return circuitBreakerRegistry;
	}

	private Supplier<RemoteWebDriver> retryOnceSupplier(AtomicInteger attempts, RemoteWebDriver expectedDriver) {
		return () -> {
			if (attempts.incrementAndGet() == 1) {
				throw new RuntimeException("Grid not ready");
			}
			return expectedDriver;
		};
	}

	private void ignoreFailure(Runnable runnable) {
		try {
			runnable.run();
		}
		catch (Exception ignored) {
		}
	}

}
