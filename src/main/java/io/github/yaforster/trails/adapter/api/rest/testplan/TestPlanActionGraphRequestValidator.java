package io.github.yaforster.trails.adapter.api.rest.testplan;

import io.github.yaforster.trails.adapter.api.rest.model.ActionDefinitionDTO;
import io.github.yaforster.trails.adapter.api.rest.model.TestPlanDefinitionDTO;
import io.github.yaforster.trails.adapter.api.rest.validation.RequestValidator;
import io.github.yaforster.trails.adapter.api.rest.validation.ValidationContext;
import io.github.yaforster.trails.core.ValidationViolation;
import org.springframework.stereotype.Component;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Component
public class TestPlanActionGraphRequestValidator implements RequestValidator<TestPlanDefinitionDTO> {

	@Override
	public void validate(TestPlanDefinitionDTO definition) {
		ValidationContext validation = ValidationContext.begin();
		validate(definition, validation);
		validation.rejectIfErrors();
	}

	void validate(TestPlanDefinitionDTO definition, ValidationContext validation) {
		if (definition == null || definition.getTestSteps() == null) {
			validation.addViolation(new ValidationViolation("TEST_PLAN_TEST_STEPS_NULL",
					"Test plan test steps must not be null.", "/testSteps"));
			return;
		}
		if (definition.getTestSteps().isEmpty()) {
			validation.addViolation(new ValidationViolation("TEST_PLAN_TEST_STEPS_EMPTY",
					"Test plan must define at least one test step.", "/testSteps"));
			return;
		}

		Map<Long, IndexedAction> actionsByReferenceId = indexActions(definition.getTestSteps(), validation);
		if (actionsByReferenceId.size() != definition.getTestSteps().size()) {
			return;
		}
		Map<Long, List<Long>> nextReferenceIdsById = nextReferenceIdsById(actionsByReferenceId, validation);
		if (hasValidationErrorsForNextActions(actionsByReferenceId, nextReferenceIdsById)) {
			return;
		}
		if (validateAcyclicGraph(actionsByReferenceId, nextReferenceIdsById, validation)) {
			validateSingleStartingAction(actionsByReferenceId, nextReferenceIdsById, validation);
		}
	}

	private Map<Long, IndexedAction> indexActions(List<ActionDefinitionDTO> actions, ValidationContext validation) {
		Map<Long, IndexedAction> actionsByReferenceId = new LinkedHashMap<>();
		for (int index = 0; index < actions.size(); index++) {
			ActionDefinitionDTO action = actions.get(index);
			String path = "/testSteps/" + index;
			if (action == null) {
				validation.addViolation(
						new ValidationViolation("TEST_PLAN_ACTION_NULL", "Test plan action must not be null.", path));
				continue;
			}
			if (action.getReferenceID() == null) {
				validation.addViolation(new ValidationViolation("TEST_PLAN_ACTION_REFERENCE_ID_NULL",
						"Test plan action reference ID must not be null.", path + "/referenceID"));
				continue;
			}
			if (actionsByReferenceId.putIfAbsent(action.getReferenceID(), new IndexedAction(action, index)) != null) {
				validation.addViolation(new ValidationViolation("TEST_PLAN_ACTION_REFERENCE_ID_DUPLICATE",
						"Test plan action reference IDs must be unique.", path + "/referenceID"));
			}
		}
		return actionsByReferenceId;
	}

	private Map<Long, List<Long>> nextReferenceIdsById(Map<Long, IndexedAction> actionsByReferenceId,
			ValidationContext validation) {
		Map<Long, List<Long>> nextReferenceIdsById = new LinkedHashMap<>();
		for (Map.Entry<Long, IndexedAction> entry : actionsByReferenceId.entrySet()) {
			List<Long> nextReferenceIds = Objects.requireNonNullElse(entry.getValue().action().getNextActions(),
					List.of());
			nextReferenceIdsById.put(entry.getKey(), new ArrayList<>(nextReferenceIds));
			Set<Long> seenNextReferenceIds = new HashSet<>();
			for (int index = 0; index < nextReferenceIds.size(); index++) {
				Long nextReferenceId = nextReferenceIds.get(index);
				String path = "/testSteps/" + entry.getValue().index() + "/nextActions/" + index;
				if (nextReferenceId == null) {
					validation.addViolation(new ValidationViolation("TEST_PLAN_NEXT_ACTION_REFERENCE_ID_NULL",
							"Next action reference ID must not be null.", path));
				}
				else if (!seenNextReferenceIds.add(nextReferenceId)) {
					validation.addViolation(new ValidationViolation("TEST_PLAN_NEXT_ACTION_REFERENCE_ID_DUPLICATE",
							"Next action reference IDs must be unique per action.", path));
				}
				else if (!actionsByReferenceId.containsKey(nextReferenceId)) {
					validation.addViolation(new ValidationViolation("TEST_PLAN_NEXT_ACTION_REFERENCE_ID_UNKNOWN",
							"Next action reference ID must identify an action in the test plan.", path));
				}
			}
		}
		return nextReferenceIdsById;
	}

