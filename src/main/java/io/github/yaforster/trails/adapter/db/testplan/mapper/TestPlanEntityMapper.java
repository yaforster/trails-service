package io.github.yaforster.trails.adapter.db.testplan.mapper;

import io.github.yaforster.trails.adapter.db.testplan.entity.ActionEntity;
import io.github.yaforster.trails.adapter.db.testplan.entity.TestPlanEntity;
import io.github.yaforster.trails.adapter.db.testplan.entity.TestPlanGroupEntity;
import io.github.yaforster.trails.adapter.db.testplan.mapper.detail.ActionEntityMapper;
import io.github.yaforster.trails.core.data.TestPlan;
import io.github.yaforster.trails.core.definition.TestPlanDefinition;
import io.github.yaforster.trails.core.definition.TestPlanGroup;
import io.github.yaforster.trails.core.persisted.PersistedTestPlan;
import io.github.yaforster.trails.core.test.action.value.ValueComputationInstruction;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.function.UnaryOperator;
import java.util.stream.Collectors;

@AllArgsConstructor
@Component
public class TestPlanEntityMapper {

	private final ActionEntityMapper actionMapper;

	public TestPlan fromEntity(TestPlanEntity entity) {
		return new TestPlan(entity.getId(), entity.getApplicationId(), entity.getStageId(), entity.getLabel(),
				entity.getTestSteps()
					.stream()
					.map(actionMapper::fromEntity)
					.collect(Collectors.toCollection(LinkedList::new)),
				mapGroups(entity.getGroups()));
	}

	public TestPlan fromEntity(TestPlanEntity entity, UnaryOperator<ValueComputationInstruction> instructionResolver) {
		return new TestPlan(entity.getId(), entity.getApplicationId(), entity.getStageId(), entity.getLabel(),
				entity.getTestSteps()
					.stream()
					.map(action -> actionMapper.fromEntity(action, instructionResolver))
					.collect(Collectors.toCollection(LinkedList::new)),
				mapGroups(entity.getGroups()));
	}

	public TestPlanEntity toEntity(TestPlan testPlan) {
		TestPlanEntity entity = new TestPlanEntity();
		entity.setId(null);
		entity.setApplicationId(testPlan.getApplicationID());
		entity.setStageId(testPlan.getStageID());
		entity.setLabel(testPlan.getLabel());
		List<ActionEntity> actionEntities = testPlan.getActions()
			.stream()
			.map(action -> actionMapper.toEntity(action, testPlan.getApplicationID(), testPlan.getStageID()))
			.collect(Collectors.toCollection(LinkedList::new));
		entity.setTestSteps(actionEntities);
		entity.setGroups(mapGroupsToEntities(testPlan.getGroups()));
		return entity;
	}

	public TestPlanEntity toEntity(TestPlanDefinition definition) {
		TestPlanEntity entity = new TestPlanEntity();
		entity.setId(null);
		entity.setApplicationId(definition.applicationID());
		entity.setStageId(definition.stageID());
		entity.setLabel(definition.label());
		List<ActionEntity> actionEntities = definition.actions()
			.stream()
			.map(action -> actionMapper.toEntity(action, definition.applicationID(), definition.stageID()))
			.collect(Collectors.toCollection(ArrayList::new));
		entity.setTestSteps(actionEntities);
		entity.setGroups(mapGroupsToEntities(definition.groups()));
		return entity;
	}

	private List<TestPlanGroup> mapGroups(List<TestPlanGroupEntity> groups) {
		if (groups == null) {
			return List.of();
		}
		return groups.stream()
			.map(group -> new TestPlanGroup(group.getLabel(), group.getXCoordinatePixels(),
					group.getYCoordinatePixels(), group.getWidthPixels(), group.getHeightPixels(),
					group.getActionReferenceIds() == null ? List.of() : group.getActionReferenceIds()))
			.toList();
	}

	private List<TestPlanGroupEntity> mapGroupsToEntities(List<TestPlanGroup> groups) {
		if (groups == null) {
			return new ArrayList<>();
		}
		return groups.stream()
			.map(group -> new TestPlanGroupEntity().setLabel(group.label())
				.setXCoordinatePixels(group.xCoordinate())
				.setYCoordinatePixels(group.yCoordinate())
				.setWidthPixels(group.widthPixels())
				.setHeightPixels(group.heightPixels())
				.setActionReferenceIds(group.actionReferenceIds() == null ? new ArrayList<>()
						: new ArrayList<>(group.actionReferenceIds())))
			.collect(Collectors.toCollection(ArrayList::new));
	}

	public PersistedTestPlan mapEntityToPersistedTestPlan(TestPlanEntity entity) {
		return new PersistedTestPlan(entity.getId(), entity.getApplicationId(), entity.getStageId(), entity.getLabel(),
				entity.isRetired(), mapGroups(entity.getGroups()));
	}

}
