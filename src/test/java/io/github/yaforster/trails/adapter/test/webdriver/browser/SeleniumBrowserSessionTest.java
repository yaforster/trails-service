package io.github.yaforster.trails.adapter.test.webdriver.browser;

import io.github.yaforster.trails.core.test.action.element.Locator;
import io.github.yaforster.trails.core.test.action.element.LocatorType;
import io.github.yaforster.trails.core.test.action.browser.ViewportCoordinates;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.HasDownloads;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Sequence;
import org.openqa.selenium.interactions.Interactive;

import java.util.List;
import java.util.Collection;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import org.mockito.ArgumentCaptor;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.withSettings;

class SeleniumBrowserSessionTest {

	@Test
	void select_usesNativeHtmlSelectForSelectElements() {
		WebDriver driver = managedDownloadDriver();
		WebElement select = mock(WebElement.class);
		WebElement option = mock(WebElement.class);
		when(driver.findElement(any())).thenReturn(select);
		when(select.getTagName()).thenReturn("select");
		when(select.getDomAttribute("multiple")).thenReturn(null);
		when(select.isEnabled()).thenReturn(true);
		when(select.getCssValue(any())).thenReturn("visible");
		when(select.findElements(any())).thenReturn(List.of(option));
		when(option.getText()).thenReturn("Option A");
		when(option.isSelected()).thenReturn(false);
		when(option.isEnabled()).thenReturn(true);
		when(option.getCssValue(any())).thenReturn("visible");

		String result = new SeleniumBrowserSession(driver, 100).select(locator(), "Option A");

		assertEquals("select", result);
	}

	@Test
	void select_usesAriaComboboxForCustomControls() {
		WebDriver driver = managedDownloadDriver();
		WebElement combobox = mock(WebElement.class);
		WebElement option = mock(WebElement.class);
		when(driver.findElement(any())).thenReturn(combobox);
		when(combobox.getTagName()).thenReturn("p-select");
		when(combobox.getDomAttribute("role")).thenReturn("combobox");
		when(driver.findElements(any())).thenReturn(List.of(option));
		when(option.isDisplayed()).thenReturn(true);
		when(option.getText()).thenReturn("Option A");

		new SeleniumBrowserSession(driver, 100).select(locator(), "Option A");

		verify(option).click();
	}

	@Test
	void clearDownloadedFiles_delegatesToSeleniumManagedDownloads() {
		WebDriver driver = managedDownloadDriver();
		HasDownloads downloads = (HasDownloads) driver;

		new SeleniumBrowserSession(driver, 100).clearDownloadedFiles();

		verify(downloads).deleteDownloadableFiles();
	}

	@Test
	void close_quitsTheSeleniumSession() {
		WebDriver driver = managedDownloadDriver();

		new SeleniumBrowserSession(driver, 100).close();

		verify(driver).quit();
	}

	@Test
	void click_performsOneAbsoluteViewportPointerSequence() {
		WebDriver driver = managedDownloadDriver();

		new SeleniumBrowserSession(driver, 100).click(new ViewportCoordinates(12, 34));

		ArgumentCaptor<Collection<Sequence>> sequences = ArgumentCaptor.forClass(Collection.class);
		verify((Interactive) driver).perform(sequences.capture());
		Sequence sequence = sequences.getValue().iterator().next();
		Map<String, Object> encoded = sequence.encode();
		List<Map<String, Object>> actions = (List<Map<String, Object>>) encoded.get("actions");
		assertEquals(3, actions.size());
		assertEquals("pointerMove", actions.getFirst().get("type"));
		assertEquals("viewport", actions.getFirst().get("origin"));
		assertEquals(12, actions.getFirst().get("x"));
		assertEquals(34, actions.getFirst().get("y"));
		assertEquals("pointerDown", actions.get(1).get("type"));
		assertEquals("pointerUp", actions.get(2).get("type"));
	}

	private static WebDriver managedDownloadDriver() {
		return mock(WebDriver.class, withSettings().extraInterfaces(HasDownloads.class, Interactive.class));
	}

	private static Locator locator() {
		return new Locator(LocatorType.CSS, "#target");
	}

}