	private boolean hasValidationErrorsForNextActions(Map<Long, IndexedAction> actionsByReferenceId,
			Map<Long, List<Long>> nextReferenceIdsById) {
		return nextReferenceIdsById.values()
			.stream()
			.anyMatch(nextReferenceIds -> hasValidationErrorsForNextActions(actionsByReferenceId, nextReferenceIds));
	}

	private boolean hasValidationErrorsForNextActions(Map<Long, IndexedAction> actionsByReferenceId,
			List<Long> nextReferenceIds) {
		return nextReferenceIds.stream()
			.anyMatch(nextReferenceId -> nextReferenceId == null || !actionsByReferenceId.containsKey(nextReferenceId))
				|| new HashSet<>(nextReferenceIds).size() != nextReferenceIds.size();
	}

	private boolean validateAcyclicGraph(Map<Long, IndexedAction> actionsByReferenceId,
			Map<Long, List<Long>> nextReferenceIdsById, ValidationContext validation) {
		Map<Long, Integer> incomingEdgeCounts = actionsByReferenceId.keySet()
			.stream()
			.collect(Collectors.toMap(referenceId -> referenceId, _ -> 0, (left, _) -> left, LinkedHashMap::new));
		nextReferenceIdsById.values()
			.forEach(nextReferenceIds -> nextReferenceIds
				.forEach(nextReferenceId -> incomingEdgeCounts.compute(nextReferenceId, (_, count) -> count + 1)));

		Deque<Long> actionIdsWithoutIncomingEdges = new ArrayDeque<>(incomingReferenceIds(incomingEdgeCounts));
		int processedActionCount = 0;
		while (!actionIdsWithoutIncomingEdges.isEmpty()) {
			Long actionId = actionIdsWithoutIncomingEdges.removeFirst();
			processedActionCount++;
			for (Long nextActionId : nextReferenceIdsById.get(actionId)) {
				int remainingIncomingEdgeCount = incomingEdgeCounts.compute(nextActionId, (_, count) -> count - 1);
				if (remainingIncomingEdgeCount == 0) {
					actionIdsWithoutIncomingEdges.addLast(nextActionId);
				}
			}
		}

		if (processedActionCount != actionsByReferenceId.size()) {
			validation.addViolation(new ValidationViolation("TEST_PLAN_ACTION_GRAPH_CYCLIC",
					"Test plan action graph must not contain cycles.", "/testSteps"));
			return false;
		}
		return true;
	}

	private void validateSingleStartingAction(Map<Long, IndexedAction> actionsByReferenceId,
			Map<Long, List<Long>> nextReferenceIdsById, ValidationContext validation) {
		Set<Long> referencedActionIds = nextReferenceIdsById.values()
			.stream()
			.flatMap(List::stream)
			.collect(Collectors.toSet());
		long startingActionCount = actionsByReferenceId.keySet()
			.stream()
			.filter(actionId -> !referencedActionIds.contains(actionId))
			.count();
		if (startingActionCount != 1) {
			validation.addViolation(new ValidationViolation("TEST_PLAN_ACTION_GRAPH_STARTING_ACTION_COUNT_INVALID",
					"Test plan action graph must have exactly one starting action.", "/testSteps"));
		}
	}

	private List<Long> incomingReferenceIds(Map<Long, Integer> incomingEdgeCounts) {
		return incomingEdgeCounts.entrySet()
			.stream()
			.filter(entry -> entry.getValue() == 0)
			.map(Map.Entry::getKey)
			.collect(Collectors.toCollection(ArrayList::new));
	}

	private record IndexedAction(ActionDefinitionDTO action, int index) {
	}

}
