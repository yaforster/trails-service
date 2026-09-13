package io.github.yaforster.trails.core.test.action.browser;

import io.github.yaforster.trails.core.test.action.value.ValueComputationInstruction;

import io.github.yaforster.trails.core.test.Action;
import io.github.yaforster.trails.core.test.Result;
import io.github.yaforster.trails.core.test.TestExecutionContext;
import io.github.yaforster.trails.core.test.result.success.Success;
import lombok.Getter;
import lombok.experimental.SuperBuilder;

@Getter
@SuperBuilder
public class SwitchWebsiteAction extends Action {

	private final ValueComputationInstruction computationInstruction;

	@Override
	public Result execute(TestExecutionContext context) {
		try {
			return runAction(context);
		}
		catch (Exception e) {
			return generateTechnicalFailureResult(e, "Could not navigate to the target URL", context);
		}
	}

	private Success runAction(TestExecutionContext context) {
		String urlToNavigateTo = computationInstruction.computeValue();
		context.browser().open(urlToNavigateTo);
		return buildSuccess(context);
	}

	private Success buildSuccess(TestExecutionContext context) {
		Success.SuccessBuilder<?, ?> builder = Success.builder()
			.actionID(getActionID())
			.label(getLabel())
			.resultMessage("Successfully Navigated to the URL.")
			.base64Screenshot(getPageScreenshot(context));
		return builder.build();
	}

}
