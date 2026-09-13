package io.github.yaforster.trails.core.test;

import io.github.yaforster.trails.core.test.action.element.CheckExistenceAction;
import io.github.yaforster.trails.core.test.action.element.Locator;
import io.github.yaforster.trails.core.test.action.element.LocatorType;
import io.github.yaforster.trails.core.test.result.failure.SkippedResult;
import org.junit.jupiter.api.Test;

import java.net.URL;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TestCaseTest {

	@Test
	void runTestsUntilFailure_skipsEveryAction_whenSessionCannotBeCreated() throws Exception {
		TestCase testCase = new TestCase(new URL("https://example.test"), steps());

		TestPathResult result = testCase.runTestsUntilFailure(() -> Optional.empty(), List.of(steps()));

		assertInstanceOf(SkippedResult.class, result.results().getFirst());
	}

	@Test
	void runTestsUntilFailure_skipsEveryAction_whenNavigationFails() throws Exception {
		TestExecutionContexts.FakeBrowserSession browser = new TestExecutionContexts.FakeBrowserSession()
			.operationFailure(new IllegalStateException("unavailable"));
		BrowserExecution execution = new BrowserExecution(browser, new TestExecutionContexts.FakeDocumentExecution());
		TestCase testCase = new TestCase(new URL("https://example.test"), steps());

		TestPathResult result = testCase.runTestsUntilFailure(() -> Optional.of(execution), List.of(steps()));

		assertInstanceOf(SkippedResult.class, result.results().getFirst());
	}

	@Test
	void runTestsUntilFailure_cleansUpDownloadsAndBrowser_whenNavigationFails() throws Exception {
		TestExecutionContexts.FakeBrowserSession browser = new TestExecutionContexts.FakeBrowserSession()
			.operationFailure(new IllegalStateException("unavailable"));
		BrowserExecution execution = new BrowserExecution(browser, new TestExecutionContexts.FakeDocumentExecution());
		TestCase testCase = new TestCase(new URL("https://example.test"), steps());

		testCase.runTestsUntilFailure(() -> Optional.of(execution), List.of(steps()));

		assertTrue(browser.downloadsCleared() && browser.closed());
	}

	@Test
	void runTestsUntilFailure_opensTheConfiguredApplicationUrl() throws Exception {
		TestExecutionContexts.FakeBrowserSession browser = new TestExecutionContexts.FakeBrowserSession();
		BrowserExecution execution = new BrowserExecution(browser, new TestExecutionContexts.FakeDocumentExecution());
		TestCase testCase = new TestCase(new URL("https://example.test"), steps());

		testCase.runTestsUntilFailure(() -> Optional.of(execution), List.of(steps()));

		assertEquals(List.of("https://example.test"), browser.openedUrls());
	}

	private static TestStep steps() {
		CheckExistenceAction action = CheckExistenceAction.builder()
			.actionID(1L)
			.label("exists")
			.nextActions(List.of())
			.locatorForElementToActOn(new Locator(LocatorType.CSS, "#target"))
			.build();
		return new TestStep(action, List.of());
	}

}
