package io.github.yaforster.trails.adapter.test.webdriver.driver;

import com.google.common.annotations.VisibleForTesting;
import io.github.resilience4j.bulkhead.Bulkhead;
import io.github.resilience4j.bulkhead.BulkheadRegistry;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import io.github.resilience4j.retry.Retry;
import io.github.resilience4j.retry.RetryRegistry;
import io.github.yaforster.trails.core.test.Browser;
import org.openqa.selenium.Capabilities;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.springframework.stereotype.Component;

import java.net.URL;
import java.util.function.Supplier;

/**
 * Factory class for creating resilient instances of {@link RemoteWebDriver} that use
 * Circuit Breaker, Retry, and Bulkhead resilience patterns. This class integrates with
 * instances of {@link CircuitBreakerRegistry}, {@link RetryRegistry}, and
 * {@link BulkheadRegistry} to provide reliable and efficient support for remote browser
 * drivers.
 */
@Component
public class ResilientRemoteWebDriverFactory {

	private static final String SELENIUM_CHROME_INSTANCE = "seleniumChrome";

	private static final String SELENIUM_FIREFOX_INSTANCE = "seleniumFirefox";

	private static final String SELENIUM_EDGE_INSTANCE = "seleniumEdge";

	private final CircuitBreakerRegistry circuitBreakerRegistry;

	private final RetryRegistry retryRegistry;

	private final BulkheadRegistry bulkheadRegistry;

	public ResilientRemoteWebDriverFactory(CircuitBreakerRegistry circuitBreakerRegistry, RetryRegistry retryRegistry,
			BulkheadRegistry bulkheadRegistry) {
		this.circuitBreakerRegistry = circuitBreakerRegistry;
		this.retryRegistry = retryRegistry;
		this.bulkheadRegistry = bulkheadRegistry;
	}

	@VisibleForTesting
	static ResilientRemoteWebDriverFactory withDefaults() {
		return new ResilientRemoteWebDriverFactory(CircuitBreakerRegistry.ofDefaults(), RetryRegistry.ofDefaults(),
				BulkheadRegistry.ofDefaults());
	}

	public RemoteWebDriver createDriver(Browser browser, URL seleniumGridUrl, Capabilities capabilities) {
		return createDriver(browser, () -> new RemoteWebDriver(seleniumGridUrl, capabilities));
	}

	@VisibleForTesting
	RemoteWebDriver createDriver(Browser browser, Supplier<RemoteWebDriver> driverSupplier) {
		return decorateSessionSupplier(browser, driverSupplier).get();
	}

	private Supplier<RemoteWebDriver> decorateSessionSupplier(Browser browser,
			Supplier<RemoteWebDriver> driverSupplier) {
		String resilienceInstanceName = getResilientInstanceName(browser);

		CircuitBreaker circuitBreaker = circuitBreakerRegistry.circuitBreaker(resilienceInstanceName);
		Retry retry = retryRegistry.retry(resilienceInstanceName);
		Bulkhead bulkhead = bulkheadRegistry.bulkhead(resilienceInstanceName);

		Supplier<RemoteWebDriver> decoratedSupplier = CircuitBreaker.decorateSupplier(circuitBreaker, driverSupplier);
		decoratedSupplier = Retry.decorateSupplier(retry, decoratedSupplier);
		return Bulkhead.decorateSupplier(bulkhead, decoratedSupplier);
	}

	private String getResilientInstanceName(Browser browser) {
		return switch (browser) {
			case CHROME -> SELENIUM_CHROME_INSTANCE;
			case FIREFOX -> SELENIUM_FIREFOX_INSTANCE;
			case EDGE -> SELENIUM_EDGE_INSTANCE;
		};
	}

}
