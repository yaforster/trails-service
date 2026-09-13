package io.github.yaforster.trails.core.test.action.browser;

import io.github.yaforster.trails.core.test.action.element.Locator;
import io.github.yaforster.trails.core.test.Action;
import io.github.yaforster.trails.core.test.TestExecutionContext;
import lombok.Getter;
import lombok.experimental.SuperBuilder;

@SuperBuilder
@Getter
public abstract class WebpageAction extends Action {

	protected final Locator locatorForElementToActOn;

	protected String getTechnicalFailureMessage() {
		return "Webpage Element was not found using locator: '" + locatorForElementToActOn.locatorString() + "'.";
	}

	@Override
	protected String getPageScreenshot(TestExecutionContext context) {
		return context.browser().captureScreenshot(locatorForElementToActOn);
	}

}
