package io.github.yaforster.trails.adapter.test.webdriver.driver;

import com.google.common.annotations.VisibleForTesting;
import io.github.yaforster.trails.adapter.test.webdriver.WebdriverProperties;
import io.github.yaforster.trails.adapter.test.webdriver.document.PdfBoxDocumentExecution;
import io.github.yaforster.trails.core.test.Browser;
import io.github.yaforster.trails.core.test.BrowserExecution;
import io.github.yaforster.trails.core.test.action.document.DocumentExecution;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.net.URI;
import java.util.Optional;

@Component
public class FirefoxWebdriverProvider extends SeleniumWebdriverProvider<RemoteWebDriver> {

	private final ResilientRemoteWebDriverFactory remoteWebDriverFactory;

	@Autowired
	public FirefoxWebdriverProvider(WebdriverProperties properties,
			ResilientRemoteWebDriverFactory remoteWebDriverFactory, DocumentExecution documentExecution) {
		super(properties.gridUrl(), properties.fileDownload().timeoutMillis(), documentExecution);
		this.remoteWebDriverFactory = remoteWebDriverFactory;
	}

	@VisibleForTesting
	FirefoxWebdriverProvider(WebdriverProperties properties) {
		this(properties, ResilientRemoteWebDriverFactory.withDefaults(), new PdfBoxDocumentExecution());
	}

	public Optional<BrowserExecution> createDriver() {
		try {
			FirefoxOptions options = setupFirefoxOptions();
			RemoteWebDriver driver = remoteWebDriverFactory.createDriver(Browser.FIREFOX, URI.create(gridUrl).toURL(),
					options);
			return initializedExecution(driver, ignored -> {
			});
		}
		catch (Exception e) {
			logUnavailableDriver(e);
			return Optional.empty();
		}
	}

	@VisibleForTesting
	protected static FirefoxOptions setupFirefoxOptions() {
		FirefoxOptions options = new FirefoxOptions();
		options.addArguments("--headless");
		options.addArguments("--allow-elevated-browser");
		options.setCapability("se:downloadsEnabled", true);
		return options;
	}

}
