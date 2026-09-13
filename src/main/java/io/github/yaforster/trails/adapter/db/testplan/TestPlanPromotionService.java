package io.github.yaforster.trails.adapter.db.testplan;

import io.github.yaforster.trails.adapter.db.testplan.entity.ActionDetailsEntity;
import io.github.yaforster.trails.adapter.db.testplan.entity.ActionEntity;
import io.github.yaforster.trails.adapter.db.testplan.entity.PositionEntity;
import io.github.yaforster.trails.adapter.db.testplan.entity.TestPlanEntity;
import io.github.yaforster.trails.adapter.db.testplan.entity.TestPlanGroupEntity;
import io.github.yaforster.trails.adapter.db.testplan.promotion.ActionDetailsCopyService;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@AllArgsConstructor
@Component
public class TestPlanPromotionService {

	private final ActionDetailsCopyService actionDetailsCopyService;

	public Optional<TestPlanEntity> copyToStage(TestPlanEntity source, Long targetStageId) {
		if (!hasOnlySourceActionReferences(source)) {
			return Optional.empty();
		}
		TestPlanEntity copy = new TestPlanEntity().setApplicationId(source.getApplicationId())
			.setStageId(targetStageId)
			.setLabel(source.getLabel())
			.setRetired(false);
		ArrayList<ActionEntity> copiedActions = new ArrayList<>();
		for (ActionEntity action : source.getTestSteps()) {
			Optional<ActionEntity> copiedAction = copyAction(action, source.getApplicationId(), targetStageId);
			if (copiedAction.isEmpty()) {
				return Optional.empty();
			}
			copiedActions.add(copiedAction.get());
		}
		copy.setTestSteps(copiedActions);
		copy.setGroups(source.getGroups() == null ? new ArrayList<>()
				: source.getGroups()
					.stream()
					.map(this::copyGroup)
					.collect(java.util.stream.Collectors.toCollection(ArrayList::new)));
		return Optional.of(copy);
	}

	private boolean hasOnlySourceActionReferences(TestPlanEntity source) {
		Set<Long> sourceActionIds = source.getTestSteps().stream().map(ActionEntity::getId).collect(Collectors.toSet());
		return source.getTestSteps()
			.stream()
			.flatMap(action -> (action.getNextActions() == null ? java.util.stream.Stream.<Long>empty()
					: action.getNextActions().stream()))
			.allMatch(sourceActionIds::contains);
	}

	public void resolveCopiedNextActions(TestPlanEntity source, TestPlanEntity copy) {
		Map<Long, Long> copiedActionIdBySourceId = new HashMap<>();
		for (int i = 0; i < source.getTestSteps().size(); i++) {
			copiedActionIdBySourceId.put(source.getTestSteps().get(i).getId(), copy.getTestSteps().get(i).getId());
		}
		copy.getTestSteps().forEach(action -> {
			action.setTestPlanId(copy.getId());
			action.setNextActions(action.getNextActions()
				.stream()
				.map(copiedActionIdBySourceId::get)
				.collect(java.util.stream.Collectors.toCollection(ArrayList::new)));
		});
	}

	private Optional<ActionEntity> copyAction(ActionEntity action, Long applicationId, Long targetStageId) {
		Optional<ActionDetailsEntity> copiedDetails = copyDetails(action.getDetails(), applicationId, targetStageId);
		if (copiedDetails.isEmpty()) {
			return Optional.empty();
		}
		return Optional.of(new ActionEntity().setActionId(action.getActionId())
			.setLabel(action.getLabel())
			.setDetails(copiedDetails.get())
			.setPosition(copyPosition(action.getPosition()))
			.setNextActions(
					action.getNextActions() == null ? new ArrayList<>() : new ArrayList<>(action.getNextActions())));
	}

	private Optional<ActionDetailsEntity> copyDetails(ActionDetailsEntity details, Long applicationId,
			Long targetStageId) {
		return actionDetailsCopyService.copy(details, applicationId, targetStageId);
	}

	private PositionEntity copyPosition(PositionEntity position) {
		return new PositionEntity().setXCoordinatePixels(position.getXCoordinatePixels())
			.setYCoordinatePixels(position.getYCoordinatePixels());
	}

	private TestPlanGroupEntity copyGroup(TestPlanGroupEntity group) {
		return new TestPlanGroupEntity().setLabel(group.getLabel())
			.setXCoordinatePixels(group.getXCoordinatePixels())
			.setYCoordinatePixels(group.getYCoordinatePixels())
			.setWidthPixels(group.getWidthPixels())
			.setHeightPixels(group.getHeightPixels())
			.setActionReferenceIds(group.getActionReferenceIds() == null ? new ArrayList<>()
					: new ArrayList<>(group.getActionReferenceIds()));
	}

}
