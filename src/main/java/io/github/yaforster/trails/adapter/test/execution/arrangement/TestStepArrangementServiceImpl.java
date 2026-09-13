package io.github.yaforster.trails.adapter.test.execution.arrangement;

import com.google.common.annotations.VisibleForTesting;
import io.github.yaforster.trails.app.services.TestStepArrangementService;
import io.github.yaforster.trails.core.TrailsException;
import io.github.yaforster.trails.core.test.Action;
import io.github.yaforster.trails.core.test.TestCase;
import io.github.yaforster.trails.core.test.TestExecutionData;
import io.github.yaforster.trails.core.test.TestSet;
import io.github.yaforster.trails.core.test.TestStep;
import io.github.yaforster.trails.core.test.WebDriverData;
import lombok.experimental.StandardException;
import org.springframework.stereotype.Service;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class TestStepArrangementServiceImpl implements TestStepArrangementService {

	@Override
	public List<TestSet> setUpTestsPerBrowser(TestExecutionData executionData) {
		try {
			TestStep startingStep = getTestStepChain(executionData.actions());
			return mapToTestSetsForEveryBrowser(startingStep, executionData);
		}
		catch (TestSetupException exception) {
			throw exception;
		}
		catch (RuntimeException exception) {
			throw new TestSetupException("Could not prepare the test execution.", exception);
		}
	}

	@VisibleForTesting
	protected TestStep getTestStepChain(List<Action> actions) {
		Map<Long, Action> actionsById = actions.stream()
			.collect(Collectors.toMap(Action::getActionID, action -> action, (left, _) -> left, LinkedHashMap::new));
		Map<Long, List<Long>> nextActionIdsById = nextActionIdsById(actionsById);
		Long startingActionId = findStartingActionId(actionsById, nextActionIdsById);
		return buildStepChain(startingActionId, actionsById, nextActionIdsById);
	}

	@VisibleForTesting
	protected List<TestSet> mapToTestSetsForEveryBrowser(TestStep startingStep, TestExecutionData executionData) {
		return executionData.webDrivers()
			.stream()
			.map(webDriverConfig -> createTestSet(startingStep, executionData.testPlanLabel(), webDriverConfig))
			.toList();
	}

	@VisibleForTesting
	protected TestSet createTestSet(TestStep startingStep, String testPlanLabel, WebDriverData webDriverConfig) {
		return new TestSet(new TestCase(webDriverConfig.webAppURL(), startingStep), webDriverConfig.webdriverProvider(),
				webDriverConfig.browserName(), testPlanLabel);
	}

	private Map<Long, List<Long>> nextActionIdsById(Map<Long, Action> actionsById) {
		return actionsById.entrySet()
			.stream()
			.collect(Collectors.toMap(Map.Entry::getKey,
					entry -> List.copyOf(Objects.requireNonNullElse(entry.getValue().getNextActions(), List.of())),
					(left, _) -> left, LinkedHashMap::new));
	}

	private Long findStartingActionId(Map<Long, Action> actionsById, Map<Long, List<Long>> nextActionIdsById) {
		Set<Long> referencedActionIds = nextActionIdsById.values()
			.stream()
			.flatMap(List::stream)
			.collect(Collectors.toSet());
		return actionsById.keySet()
			.stream()
			.filter(actionId -> !referencedActionIds.contains(actionId))
			.findFirst()
			.orElseThrow();
	}

	private TestStep buildStepChain(Long actionId, Map<Long, Action> actionsById,
			Map<Long, List<Long>> nextActionIdsById) {
		List<TestStep> nextSteps = nextActionIdsById.get(actionId)
			.stream()
			.map(nextActionId -> buildStepChain(nextActionId, actionsById, nextActionIdsById))
			.toList();
		return new TestStep(actionsById.get(actionId), nextSteps);
	}

	@StandardException
	static class TestSetupException extends TrailsException {

	}

}
