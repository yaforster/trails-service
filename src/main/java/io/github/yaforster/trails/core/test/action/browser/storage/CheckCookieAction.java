package io.github.yaforster.trails.core.test.action.browser.storage;

import io.github.yaforster.trails.core.test.action.value.ValueComputationInstruction;

import io.github.yaforster.trails.core.test.Action;
import io.github.yaforster.trails.core.test.Result;
import io.github.yaforster.trails.core.test.TestExecutionContext;
import io.github.yaforster.trails.core.test.result.failure.ValidationFailure;
import io.github.yaforster.trails.core.test.result.success.Success;
import lombok.Getter;
import lombok.experimental.SuperBuilder;
import org.apache.commons.lang3.Strings;
import java.util.Optional;

@SuperBuilder
@Getter
public class CheckCookieAction extends Action {

	private final String cookieName;

	private final ValueComputationInstruction computationInstruction;

	@Override
	public Result execute(TestExecutionContext context) {
		Optional<String> cookie = context.browser().cookieValue(cookieName);
		if (cookie.isPresent()) {
			return searchForTextInCookie(cookie.get(), context);
		}
		else {
			return buildCookieDoesNotExistFailure(context);
		}
	}

	private Result searchForTextInCookie(String cookieValue, TestExecutionContext context) {
		String contentToSearchFor = computationInstruction.computeValue();
		if (Strings.CI.contains(cookieValue, contentToSearchFor)) {
			return buildSuccess(cookieValue, context);
		}
		else {
			return buildCookieDoesNotContainStringFailure(context);
		}
	}

	private ValidationFailure buildCookieDoesNotExistFailure(TestExecutionContext context) {
		ValidationFailure.ValidationFailureBuilder<?, ?> builder = ValidationFailure.builder()
			.actionID(getActionID())
			.resultMessage(getCookieDoesNotExistMessage())
			.base64Screenshot(getPageScreenshot(context));
		return builder.build();
	}

	private Success buildSuccess(String cookieValue, TestExecutionContext context) {
		Success.SuccessBuilder<?, ?> builder = Success.builder()
			.actionID(getActionID())
			.label(getLabel())
			.resultMessage(getSuccessMessage(cookieValue))
			.base64Screenshot(getPageScreenshot(context));
		return builder.build();
	}

	private ValidationFailure buildCookieDoesNotContainStringFailure(TestExecutionContext context) {
		ValidationFailure.ValidationFailureBuilder<?, ?> builder = ValidationFailure.builder()
			.actionID(getActionID())
			.resultMessage(getContentDoesNotExistInCookieValueMessage())
			.label(getLabel())
			.base64Screenshot(getPageScreenshot(context));
		return builder.build();
	}

	private String getCookieDoesNotExistMessage() {
		return getCookieName() + " does not exist.";
	}

	private String getSuccessMessage(String cookieValue) {
		return getCookieName() + " does contain the target value. Complete content: " + cookieValue;
	}

	private String getContentDoesNotExistInCookieValueMessage() {
		return getCookieName() + " exists but does not contain the target value.";
	}

}
