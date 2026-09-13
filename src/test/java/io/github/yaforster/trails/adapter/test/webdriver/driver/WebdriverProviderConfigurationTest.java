package io.github.yaforster.trails.adapter.test.webdriver.driver;

import io.github.yaforster.trails.adapter.test.webdriver.WebdriverProperties;
import io.github.yaforster.trails.core.test.Browser;
import io.github.yaforster.trails.core.test.BrowserExecution;
import io.github.yaforster.trails.core.test.TestExecutionContexts;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.openqa.selenium.Capabilities;
import org.openqa.selenium.HasDownloads;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.edge.EdgeOptions;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.remote.LocalFileDetector;
import org.openqa.selenium.remote.RemoteWebDriver;

import java.net.URL;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.withSettings;

@SuppressWarnings("unchecked")
class WebdriverProviderConfigurationTest {

	@Test
	void chromeOptions_enableHeadlessManagedDownloads() {
		ChromeOptions options = ChromeWebdriverProvider.setupChromeOptions();
		Map<String, Object> chromeOptions = (Map<String, Object>) options.asMap().get("goog:chromeOptions");

		assertTrue(((List<String>) chromeOptions.get("args")).contains("--headless=new")
				&& Boolean.TRUE.equals(options.asMap().get("se:downloadsEnabled")));
	}

	@Test
	void edgeOptions_enableHeadlessManagedDownloads() {
		EdgeOptions options = EdgeWebdriverProvider.setupEdgeOptions();
		Map<String, Object> edgeOptions = (Map<String, Object>) options.asMap().get("ms:edgeOptions");

		assertTrue(((List<String>) edgeOptions.get("args")).contains("--remote-allow-origins=*")
				&& Boolean.TRUE.equals(options.asMap().get("se:downloadsEnabled")));
	}

	@Test
	void firefoxOptions_enableHeadlessManagedDownloads() {
		FirefoxOptions options = new FirefoxWebdriverProvider(properties(), mock(ResilientRemoteWebDriverFactory.class),
				new TestExecutionContexts.FakeDocumentExecution())
			.setupFirefoxOptions();
		Map<String, Object> firefoxOptions = (Map<String, Object>) options.asMap().get("moz:firefoxOptions");

		assertTrue(((List<String>) firefoxOptions.get("args")).contains("--allow-elevated-browser")
				&& Boolean.TRUE.equals(options.asMap().get("se:downloadsEnabled")));
	}

	@Test
	void chromeProvider_createsConfiguredRemoteBrowserExecution() throws Exception {
		ResilientRemoteWebDriverFactory factory = mock(ResilientRemoteWebDriverFactory.class);
		RemoteWebDriver driver = managedDownloadDriver();
		when(factory.createDriver(eq(Browser.CHROME), any(URL.class), any(Capabilities.class))).thenReturn(driver);
		ChromeWebdriverProvider provider = new ChromeWebdriverProvider(properties(), factory,
				new TestExecutionContexts.FakeDocumentExecution());

		Optional<BrowserExecution> result = provider.createDriver();

		assertTrue(result.isPresent());
	}

	@Test
	void edgeProvider_usesConfiguredGridUrl() throws Exception {
		ResilientRemoteWebDriverFactory factory = mock(ResilientRemoteWebDriverFactory.class);
		when(factory.createDriver(eq(Browser.EDGE), any(URL.class), any(Capabilities.class)))
			.thenReturn(managedDownloadDriver());
		EdgeWebdriverProvider provider = new EdgeWebdriverProvider(properties(), factory,
				new TestExecutionContexts.FakeDocumentExecution());

		provider.createDriver();
		ArgumentCaptor<URL> gridUrl = ArgumentCaptor.forClass(URL.class);
		Mockito.verify(factory).createDriver(eq(Browser.EDGE), gridUrl.capture(), any(Capabilities.class));

		assertEquals("http://selenium-hub:4444", gridUrl.getValue().toString());
	}

	@Test
	void firefoxProvider_returnsEmptyExecution_whenGridUrlIsInvalid() {
		FirefoxWebdriverProvider provider = new FirefoxWebdriverProvider(
				new WebdriverProperties("invalid url", new WebdriverProperties.FileDownload(25), ""),
				mock(ResilientRemoteWebDriverFactory.class), new TestExecutionContexts.FakeDocumentExecution());

		assertTrue(provider.createDriver().isEmpty());
	}

	@Test
	void chromeProvider_closesDriver_whenPostCreationInitializationFails() throws Exception {
		ResilientRemoteWebDriverFactory factory = mock(ResilientRemoteWebDriverFactory.class);
		RemoteWebDriver driver = mock(RemoteWebDriver.class);
		doThrow(new IllegalStateException("File detector unavailable")).when(driver)
			.setFileDetector(any(LocalFileDetector.class));
		when(factory.createDriver(eq(Browser.CHROME), any(URL.class), any(Capabilities.class))).thenReturn(driver);
		ChromeWebdriverProvider provider = new ChromeWebdriverProvider(properties(), factory,
				new TestExecutionContexts.FakeDocumentExecution());

		provider.createDriver();

		verify(driver).quit();
	}

	private static WebdriverProperties properties() {
		return new WebdriverProperties("http://selenium-hub:4444", new WebdriverProperties.FileDownload(25), "");
	}

	private static RemoteWebDriver managedDownloadDriver() {
		return mock(RemoteWebDriver.class, withSettings().extraInterfaces(HasDownloads.class));
	}

}
