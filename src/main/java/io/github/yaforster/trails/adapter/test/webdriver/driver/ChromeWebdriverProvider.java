package io.github.yaforster.trails.adapter.test.webdriver.driver;

import com.google.common.annotations.VisibleForTesting;
import io.github.yaforster.trails.adapter.test.webdriver.WebdriverProperties;
import io.github.yaforster.trails.adapter.test.webdriver.document.PdfBoxDocumentExecution;
import io.github.yaforster.trails.core.test.Browser;
import io.github.yaforster.trails.core.test.BrowserExecution;
import io.github.yaforster.trails.core.test.action.document.DocumentExecution;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.remote.LocalFileDetector;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.net.URI;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Component
public class ChromeWebdriverProvider extends SeleniumWebdriverProvider<RemoteWebDriver> {

	private final ResilientRemoteWebDriverFactory remoteWebDriverFactory;

	@Autowired
	public ChromeWebdriverProvider(WebdriverProperties properties,
			ResilientRemoteWebDriverFactory remoteWebDriverFactory, DocumentExecution documentExecution) {
		super(properties.gridUrl(), properties.fileDownload().timeoutMillis(), documentExecution);
		this.remoteWebDriverFactory = remoteWebDriverFactory;
	}

	@VisibleForTesting
	ChromeWebdriverProvider(WebdriverProperties properties) {
		this(properties, ResilientRemoteWebDriverFactory.withDefaults(), new PdfBoxDocumentExecution());
	}

	@VisibleForTesting
	protected static ChromeOptions setupChromeOptions() {
		Map<String, Object> prefs = new HashMap<>();
		prefs.put("download.default_directory", "/home/seluser/Downloads");
		prefs.put("download.prompt_for_download", false);
		prefs.put("download.open_pdf_in_system_reader", false);
		prefs.put("plugins.always_open_pdf_externally", true);
		prefs.put("credentials_enable_service", false);
		prefs.put("profile.password_manager_enabled", false);

		ChromeOptions options = new ChromeOptions();
		options.addArguments("--headless=new");
		options.addArguments("--start-maximized");
		options.setExperimentalOption("excludeSwitches", List.of("enable-automation"));

		options.setCapability("se:downloadsEnabled", true);
		options.setEnableDownloads(true);
		options.setExperimentalOption("prefs", prefs);
		return options;
	}

	public Optional<BrowserExecution> createDriver() {
		try {
			ChromeOptions options = setupChromeOptions();
			RemoteWebDriver driver = remoteWebDriverFactory.createDriver(Browser.CHROME, URI.create(gridUrl).toURL(),
					options);
			return initializedExecution(driver,
					createdDriver -> createdDriver.setFileDetector(new LocalFileDetector()));
		}
		catch (Exception e) {
			logUnavailableDriver(e);
			return Optional.empty();
		}
	}

}
