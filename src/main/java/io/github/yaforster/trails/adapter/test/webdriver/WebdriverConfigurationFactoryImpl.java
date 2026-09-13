package io.github.yaforster.trails.adapter.test.webdriver;

import com.google.common.annotations.VisibleForTesting;
import io.github.yaforster.trails.app.services.StageDatabaseService;
import io.github.yaforster.trails.app.services.TestPlanDatabaseService;
import io.github.yaforster.trails.app.services.WebdriverConfigurationFactory;
import io.github.yaforster.trails.core.EntityMissingException;
import io.github.yaforster.trails.core.data.TestPlan;
import io.github.yaforster.trails.core.persisted.PersistedStage;
import io.github.yaforster.trails.core.test.Browser;
import io.github.yaforster.trails.core.test.DriverProvider;
import io.github.yaforster.trails.core.test.TestExecutionData;
import io.github.yaforster.trails.core.test.TestPlanRunDefinition;
import io.github.yaforster.trails.core.test.WebDriverData;
import lombok.SneakyThrows;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.net.URI;
import java.net.URL;
import java.util.List;
import java.util.Optional;

@Service
public class WebdriverConfigurationFactoryImpl implements WebdriverConfigurationFactory {

	private final TestPlanDatabaseService testPlanDatabaseService;

	private final StageDatabaseService stageDatabaseService;

	private final WebdriverProperties properties;

	private final DriverProvider configuredEdgeDriver;

	private final DriverProvider configuredChromeDriver;

	private final DriverProvider configuredFirefoxDriver;

	public WebdriverConfigurationFactoryImpl(TestPlanDatabaseService testPlanDatabaseService,
			StageDatabaseService stageDatabaseService, WebdriverProperties properties,
			@Qualifier("edgeWebdriverProvider") DriverProvider configuredEdgeDriver,
			@Qualifier("chromeWebdriverProvider") DriverProvider configuredChromeDriver,
			@Qualifier("firefoxWebdriverProvider") DriverProvider configuredFirefoxDriver) {
		this.testPlanDatabaseService = testPlanDatabaseService;
		this.stageDatabaseService = stageDatabaseService;
		this.properties = properties;
		this.configuredEdgeDriver = configuredEdgeDriver;
		this.configuredChromeDriver = configuredChromeDriver;
		this.configuredFirefoxDriver = configuredFirefoxDriver;
	}

	@Override
	public TestExecutionData getTestExecutionData(TestPlanRunDefinition runDefinition) {
		Optional<TestPlan> loadedTestPlan = testPlanDatabaseService.getExecutableTestPlan(runDefinition.testPlanID());
		if (loadedTestPlan.isEmpty()) {
			throw new EntityMissingException(
					"Test plan with id " + runDefinition.testPlanID() + " could not be found.");
		}
		TestPlan testPlan = loadedTestPlan.get();
		URL applicationURL = findURLOfStageToTestOn(testPlan.getApplicationID(), testPlan.getStageID());
		List<WebDriverData> webDriversToTestWith = runDefinition.browsersToTest()
			.stream()
			.map(browser -> getWebdriverConfiguration(applicationURL, browser))
			.toList();
		return new TestExecutionData(webDriversToTestWith, testPlan.getActions(), testPlan.getApplicationID(),
				testPlan.getStageID(), testPlan.getId(), testPlan.getLabel());
	}

	@SneakyThrows
	@VisibleForTesting
	URL findURLOfStageToTestOn(Long applicationID, Long stageID) {
		Optional<PersistedStage> loadedStage = stageDatabaseService
			.getStage(new StageDatabaseService.StageDetails(applicationID, stageID, true));
		if (loadedStage.isEmpty()) {
			throw new EntityMissingException("Stage with id " + stageID + " could not be found.");
		}
		PersistedStage stage = loadedStage.get();
		return rewriteUrlForBrowser(stage.url());
	}

	@SneakyThrows
	private URL rewriteUrlForBrowser(String stageUrl) {
		URI stageUri = URI.create(stageUrl);
		String browserBaseUrl = properties.browserBaseUrl();
		if (!StringUtils.hasText(browserBaseUrl) || !isLoopbackHost(stageUri)) {
			return stageUri.toURL();
		}
		URI browserBaseUri = URI.create(browserBaseUrl);
		return URI.create(browserBaseUri.getScheme() + "://" + browserBaseUri.getAuthority())
			.resolve(stageUri.getRawPath() + optionalQuery(stageUri) + optionalFragment(stageUri))
			.toURL();
	}

	private String optionalQuery(URI uri) {
		if (!StringUtils.hasText(uri.getRawQuery())) {
			return "";
		}
		return "?" + uri.getRawQuery();
	}

	private String optionalFragment(URI uri) {
		if (!StringUtils.hasText(uri.getRawFragment())) {
			return "";
		}
		return "#" + uri.getRawFragment();
	}

	private boolean isLoopbackHost(URI uri) {
		String host = uri.getHost();
		return "localhost".equalsIgnoreCase(host) || "127.0.0.1".equals(host) || "::1".equals(host);
	}

	@VisibleForTesting
	WebDriverData getWebdriverConfiguration(URL webAppURL, Browser browser) {
		DriverProvider webdriver = getWebdriverForBrowser(browser);
		return new WebDriverData(webdriver, webAppURL, browser);
	}

	@VisibleForTesting
	DriverProvider getWebdriverForBrowser(Browser browser) {
		return switch (browser) {
			case EDGE -> configuredEdgeDriver;
			case FIREFOX -> configuredFirefoxDriver;
			case CHROME -> configuredChromeDriver;
		};
	}

}
