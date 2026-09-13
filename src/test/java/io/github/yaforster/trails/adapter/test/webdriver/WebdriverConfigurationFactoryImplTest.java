package io.github.yaforster.trails.adapter.test.webdriver;

import io.github.yaforster.trails.core.EntityMissingException;
import io.github.yaforster.trails.app.services.StageDatabaseService;
import io.github.yaforster.trails.app.services.TestPlanDatabaseService;
import io.github.yaforster.trails.core.TrailsTest;
import io.github.yaforster.trails.core.data.TestPlan;
import io.github.yaforster.trails.core.persisted.PersistedStage;
import io.github.yaforster.trails.core.test.Browser;
import io.github.yaforster.trails.core.test.DriverProvider;
import io.github.yaforster.trails.core.test.TestExecutionData;
import io.github.yaforster.trails.core.test.TestPlanRunDefinition;
import org.instancio.Instancio;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.List;
import java.util.Optional;

import static org.instancio.Select.field;

class WebdriverConfigurationFactoryImplTest extends TrailsTest {

	private final TestPlanDatabaseService testPlanDatabaseService = Mockito.mock(TestPlanDatabaseService.class);

	private final StageDatabaseService stageDatabaseService = Mockito.mock(StageDatabaseService.class);

	private final WebdriverProperties settings = Mockito.mock(WebdriverProperties.class);

	private final DriverProvider configuredEdgeDriver = Mockito.mock(DriverProvider.class);

	private final DriverProvider configuredChromeDriver = Mockito.mock(DriverProvider.class);

	private final DriverProvider configuredFirefoxDriver = Mockito.mock(DriverProvider.class);

	private final WebdriverConfigurationFactoryImpl factory = new WebdriverConfigurationFactoryImpl(
			testPlanDatabaseService, stageDatabaseService, settings, configuredEdgeDriver, configuredChromeDriver,
			configuredFirefoxDriver);

	@Test
	void testGetTestExecutionData_success() {
		Mockito.when(settings.browserBaseUrl()).thenReturn("");
		TestPlan testPlan = Instancio.of(TestPlan.class)
			.set(field(TestPlan::getApplicationID), 1L)
			.set(field(TestPlan::getStageID), 2L)
			.set(field(TestPlan::getLabel), "Test Plan Label")
			.set(field(TestPlan::getActions), List.of())
			.create();

		PersistedStage stage = Instancio.of(PersistedStage.class)
			.set(field(PersistedStage::url), "https://example.com")
			.create();

		Mockito.when(testPlanDatabaseService.getExecutableTestPlan(testPlan.getId())).thenReturn(Optional.of(testPlan));
		Mockito
			.when(stageDatabaseService.getStage(
					new StageDatabaseService.StageDetails(testPlan.getApplicationID(), testPlan.getStageID(), true)))
			.thenReturn(Optional.of(stage));
		Mockito.when(configuredChromeDriver.createDriver()).thenReturn(Optional.empty());
		Mockito.when(configuredFirefoxDriver.createDriver()).thenReturn(Optional.empty());

		TestPlanRunDefinition runDefinition = new TestPlanRunDefinition(testPlan.getId(),
				List.of(Browser.CHROME, Browser.FIREFOX));
		TestExecutionData result = factory.getTestExecutionData(runDefinition);

		Assertions.assertNotNull(result);
		Assertions.assertEquals(testPlan.getLabel(), result.testPlanLabel());
		Assertions.assertEquals(testPlan.getApplicationID(), result.applicationID());
		Assertions.assertEquals(testPlan.getStageID(), result.stageID());
		Assertions.assertEquals(testPlan.getId(), result.testPlanID());
		Assertions.assertEquals(2, result.webDrivers().size());
	}

	@Test
	void testGetTestExecutionData_missingTestPlan() {
		TestPlanRunDefinition runDefinition = new TestPlanRunDefinition(1L, List.of(Browser.CHROME));

		Mockito.when(testPlanDatabaseService.getExecutableTestPlan(1L)).thenReturn(Optional.empty());

		Assertions.assertThrows(EntityMissingException.class, () -> factory.getTestExecutionData(runDefinition));
	}

