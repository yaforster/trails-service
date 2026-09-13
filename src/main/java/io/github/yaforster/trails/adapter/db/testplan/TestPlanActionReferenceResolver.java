package io.github.yaforster.trails.adapter.db.testplan;

import io.github.yaforster.trails.adapter.db.testplan.entity.ActionEntity;
import io.github.yaforster.trails.adapter.db.testplan.entity.TestPlanEntity;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

@Component
public class TestPlanActionReferenceResolver {

	public void resolveReferences(TestPlanEntity storedEntity) {
		Map<Long, Long> referenceIdToPersistedId = referenceIdToPersistedId(storedEntity);
		storedEntity.getTestSteps()
			.forEach(action -> resolveActionReferences(storedEntity, action, referenceIdToPersistedId));
	}

	private Map<Long, Long> referenceIdToPersistedId(TestPlanEntity storedEntity) {
		Map<Long, Long> referenceIdToPersistedId = new HashMap<>();
		storedEntity.getTestSteps().forEach(action -> {
			if (action.getActionId() == null) {
				throw new IllegalStateException("Action referenceID must be set when storing a test plan");
			}
			referenceIdToPersistedId.put(action.getActionId(), action.getId());
		});
		return referenceIdToPersistedId;
	}

	private void resolveActionReferences(TestPlanEntity storedEntity, ActionEntity action,
			Map<Long, Long> referenceIdToPersistedId) {
		action.setTestPlanId(storedEntity.getId());
		if (action.getNextActions() == null) {
			action.setNextActions(new ArrayList<>());
			return;
		}
		action.setNextActions(action.getNextActions()
			.stream()
			.map(nextReferenceId -> resolveReference(referenceIdToPersistedId, nextReferenceId))
			.collect(java.util.stream.Collectors.toCollection(ArrayList::new)));
	}

	private Long resolveReference(Map<Long, Long> referenceIdToPersistedId, Long nextReferenceId) {
		Long resolved = referenceIdToPersistedId.get(nextReferenceId);
		if (resolved == null) {
			throw new IllegalStateException("Unknown next action referenceID: " + nextReferenceId);
		}
		return resolved;
	}

}
