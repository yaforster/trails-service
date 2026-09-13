package io.github.yaforster.trails.core.test.action.browser.viewport;

import io.github.yaforster.trails.core.test.Action;
import io.github.yaforster.trails.core.test.Result;
import io.github.yaforster.trails.core.test.TestExecutionContext;
import io.github.yaforster.trails.core.test.result.success.Success;
import lombok.Getter;
import lombok.experimental.SuperBuilder;

@SuperBuilder
@Getter
public class MoveViewportAction extends Action {

	private final ViewportMove movement;

	private final ViewportMoveDirection direction;

	private final double amount;

	private final ViewportMoveUnit unit;

	private final long delayAfterMoveMillis;

	@Override
	public Result execute(TestExecutionContext context) {
		try {
			runAction(context);
			return Success.builder()
				.actionID(getActionID())
				.label(getLabel())
				.resultMessage(resultMessage())
				.base64Screenshot(getPageScreenshot(context))
				.build();
		}
		catch (Exception e) {
			return generateTechnicalFailureResult(e, "Could not move the viewport.", context);
		}
	}

	private void runAction(TestExecutionContext context) throws InterruptedException {
		context.browser().moveViewport(safeMovement(), safeDirection(), safeAmount(), safeUnit());
		waitAfterMove();
	}

	private void waitAfterMove() throws InterruptedException {
		long safeDelayMillis = Math.max(0L, delayAfterMoveMillis);
		if (safeDelayMillis > 0) {
			try {
				Thread.sleep(safeDelayMillis);
			}
			catch (InterruptedException e) {
				Thread.currentThread().interrupt();
				throw e;
			}
		}
	}

	private ViewportMove safeMovement() {
		return movement == null ? ViewportMove.SCROLL_BY : movement;
	}

	private ViewportMoveDirection safeDirection() {
		return direction == null ? ViewportMoveDirection.DOWN : direction;
	}

	private ViewportMoveUnit safeUnit() {
		return unit == null ? ViewportMoveUnit.VIEWPORTS : unit;
	}

	private double safeAmount() {
		return Math.max(0D, amount);
	}

	private String resultMessage() {
		return switch (safeMovement()) {
			case SCROLL_BY -> "Moved viewport " + safeDirection().name().toLowerCase() + " by " + safeAmount() + " "
					+ safeUnit().name().toLowerCase() + ".";
			case PAGE_FLIP ->
				"Sent " + safeDirectionPageKey() + " " + Math.max(0, (int) Math.round(safeAmount())) + " times.";
			case SCROLL_TO_TOP -> "Moved viewport to the top.";
			case SCROLL_TO_BOTTOM -> "Moved viewport to the bottom.";
		};
	}

	private String safeDirectionPageKey() {
		return safeDirection() == ViewportMoveDirection.UP ? "PAGE_UP" : "PAGE_DOWN";
	}

}
