package io.github.yaforster.trails.adapter.test.webdriver.browser;

import io.github.yaforster.trails.core.test.BrowserElementText;
import io.github.yaforster.trails.core.test.BrowserSession;
import io.github.yaforster.trails.core.test.FileData;
import io.github.yaforster.trails.core.test.action.browser.viewport.ViewportMove;
import io.github.yaforster.trails.core.test.action.browser.viewport.ViewportMoveDirection;
import io.github.yaforster.trails.core.test.action.browser.viewport.ViewportMoveUnit;
import io.github.yaforster.trails.core.test.action.browser.ViewportCoordinates;
import io.github.yaforster.trails.core.test.action.browser.viewport.ViewportDimensions;
import io.github.yaforster.trails.core.test.action.browser.viewport.ViewportResizeOutcome;
import io.github.yaforster.trails.core.test.action.element.ElementValueSource;
import io.github.yaforster.trails.core.test.action.element.Locator;
import org.openqa.selenium.By;
import org.openqa.selenium.HasDownloads;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.StaleElementReferenceException;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.PointerInput;
import org.openqa.selenium.interactions.Interactive;
import org.openqa.selenium.interactions.Sequence;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;

public class SeleniumBrowserSession implements BrowserSession {

	private static final Duration ELEMENT_LOOKUP_TIMEOUT = Duration.ofSeconds(5);

	private static final Duration ELEMENT_LOOKUP_RETRY_INTERVAL = Duration.ofMillis(100);

	private static final Set<String> NATIVE_INTERACTABLE_TAGS = Set.of("button", "a", "input", "textarea", "select");

	private static final By INTERACTABLE_CHILD_LOCATOR = By
		.cssSelector("button, a, input, textarea, select, [role='button']");

	private static final String SCROLL_TO_ELEMENT = """
			var viewPortHeight = Math.max(document.documentElement.clientHeight, window.innerHeight || 0);
			var elementTop = arguments[0].getBoundingClientRect().top;
			window.scrollBy(0, elementTop-(viewPortHeight/2));
			""";

	private static final String RENDER_HTML = """
			const proofDocument = new DOMParser().parseFromString(arguments[0], 'text/html');
			const headNodes = Array.from(proofDocument.head.childNodes).map(node => document.importNode(node, true));
			const bodyNodes = Array.from(proofDocument.body.childNodes).map(node => document.importNode(node, true));
			document.documentElement.lang = proofDocument.documentElement.lang;
			document.head.replaceChildren(...headNodes);
			document.body.replaceChildren(...bodyNodes);
			const proofImage = document.querySelector('main img');
			window.scrollTo(0, (proofImage ? proofImage.offsetTop : 0) + arguments[1]);
			""";

	private final WebDriver driver;

	private final HasDownloads downloads;

	private final int downloadTimeoutMillis;

	private final SeleniumViewportOperations viewportOperations;

	public SeleniumBrowserSession(WebDriver driver, int downloadTimeoutMillis) {
		this.driver = driver;
		this.downloads = downloadsFrom(driver);
		this.downloadTimeoutMillis = downloadTimeoutMillis;
		this.viewportOperations = new SeleniumViewportOperations(driver);
	}

	@Override
	public void open(String url) {
		driver.navigate().to(url);
	}

	@Override
	public String captureScreenshot() {
		return ((TakesScreenshot) driver).getScreenshotAs(OutputType.BASE64);
	}

	@Override
	public String captureScreenshot(Locator locator) {
		try {
			findElementIfPresent(locator).ifPresent(element -> javascript().executeScript(SCROLL_TO_ELEMENT, element));
		}
		catch (StaleElementReferenceException ignored) {
		}
		return captureScreenshot();
	}

	@Override
	public boolean elementExists(Locator locator) {
		try {
			return waitForElement().until(ignored -> findElementIfPresent(locator).isPresent());
		}
		catch (TimeoutException ignored) {
			return false;
		}
	}

	@Override
	public String click(Locator locator) {
		return withFreshElement(locator, element -> {
			WebElement interactable = resolveInteractableElement(element);
			String tagName = tagName(interactable);
			interactable.click();
			return tagName;
		});
	}

	@Override
	public void click(ViewportCoordinates coordinates) {
		PointerInput pointer = new PointerInput(PointerInput.Kind.MOUSE, "coordinate-click");
		Sequence sequence = new Sequence(pointer, 0)
			.addAction(pointer.createPointerMove(Duration.ZERO, PointerInput.Origin.viewport(),
					coordinates.xCoordinate(), coordinates.yCoordinate()))
			.addAction(pointer.createPointerDown(PointerInput.MouseButton.LEFT.asArg()))
			.addAction(pointer.createPointerUp(PointerInput.MouseButton.LEFT.asArg()));
		((Interactive) driver).perform(List.of(sequence));
	}

	@Override
	public ViewportResizeOutcome resizeViewport(ViewportDimensions dimensions) {
		return viewportOperations.resize(dimensions);
	}

