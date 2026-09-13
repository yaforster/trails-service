package io.github.yaforster.trails.adapter.test.webdriver.browser;

import io.github.yaforster.trails.core.test.action.browser.viewport.ViewportDimensions;
import io.github.yaforster.trails.core.test.action.browser.viewport.ViewportResizeOutcome;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.Dimension;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.withSettings;

class SeleniumViewportResizerTest {

	@Test
	void resize_compensatesOuterWindowOnceAndReturnsExactSuccess() {
		WebDriver driver = driver(List.of(100, 100), List.of(800, 600));
		WebDriver.Window window = window(driver, new Dimension(120, 140));

		ViewportResizeOutcome outcome = new SeleniumViewportResizer(driver).resize(new ViewportDimensions(800, 600));

		ViewportResizeOutcome.Success success = assertInstanceOf(ViewportResizeOutcome.Success.class, outcome);
		assertEquals(new ViewportDimensions(800, 600), success.measured());
		verify(window).setSize(new Dimension(820, 640));
	}

	@Test
	void resize_returnsMeasuredTechnicalFailureWhenOneSetSizeCannotReachTarget() {
		WebDriver driver = driver(List.of(100, 100), List.of(799, 600));
		WebDriver.Window window = window(driver, new Dimension(120, 140));

		ViewportResizeOutcome.Failure failure = assertInstanceOf(ViewportResizeOutcome.Failure.class,
				new SeleniumViewportResizer(driver).resize(new ViewportDimensions(800, 600)));

		assertEquals(new ViewportDimensions(799, 600), failure.lastMeasured().orElseThrow());
		assertTrue(failure.cause().contains("one outer-window resize attempt"));
		verify(window).setSize(new Dimension(820, 640));
	}

	@Test
	void resize_remeasuresOnlyForDiagnosticsAfterSetSizeFailure() {
		WebDriver driver = driver(List.of(100, 100), List.of(101, 100));
		WebDriver.Window window = window(driver, new Dimension(120, 140));
		doThrow(new IllegalStateException("window manager rejected resize")).when(window).setSize(any());

		ViewportResizeOutcome.Failure failure = assertInstanceOf(ViewportResizeOutcome.Failure.class,
				new SeleniumViewportResizer(driver).resize(new ViewportDimensions(800, 600)));

		assertEquals(new ViewportDimensions(101, 100), failure.lastMeasured().orElseThrow());
		assertTrue(failure.cause().contains("window manager rejected resize"));
		verify(window).setSize(new Dimension(820, 640));
		verify((JavascriptExecutor) driver, org.mockito.Mockito.times(2)).executeScript(anyString());
	}

	@Test
	void resize_returnsInitialMeasurementWhenPostResizeMeasurementFails() {
		WebDriver driver = driver(List.of(100, 100), new IllegalStateException("script unavailable"));
		WebDriver.Window window = window(driver, new Dimension(120, 140));

		ViewportResizeOutcome.Failure failure = assertInstanceOf(ViewportResizeOutcome.Failure.class,
				new SeleniumViewportResizer(driver).resize(new ViewportDimensions(800, 600)));

		assertEquals(new ViewportDimensions(100, 100), failure.lastMeasured().orElseThrow());
		assertTrue(failure.cause().contains("script unavailable"));
		verify(window).setSize(new Dimension(820, 640));
	}

	@Test
	void resize_returnsUnavailableMeasurementWhenInitialMeasurementIsMalformed() {
		WebDriver driver = driver(List.of("bad", 100));

		ViewportResizeOutcome.Failure failure = assertInstanceOf(ViewportResizeOutcome.Failure.class,
				new SeleniumViewportResizer(driver).resize(new ViewportDimensions(800, 600)));

		assertTrue(failure.lastMeasured().isEmpty());
		assertTrue(failure.cause().contains("non-numeric"));
		verify(driver, never()).manage();
	}

	@Test
	void resize_returnsInitialMeasurementWhenCompensatedOuterDimensionOverflows() {
		WebDriver driver = driver(List.of(100, 100));
		WebDriver.Window window = window(driver, new Dimension(101, 101));

		ViewportResizeOutcome.Failure failure = assertInstanceOf(ViewportResizeOutcome.Failure.class,
				new SeleniumViewportResizer(driver)
					.resize(new ViewportDimensions(Integer.MAX_VALUE, Integer.MAX_VALUE)));

		assertEquals(new ViewportDimensions(100, 100), failure.lastMeasured().orElseThrow());
		assertTrue(failure.cause().contains("outside Selenium Dimension range"));
		verify(window, never()).setSize(any());
	}

	private static WebDriver driver(Object... measurements) {
		WebDriver driver = mock(WebDriver.class, withSettings().extraInterfaces(JavascriptExecutor.class));
		when(((JavascriptExecutor) driver).executeScript(anyString())).thenReturn(measurements[0],
				measurements.length > 1 ? measurements[1] : null);
		if (measurements.length > 1 && measurements[1] instanceof RuntimeException exception) {
			when(((JavascriptExecutor) driver).executeScript(anyString())).thenReturn(measurements[0])
				.thenThrow(exception);
		}
		return driver;
	}

	private static WebDriver.Window window(WebDriver driver, Dimension size) {
		WebDriver.Options options = mock(WebDriver.Options.class);
		WebDriver.Window window = mock(WebDriver.Window.class);
		when(driver.manage()).thenReturn(options);
		when(options.window()).thenReturn(window);
		when(window.getSize()).thenReturn(size);
		return window;
	}

}
