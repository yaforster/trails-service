package io.github.yaforster.trails.adapter.test.webdriver.driver;

import io.github.yaforster.trails.adapter.test.webdriver.browser.SeleniumBrowserSession;
import io.github.yaforster.trails.core.test.BrowserExecution;
import io.github.yaforster.trails.core.test.Result;
import io.github.yaforster.trails.core.test.TestExecutionContext;
import io.github.yaforster.trails.core.test.TestExecutionContexts;
import io.github.yaforster.trails.core.test.action.browser.ResizeViewportAction;
import io.github.yaforster.trails.core.test.action.browser.viewport.ViewportDimensions;
import io.github.yaforster.trails.core.test.result.failure.TechnicalFailure;
import io.github.yaforster.trails.core.test.result.success.Success;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.openqa.selenium.MutableCapabilities;
import org.openqa.selenium.remote.RemoteWebDriver;

import java.net.URI;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ResizeViewportGridIT {

	private static final String GRID_URL_ENVIRONMENT_VARIABLE = "TRAILS_RESIZE_VIEWPORT_GRID_URL";

	@ParameterizedTest(name = "{0}")
	@MethodSource("browserOptions")
	void resizeViewport_reportsExactSuccessOrTechnicalFailure(String browser, MutableCapabilities options)
			throws Exception {
		String gridUrl = System.getenv(GRID_URL_ENVIRONMENT_VARIABLE);
		Assumptions.assumeTrue(gridUrl != null && !gridUrl.isBlank(),
				() -> GRID_URL_ENVIRONMENT_VARIABLE + " is not configured.");

		RemoteWebDriver driver = new RemoteWebDriver(URI.create(gridUrl).toURL(), options);
		try {
			ViewportDimensions requested = new ViewportDimensions(800, 600);
			Result result = ResizeViewportAction.builder()
				.actionID(1L)
				.label("Grid viewport resize")
				.viewportDimensions(requested)
				.build()
				.execute(new TestExecutionContext(new BrowserExecution(new SeleniumBrowserSession(driver, 1000),
						new TestExecutionContexts.FakeDocumentExecution())));

			if (result instanceof Success success) {
				assertTrue(success.getResultMessage().contains("800 x 600 CSS pixels"));
				return;
			}
			TechnicalFailure failure = assertInstanceOf(TechnicalFailure.class, result);
			assertTrue(failure.getExceptionMessageFromAction().contains("Requested viewport: 800 x 600 CSS pixels."));
			assertTrue(failure.getExceptionMessageFromAction().contains("Last measured viewport:"));
			assertTrue(failure.getExceptionMessageFromAction().contains("Cause:"));
		}
		finally {
			driver.quit();
		}
	}

	private static Stream<org.junit.jupiter.params.provider.Arguments> browserOptions() {
		return Stream.of(
				org.junit.jupiter.params.provider.Arguments.of("Chrome", ChromeWebdriverProvider.setupChromeOptions()),
				org.junit.jupiter.params.provider.Arguments.of("Edge", EdgeWebdriverProvider.setupEdgeOptions()),
				org.junit.jupiter.params.provider.Arguments.of("Firefox",
						FirefoxWebdriverProvider.setupFirefoxOptions()));
	}

}
