package io.github.yaforster.trails.adapter.test.webdriver.browser;

import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;

final class AriaComboboxSelect {

	private static final By OPTION = By.cssSelector("[role='option']");

	private final WebDriver driver;

	AriaComboboxSelect(WebDriver driver) {
		this.driver = driver;
	}

	String select(WebElement element, String label) {
		if (!"combobox".equalsIgnoreCase(element.getDomAttribute("role"))) {
			throw new NoSuchElementException("No supported select control found for element: " + element.getTagName());
		}
		element.click();
		try {
			new WebDriverWait(driver, Duration.ofSeconds(5)).until(ignored -> driver.findElements(OPTION)
				.stream()
				.filter(option -> option.isDisplayed() && label.equals(option.getText()))
				.findFirst()
				.map(option -> {
					option.click();
					return true;
				})
				.orElse(false));
		}
		catch (TimeoutException exception) {
			throw new NoSuchElementException("No option with label '" + label + "' was found.", exception);
		}
		return element.getTagName();
	}

}