	@Test
	void testGetTestExecutionData_missingStage() {
		Mockito.when(settings.browserBaseUrl()).thenReturn("");
		TestPlan testPlan = Instancio.of(TestPlan.class)
			.set(field(TestPlan::getApplicationID), 1L)
			.set(field(TestPlan::getStageID), 2L)
			.set(field(TestPlan::getLabel), "Test Plan Label")
			.set(field(TestPlan::getActions), List.of())
			.create();

		Mockito.when(testPlanDatabaseService.getExecutableTestPlan(testPlan.getId())).thenReturn(Optional.of(testPlan));
		Mockito
			.when(stageDatabaseService.getStage(
					new StageDatabaseService.StageDetails(testPlan.getApplicationID(), testPlan.getStageID(), true)))
			.thenReturn(Optional.empty());

		TestPlanRunDefinition runDefinition = new TestPlanRunDefinition(testPlan.getId(), List.of(Browser.CHROME));
		Assertions.assertThrows(EntityMissingException.class, () -> factory.getTestExecutionData(runDefinition));
	}

	@Test
	void testGetTestExecutionData_browserConfigurations() {
		Mockito.when(settings.browserBaseUrl()).thenReturn("");
		TestPlan testPlan = Instancio.of(TestPlan.class)
			.set(field(TestPlan::getApplicationID), 1L)
			.set(field(TestPlan::getStageID), 2L)
			.set(field(TestPlan::getLabel), "Test Plan Label")
			.set(field(TestPlan::getActions), List.of())
			.create();

		PersistedStage stage = Instancio.of(PersistedStage.class)
			.set(field(PersistedStage::url), "https://test-url.com")
			.create();

		Mockito.when(testPlanDatabaseService.getExecutableTestPlan(testPlan.getId())).thenReturn(Optional.of(testPlan));
		Mockito
			.when(stageDatabaseService.getStage(
					new StageDatabaseService.StageDetails(testPlan.getApplicationID(), testPlan.getStageID(), true)))
			.thenReturn(Optional.of(stage));
		Mockito.when(configuredEdgeDriver.createDriver()).thenReturn(Optional.empty());
		Mockito.when(configuredChromeDriver.createDriver()).thenReturn(Optional.empty());
		Mockito.when(configuredFirefoxDriver.createDriver()).thenReturn(Optional.empty());

		TestPlanRunDefinition runDefinition = new TestPlanRunDefinition(testPlan.getId(),
				List.of(Browser.EDGE, Browser.CHROME, Browser.FIREFOX));
		TestExecutionData result = factory.getTestExecutionData(runDefinition);

		Assertions.assertNotNull(result.webDrivers());
		Assertions.assertEquals(3, result.webDrivers().size());
		Assertions.assertTrue(result.webDrivers().stream().anyMatch(wd -> wd.browserName() == Browser.EDGE));
		Assertions.assertTrue(result.webDrivers().stream().anyMatch(wd -> wd.browserName() == Browser.CHROME));
		Assertions.assertTrue(result.webDrivers().stream().anyMatch(wd -> wd.browserName() == Browser.FIREFOX));
	}

	@Test
	void findURLOfStageToTestOn_rewritesBrowserBaseUrl_whenConfigured() {
		Mockito.when(settings.browserBaseUrl()).thenReturn("http://trails-service:8080");
		PersistedStage stage = Instancio.of(PersistedStage.class)
			.set(field(PersistedStage::url), "http://localhost:8080/dummy.html?download=true#top")
			.create();
		Mockito.when(stageDatabaseService.getStage(new StageDatabaseService.StageDetails(1L, 2L, true)))
			.thenReturn(Optional.of(stage));

		String result = factory.findURLOfStageToTestOn(1L, 2L).toString();

		Assertions.assertEquals("http://trails-service:8080/dummy.html?download=true#top", result);
	}

	@Test
	void findURLOfStageToTestOn_keepsExternalUrl_whenBrowserBaseUrlConfigured() {
		Mockito.when(settings.browserBaseUrl()).thenReturn("http://trails-service:8080");
		PersistedStage stage = Instancio.of(PersistedStage.class)
			.set(field(PersistedStage::url), "https://example.com/app")
			.create();
		Mockito.when(stageDatabaseService.getStage(new StageDatabaseService.StageDetails(1L, 2L, true)))
			.thenReturn(Optional.of(stage));

		String result = factory.findURLOfStageToTestOn(1L, 2L).toString();

		Assertions.assertEquals("https://example.com/app", result);
	}

}
