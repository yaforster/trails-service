package io.github.yaforster.trails.core.test.action.element;

import io.github.yaforster.trails.core.test.action.browser.WebpageAction;

import io.github.yaforster.trails.core.test.Result;
import io.github.yaforster.trails.core.test.TestExecutionContext;
import io.github.yaforster.trails.core.test.result.failure.ValidationFailure;
import io.github.yaforster.trails.core.test.result.success.Success;
import lombok.experimental.SuperBuilder;

@SuperBuilder
public class CheckExistenceAction extends WebpageAction {

	@Override
	public Result execute(TestExecutionContext context) {
		try {
			return runAction(context);
		}
		catch (Exception e) {
			return generateTechnicalFailureResult(e, getTechnicalFailureMessage(), context);
		}
	}

	private Result runAction(TestExecutionContext context) {
		if (context.browser().elementExists(getLocatorForElementToActOn())) {
			return buildSuccess(context);
		}
		return buildElementDoesNotExistFailure(context);
	}

	private Success buildSuccess(TestExecutionContext context) {
		return Success.builder()
			.actionID(getActionID())
			.label(getLabel())
			.resultMessage(getSuccessMessage())
			.base64Screenshot(captureScreenshotIfAvailable(context))
			.build();
	}

	private ValidationFailure buildElementDoesNotExistFailure(TestExecutionContext context) {
		return ValidationFailure.builder()
			.actionID(getActionID())
			.resultMessage(getElementDoesNotExistMessage())
			.label(getLabel())
			.base64Screenshot(captureScreenshotIfAvailable(context))
			.build();
	}

	private String getSuccessMessage() {
		return "Successfully located an element on the current website using the "
				+ getLocatorForElementToActOn().type().name() + " locator: "
				+ getLocatorForElementToActOn().locatorString() + ".";
	}

	private String getElementDoesNotExistMessage() {
		return "No element could be found on the current website using the "
				+ getLocatorForElementToActOn().type().name() + " locator: "
				+ getLocatorForElementToActOn().locatorString() + ".";
	}

}
