package io.github.yaforster.trails.adapter.test.webdriver.driver;

import com.google.common.annotations.VisibleForTesting;
import io.github.yaforster.trails.adapter.test.webdriver.WebdriverProperties;
import io.github.yaforster.trails.adapter.test.webdriver.document.PdfBoxDocumentExecution;
import io.github.yaforster.trails.core.test.Browser;
import io.github.yaforster.trails.core.test.BrowserExecution;
import io.github.yaforster.trails.core.test.action.document.DocumentExecution;
import org.openqa.selenium.edge.EdgeOptions;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.net.URI;
import java.util.Optional;

@Component
public class EdgeWebdriverProvider extends SeleniumWebdriverProvider<RemoteWebDriver> {

	private final ResilientRemoteWebDriverFactory remoteWebDriverFactory;

	@Autowired
	public EdgeWebdriverProvider(WebdriverProperties properties, ResilientRemoteWebDriverFactory remoteWebDriverFactory,
			DocumentExecution documentExecution) {
		super(properties.gridUrl(), properties.fileDownload().timeoutMillis(), documentExecution);
		this.remoteWebDriverFactory = remoteWebDriverFactory;
	}

	@VisibleForTesting
	EdgeWebdriverProvider(WebdriverProperties properties) {
		this(properties, ResilientRemoteWebDriverFactory.withDefaults(), new PdfBoxDocumentExecution());
	}

	@VisibleForTesting
	protected static EdgeOptions setupEdgeOptions() {
		EdgeOptions edgeOptions = new EdgeOptions();
		edgeOptions.addArguments("--headless=new");
		edgeOptions.addArguments("--remote-allow-origins=*");
		edgeOptions.setCapability("se:downloadsEnabled", true);
		return edgeOptions;
	}

	public Optional<BrowserExecution> createDriver() {
		try {
			EdgeOptions edgeOptions = setupEdgeOptions();
			RemoteWebDriver driver = remoteWebDriverFactory.createDriver(Browser.EDGE, URI.create(gridUrl).toURL(),
					edgeOptions);
			return initializedExecution(driver, ignored -> {
			});
		}
		catch (Exception e) {
			logUnavailableDriver(e);
			return Optional.empty();
		}
	}

}
