package io.github.yaforster.trails.core.test.action.element;

import io.github.yaforster.trails.core.test.action.value.ValueComputationInstruction;

import io.github.yaforster.trails.core.test.action.browser.WebpageAction;

import io.github.yaforster.trails.core.test.Result;
import io.github.yaforster.trails.core.test.TestExecutionContext;
import io.github.yaforster.trails.core.test.BrowserElementText;
import io.github.yaforster.trails.core.test.result.failure.ValidationFailure;
import io.github.yaforster.trails.core.test.result.success.Success;
import lombok.Getter;
import lombok.experimental.SuperBuilder;

@SuperBuilder

@Getter
public class TextCheckAction extends WebpageAction {

	private final ValueComputationInstruction computationInstruction;

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
		String textToSearchFor = computationInstruction.computeValue();
		BrowserElementText elementText = context.browser().readText(getLocatorForElementToActOn());
		if (elementText.text().contains(textToSearchFor)) {
			return buildSuccess(context, elementText.tagName());
		}
		else {
			return buildValidationFailure(context);
		}
	}

	private Success buildSuccess(TestExecutionContext context, String textElementTagName) {
		Success.SuccessBuilder<?, ?> builder = Success.builder()
			.actionID(getActionID())
			.label(getLabel())
			.resultMessage(getSuccessMessage(textElementTagName))
			.base64Screenshot(getPageScreenshot(context));
		return builder.build();
	}

	private ValidationFailure buildValidationFailure(TestExecutionContext context) {
		ValidationFailure.ValidationFailureBuilder<?, ?> builder = ValidationFailure.builder()
			.actionID(getActionID())
			.label(getLabel())
			.resultMessage("Text element was found but does not contain the search string.")
			.base64Screenshot(getPageScreenshot(context));
		return builder.build();
	}

	private String getSuccessMessage(String textElementTagName) {
		return "'The search string was successfully located in " + textElementTagName + ".";
	}

}
