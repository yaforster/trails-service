package io.github.yaforster.trails.core.test.action.browser;

import io.github.yaforster.trails.core.test.Action;
import io.github.yaforster.trails.core.test.Result;
import io.github.yaforster.trails.core.test.TestExecutionContext;
import io.github.yaforster.trails.core.test.result.success.Success;
import lombok.Getter;
import lombok.experimental.SuperBuilder;

@Getter
@SuperBuilder
public class ExplicitWaitAction extends Action {

	private final long delayMillis;

	@Override
	public Result execute(TestExecutionContext context) {
		try {
			return runAction(context);
		}
		catch (Exception e) {
			return generateTechnicalFailureResult(e, "Could not wait for the configured delay.", context);
		}
	}

	private Success runAction(TestExecutionContext context) throws InterruptedException {
		long safeDelayMillis = Math.max(0L, delayMillis);

		if (safeDelayMillis > 0) {
			try {
				Thread.sleep(safeDelayMillis);
			}
			catch (InterruptedException e) {
				Thread.currentThread().interrupt();
				throw e;
			}
		}

		return Success.builder()
			.actionID(getActionID())
			.label(getLabel())
			.resultMessage("Waited for " + safeDelayMillis + " milliseconds.")
			.base64Screenshot(getPageScreenshot(context))
			.build();
	}

}
