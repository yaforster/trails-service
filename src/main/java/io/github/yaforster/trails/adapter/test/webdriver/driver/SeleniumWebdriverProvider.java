package io.github.yaforster.trails.adapter.test.webdriver.driver;

import io.github.yaforster.trails.adapter.test.webdriver.browser.SeleniumBrowserSession;
import io.github.yaforster.trails.core.test.BrowserExecution;
import io.github.yaforster.trails.core.test.DriverProvider;
import io.github.yaforster.trails.core.test.action.document.DocumentExecution;
import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.WebDriver;

import java.util.Optional;
import java.util.function.Consumer;

@Slf4j
abstract class SeleniumWebdriverProvider<T extends WebDriver> implements DriverProvider {

	protected final String gridUrl;

	protected final int fileDownloadTimeoutMillis;

	private final DocumentExecution documentExecution;

	SeleniumWebdriverProvider(String gridUrl, int fileDownloadTimeoutMillis, DocumentExecution documentExecution) {
		this.gridUrl = gridUrl;
		this.fileDownloadTimeoutMillis = fileDownloadTimeoutMillis;
		this.documentExecution = documentExecution;
	}

	protected BrowserExecution executionFor(T driver) {
		return new BrowserExecution(new SeleniumBrowserSession(driver, fileDownloadTimeoutMillis), documentExecution);
	}

	protected Optional<BrowserExecution> initializedExecution(T driver, Consumer<T> initializer) {
		try {
			initializer.accept(driver);
			return Optional.of(executionFor(driver));
		}
		catch (Exception exception) {
			try {
				driver.quit();
			}
			catch (Exception cleanupException) {
				exception.addSuppressed(cleanupException);
			}
			logUnavailableDriver(exception);
			return Optional.empty();
		}
	}

	protected void logUnavailableDriver(Exception exception) {
		log.error("Invalid or missing WebDriver configuration while initializing the browser.", exception);
	}

}
