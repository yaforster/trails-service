package io.github.yaforster.trails.adapter.test.webdriver.browser;

import io.github.yaforster.trails.core.test.action.browser.viewport.ViewportDimensions;
import io.github.yaforster.trails.core.test.action.browser.viewport.ViewportResizeOutcome;
import org.openqa.selenium.Dimension;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;

import java.util.List;
import java.util.Optional;

class SeleniumViewportResizer {

	private final WebDriver driver;

	SeleniumViewportResizer(WebDriver driver) {
		this.driver = driver;
	}

	ViewportResizeOutcome resize(ViewportDimensions requested) {
		ViewportDimensions initial;
		try {
			initial = measureViewport();
		}
		catch (RuntimeException exception) {
			return failure(requested, Optional.empty(), "Could not measure current viewport", exception);
		}

		Dimension outerWindow;
		try {
			outerWindow = driver.manage().window().getSize();
		}
		catch (RuntimeException exception) {
			return failure(requested, Optional.of(initial), "Could not read current outer window size", exception);
		}

		Dimension targetOuterWindow;
		try {
			targetOuterWindow = compensatedOuterWindow(requested, initial, outerWindow);
		}
		catch (RuntimeException exception) {
			return failure(requested, Optional.of(initial), "Could not calculate compensated outer window size",
					exception);
		}

		try {
			driver.manage().window().setSize(targetOuterWindow);
		}
		catch (RuntimeException exception) {
			return failedSetSize(requested, initial, exception);
		}

		try {
			ViewportDimensions measured = measureViewport();
			if (requested.equals(measured)) {
				return new ViewportResizeOutcome.Success(requested, measured);
			}
			return new ViewportResizeOutcome.Failure(requested, Optional.of(measured),
					"Browser did not reach requested viewport dimensions after one outer-window resize attempt.");
		}
		catch (RuntimeException exception) {
			return failure(requested, Optional.of(initial), "Could not measure viewport after resize", exception);
		}
	}

	private ViewportResizeOutcome.Failure failedSetSize(ViewportDimensions requested, ViewportDimensions initial,
			RuntimeException setSizeFailure) {
		try {
			ViewportDimensions measured = measureViewport();
			return failure(requested, Optional.of(measured), "Could not set outer window size", setSizeFailure);
		}
		catch (RuntimeException measurementFailure) {
			return new ViewportResizeOutcome.Failure(requested, Optional.of(initial),
					cause("Could not set outer window size", setSizeFailure)
							+ " Diagnostic viewport measurement unavailable: " + cause(measurementFailure));
		}
	}

	private Dimension compensatedOuterWindow(ViewportDimensions requested, ViewportDimensions initial,
			Dimension outerWindow) {
		long horizontalDecoration = Math.subtractExact((long) outerWindow.getWidth(), initial.width());
		long verticalDecoration = Math.subtractExact((long) outerWindow.getHeight(), initial.height());
		long targetWidth = Math.addExact(requested.width(), horizontalDecoration);
		long targetHeight = Math.addExact(requested.height(), verticalDecoration);
		if (targetWidth < 1 || targetWidth > Integer.MAX_VALUE || targetHeight < 1
				|| targetHeight > Integer.MAX_VALUE) {
			throw new ArithmeticException("Compensated outer window dimensions are outside Selenium Dimension range.");
		}
		return new Dimension(Math.toIntExact(targetWidth), Math.toIntExact(targetHeight));
	}

	private ViewportDimensions measureViewport() {
		Object measurement = javascript().executeScript("return [window.innerWidth, window.innerHeight];");
		if (!(measurement instanceof List<?> values) || values.size() != 2) {
			throw new IllegalStateException("Browser did not return two viewport dimensions.");
		}
		return new ViewportDimensions(measuredDimension(values.get(0)), measuredDimension(values.get(1)));
	}

	private int measuredDimension(Object value) {
		if (!(value instanceof Number number)) {
			throw new IllegalStateException("Browser returned a non-numeric viewport dimension.");
		}
		double dimension = number.doubleValue();
		if (!Double.isFinite(dimension) || Math.rint(dimension) != dimension || dimension < 1
				|| dimension > Integer.MAX_VALUE) {
			throw new IllegalStateException("Browser returned an invalid viewport dimension.");
		}
		return (int) dimension;
	}

	private ViewportResizeOutcome.Failure failure(ViewportDimensions requested, Optional<ViewportDimensions> measured,
			String operation, RuntimeException exception) {
		return new ViewportResizeOutcome.Failure(requested, measured, cause(operation, exception));
	}

	private String cause(String operation, RuntimeException exception) {
		return operation + ": " + cause(exception);
	}

	private String cause(RuntimeException exception) {
		return exception.getMessage() == null || exception.getMessage().isBlank()
				? "browser driver did not provide a cause" : exception.getMessage();
	}

	private JavascriptExecutor javascript() {
		return (JavascriptExecutor) driver;
	}

}
