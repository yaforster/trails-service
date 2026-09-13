package io.github.yaforster.trails.core.test.action.element;

import io.github.yaforster.trails.core.test.action.value.ValueComputationInstruction;

import io.github.yaforster.trails.core.test.action.browser.WebpageAction;

import io.github.yaforster.trails.core.test.Result;
import io.github.yaforster.trails.core.test.TestExecutionContext;
import io.github.yaforster.trails.core.test.result.success.Success;
import lombok.Getter;
import lombok.experimental.SuperBuilder;

@Getter

@SuperBuilder
public class TypingAction extends WebpageAction {

	private final ValueComputationInstruction computationInstruction;

	private final boolean clearBeforeTyping;

	private final long delayAfterClearMillis;

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
		String charactersToEnter = computationInstruction.computeValue();
		String typedElementTagName = context.browser()
			.type(getLocatorForElementToActOn(), charactersToEnter, clearBeforeTyping, delayAfterClearMillis);
		return buildSuccess(context, typedElementTagName);
	}

	private Success buildSuccess(TestExecutionContext context, String typedElementTagName) {
		Success.SuccessBuilder<?, ?> builder = Success.builder()
			.actionID(getActionID())
			.label(getLabel())
			.resultMessage(getSuccessMessage(typedElementTagName))
			.base64Screenshot(getPageScreenshot(context));
		return builder.build();
	}

	private String getSuccessMessage(String typedElementTagName) {
		return "Value was successfully typed into " + typedElementTagName + ".";
	}

}
