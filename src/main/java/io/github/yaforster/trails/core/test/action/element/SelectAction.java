package io.github.yaforster.trails.core.test.action.element;

import io.github.yaforster.trails.core.test.action.value.ValueComputationInstruction;

import io.github.yaforster.trails.core.test.action.browser.WebpageAction;
import io.github.yaforster.trails.core.test.Result;
import io.github.yaforster.trails.core.test.TestExecutionContext;
import io.github.yaforster.trails.core.test.result.success.Success;
import lombok.Getter;
import lombok.experimental.SuperBuilder;

@SuperBuilder
@Getter
public class SelectAction extends WebpageAction {

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

	private Success runAction(TestExecutionContext context) {
		String labelToSelect = computationInstruction.computeValue();
		String selectElementTagName = context.browser().select(getLocatorForElementToActOn(), labelToSelect);
		return buildSuccess(context, selectElementTagName);
	}

	private Success buildSuccess(TestExecutionContext context, String selectElementTagName) {
		Success.SuccessBuilder<?, ?> builder = Success.builder()
			.actionID(getActionID())
			.label(getLabel())
			.resultMessage(getSuccessMessage(selectElementTagName))
			.base64Screenshot(getPageScreenshot(context));
		return builder.build();
	}

	private String getSuccessMessage(String selectElementTagName) {
		return "The target value was successfully selected from " + selectElementTagName + ".";
	}

}
