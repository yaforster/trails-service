package io.github.yaforster.trails.core.test.action.browser.storage;

import io.github.yaforster.trails.core.test.action.value.ValueComputationInstruction;

import io.github.yaforster.trails.core.test.Action;
import io.github.yaforster.trails.core.test.Result;
import io.github.yaforster.trails.core.test.TestExecutionContext;
import io.github.yaforster.trails.core.test.result.failure.ValidationFailure;
import io.github.yaforster.trails.core.test.result.success.Success;
import lombok.Getter;
import lombok.experimental.SuperBuilder;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Strings;

@Getter
@SuperBuilder

public class CheckLocalStorageAction extends Action {

	private final String localStorageItemKey;

	private final ValueComputationInstruction computationInstruction;

	@Override
	public Result execute(TestExecutionContext context) {
		String webStorageItem = context.browser().localStorageValue(getLocalStorageItemKey());
		if (StringUtils.isNotBlank(webStorageItem)) {
			return searchForTextInKey(webStorageItem, context);
		}
		else {
			return buildKeyDoesNotExistFailure();
		}
	}

	private Result searchForTextInKey(String webStorageItem, TestExecutionContext context) {
		String contentToSearchFor = computationInstruction.computeValue();
		if (Strings.CI.contains(webStorageItem, contentToSearchFor)) {
			return buildSuccess(webStorageItem, context);
		}
		else {
			return buildKeyDoesNotContainStringFailure(context);
		}
	}

	private ValidationFailure buildKeyDoesNotExistFailure() {
		return ValidationFailure.builder().actionID(getActionID()).resultMessage(getKeyDoesNotExistMessage()).build();
	}

	private Success buildSuccess(String webStorageItem, TestExecutionContext context) {
		Success.SuccessBuilder<?, ?> builder = Success.builder()
			.actionID(getActionID())
			.label(getLabel())
			.resultMessage(getSuccessMessage(webStorageItem))
			.base64Screenshot(getPageScreenshot(context));
		return builder.build();
	}

	private ValidationFailure buildKeyDoesNotContainStringFailure(TestExecutionContext context) {
		ValidationFailure.ValidationFailureBuilder<?, ?> builder = ValidationFailure.builder()
			.actionID(getActionID())
			.resultMessage(getContentDoesNotExistInKeyMessage())
			.label(getLabel())
			.base64Screenshot(getPageScreenshot(context));

		return builder.build();
	}

	private String getKeyDoesNotExistMessage() {
		return getLocalStorageItemKey() + " does not exist.";
	}

	private String getSuccessMessage(String webStorageItem) {
		return getLocalStorageItemKey() + " does contain the target value. Complete content: " + webStorageItem;
	}

	private String getContentDoesNotExistInKeyMessage() {
		return getLocalStorageItemKey() + " exists but does not contain the target value.";
	}

}