	@Override
	public String type(Locator locator, String value, boolean clearBeforeTyping, long delayAfterClearMillis) {
		return withFreshElement(locator, element -> {
			WebElement interactable = resolveInteractableElement(element);
			if (clearBeforeTyping) {
				interactable.clear();
				waitAfterClear(delayAfterClearMillis);
			}
			String tagName = tagName(interactable);
			interactable.sendKeys(value);
			return tagName;
		});
	}

	@Override
	public BrowserElementText readText(Locator locator) {
		return withFreshElement(locator,
				element -> new BrowserElementText(tagName(element), element.getText().toLowerCase()));
	}

	@Override
	public String readValue(Locator locator, ElementValueSource valueSource, String valueName) {
		return withFreshElement(locator, element -> switch (valueSource) {
			case ATTRIBUTE -> element.getAttribute(valueName);
			case CSS_VALUE -> element.getCssValue(valueName);
			case PROPERTY -> element.getDomProperty(valueName);
		});
	}

	@Override
	public String select(Locator locator, String label) {
		return withFreshElement(locator, element -> select(element, label));
	}

	@Override
	public Optional<String> cookieValue(String name) {
		return Optional.ofNullable(driver.manage().getCookieNamed(name)).map(cookie -> cookie.getValue());
	}

	@Override
	public String localStorageValue(String key) {
		return (String) javascript().executeScript("return window.localStorage.getItem(arguments[0]);", key);
	}

	@Override
	public String sessionStorageValue(String key) {
		return (String) javascript().executeScript("return window.sessionStorage.getItem(arguments[0]);", key);
	}

	@Override
	public void moveViewport(ViewportMove movement, ViewportMoveDirection direction, double amount,
			ViewportMoveUnit unit) {
		viewportOperations.move(movement, direction, amount, unit);
	}

	@Override
	public void renderHtml(String html, int scrollTop) {
		javascript().executeScript(RENDER_HTML, html, scrollTop);
	}

	@Override
	public List<FileData> refreshDownloadedFiles() {
		return new SeleniumDownloadCollector(driver, downloads, downloadTimeoutMillis).getDownloadedFiles();
	}

	@Override
	public void clearDownloadedFiles() {
		downloads.deleteDownloadableFiles();
	}

	@Override
	public void close() {
		driver.quit();
	}

	private String select(WebElement element, String label) {
		if ("select".equalsIgnoreCase(element.getTagName())) {
			new Select(element).selectByVisibleText(label);
			return tagName(element);
		}
		return new AriaComboboxSelect(driver).select(element, label);
	}

	private <T> T withFreshElement(Locator locator, Function<WebElement, T> operation) {
		return waitForElement().until(ignored -> {
			try {
				return operation.apply(driver.findElement(by(locator)));
			}
			catch (StaleElementReferenceException exception) {
				return null;
			}
		});
	}

	private WebDriverWait waitForElement() {
		WebDriverWait wait = new WebDriverWait(driver, ELEMENT_LOOKUP_TIMEOUT);
		wait.pollingEvery(ELEMENT_LOOKUP_RETRY_INTERVAL);
		wait.ignoring(StaleElementReferenceException.class);
		return wait;
	}

	private Optional<WebElement> findElementIfPresent(Locator locator) {
		try {
			return Optional.ofNullable(driver.findElement(by(locator)));
		}
		catch (NoSuchElementException | StaleElementReferenceException ignored) {
			return Optional.empty();
		}
	}

	private By by(Locator locator) {
		return switch (locator.type()) {
			case CSS -> By.cssSelector(locator.locatorString());
			case XPATH -> By.xpath(locator.locatorString());
		};
	}

	private WebElement resolveInteractableElement(WebElement element) {
		if (isInteractable(element) && isNativeInteractable(element)) {
			return element;
		}
		return element.findElements(INTERACTABLE_CHILD_LOCATOR)
			.stream()
			.filter(this::isInteractable)
			.findFirst()
			.orElse(element);
	}

	private boolean isNativeInteractable(WebElement element) {
		String tagName = element.getTagName();
		return tagName != null && NATIVE_INTERACTABLE_TAGS.contains(tagName.toLowerCase(Locale.ROOT))
				|| "button".equalsIgnoreCase(element.getDomAttribute("role"));
	}

	private boolean isInteractable(WebElement element) {
		try {
			return element.isDisplayed() && element.isEnabled();
		}
		catch (StaleElementReferenceException ignored) {
			return false;
		}
	}

	private String tagName(WebElement element) {
		String tagName = element.getTagName();
		return tagName == null || tagName.isBlank() ? "Element" : tagName;
	}

	private void waitAfterClear(long delayMillis) {
		if (delayMillis <= 0) {
			return;
		}
		try {
			Thread.sleep(delayMillis);
		}
		catch (InterruptedException exception) {
			Thread.currentThread().interrupt();
			throw new IllegalStateException("Typing action was interrupted while waiting after clearing the input.",
					exception);
		}
	}

	private JavascriptExecutor javascript() {
		return (JavascriptExecutor) driver;
	}

	private static HasDownloads downloadsFrom(WebDriver driver) {
		if (driver instanceof HasDownloads managedDownloads) {
			return managedDownloads;
		}
		throw new IllegalArgumentException("WebDriver must support Selenium managed downloads.");
	}

}
