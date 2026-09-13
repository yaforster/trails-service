package io.github.yaforster.trails.adapter.test.webdriver.browser;

import io.github.yaforster.trails.core.test.action.browser.viewport.ViewportDimensions;
import io.github.yaforster.trails.core.test.action.browser.viewport.ViewportMove;
import io.github.yaforster.trails.core.test.action.browser.viewport.ViewportMoveDirection;
import io.github.yaforster.trails.core.test.action.browser.viewport.ViewportMoveUnit;
import io.github.yaforster.trails.core.test.action.browser.viewport.ViewportResizeOutcome;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

class SeleniumViewportOperations {

	private final WebDriver driver;

	private final SeleniumViewportResizer resizer;

	SeleniumViewportOperations(WebDriver driver) {
		this.driver = driver;
		this.resizer = new SeleniumViewportResizer(driver);
	}

	ViewportResizeOutcome resize(ViewportDimensions dimensions) {
		return resizer.resize(dimensions);
	}

	void move(ViewportMove movement, ViewportMoveDirection direction, double amount, ViewportMoveUnit unit) {
		switch (movement) {
			case SCROLL_BY -> scrollBy(direction, amount, unit);
			case PAGE_FLIP -> pageFlip(direction, amount);
			case SCROLL_TO_TOP -> javascript().executeScript("window.scrollTo(0, 0);");
			case SCROLL_TO_BOTTOM -> javascript().executeScript(
					"window.scrollTo(0, document.body.scrollHeight || document.documentElement.scrollHeight);");
		}
	}

	private void scrollBy(ViewportMoveDirection direction, double amount, ViewportMoveUnit unit) {
		double pixels = amount;
		if (unit == ViewportMoveUnit.VIEWPORTS) {
			Object height = javascript()
				.executeScript("return window.innerHeight || document.documentElement.clientHeight || 0;");
			if (height instanceof Number number) {
				pixels *= number.doubleValue();
			}
		}
		javascript().executeScript("window.scrollBy(0, arguments[0]);",
				direction == ViewportMoveDirection.UP ? -pixels : pixels);
	}

	private void pageFlip(ViewportMoveDirection direction, double amount) {
		CharSequence key = direction == ViewportMoveDirection.UP ? Keys.PAGE_UP : Keys.PAGE_DOWN;
		WebElement activeElement = driver.switchTo().activeElement();
		for (int i = 0; i < Math.max(0, (int) Math.round(amount)); i++) {
			activeElement.sendKeys(key);
		}
	}

	private JavascriptExecutor javascript() {
		return (JavascriptExecutor) driver;
	}

}
