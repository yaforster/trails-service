package io.github.yaforster.trails.core.test.action.element;

import io.github.yaforster.trails.core.test.action.browser.WebpageAction;

import io.github.yaforster.trails.core.test.Result;
import io.github.yaforster.trails.core.test.TestExecutionContext;
import io.github.yaforster.trails.core.test.result.success.Success;
import lombok.experimental.SuperBuilder;

@SuperBuilder
public class ClickAction extends WebpageAction {

	@Override
	public Result execute(TestExecutionContext context) {
		try {
			return runAction(context);
		}
		catch (Exception e) {
			return generateTechnicalFailureResult(e, getTechnicalFailureMessage(), context);
		}
	}

	private Success runAction(TestExecutionContext context) {
		String clickedElementTagName = context.browser().click(getLocatorForElementToActOn());
		return buildSuccess(clickedElementTagName, context);
	}

	private Success buildSuccess(String clickedElementTagName, TestExecutionContext context) {
		Success.SuccessBuilder<?, ?> builder = Success.builder()
			.actionID(getActionID())
			.label(getLabel())
			.resultMessage(getSuccessMessage(clickedElementTagName))
			.base64Screenshot(getPageScreenshot(context));
		return builder.build();
	}

	private String getSuccessMessage(String clickedElementTagName) {
		return clickedElementTagName + " was successfully clicked on.";
	}

}
