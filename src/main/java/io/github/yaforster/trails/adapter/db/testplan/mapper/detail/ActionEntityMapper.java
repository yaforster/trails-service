package io.github.yaforster.trails.adapter.db.testplan.mapper.detail;

import io.github.yaforster.trails.adapter.db.testplan.entity.ActionDetailsEntity;
import io.github.yaforster.trails.adapter.db.testplan.entity.ActionEntity;
import io.github.yaforster.trails.adapter.db.testplan.mapper.PositionEntityMapper;
import io.github.yaforster.trails.adapter.db.testplan.mapper.detail.variant.ActionEntityVariantMapper;
import io.github.yaforster.trails.core.MappingException;
import io.github.yaforster.trails.core.test.Action;
import io.github.yaforster.trails.core.test.action.value.ValueComputationInstruction;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.function.UnaryOperator;

@Component
public class ActionEntityMapper {

	private final PositionEntityMapper positionEntityMapper;

	private final Map<Class<? extends Action>, ActionEntityVariantMapper> actionMappers;

	private final Map<Class<? extends ActionDetailsEntity>, ActionEntityVariantMapper> detailsMappers;

	private final Map<Class<?>, ActionEntityVariantMapper> resolvedActionMappers = new ConcurrentHashMap<>();

	private final Map<Class<?>, ActionEntityVariantMapper> resolvedDetailsMappers = new ConcurrentHashMap<>();

	public ActionEntityMapper(PositionEntityMapper positionEntityMapper,
			List<ActionEntityVariantMapper> variantMappers) {
		this.positionEntityMapper = positionEntityMapper;
		this.actionMappers = indexVariantMappers(variantMappers, ActionEntityVariantMapper::actionType, "action type");
		this.detailsMappers = indexVariantMappers(variantMappers, ActionEntityVariantMapper::detailsType,
				"details type");
	}

	public ActionEntity toEntity(Action action, Long applicationId, Long stageId) {
		ActionEntity entity = new ActionEntity();
		entity.setActionId(action.getActionID());
		entity.setNextActions(mapNextActions(action));
		entity.setLabel(action.getLabel());
		entity.setPosition(positionEntityMapper.toEntity(action.getPosition()));
		entity.setDetails(mapDetails(action, applicationId, stageId));
		return entity;
	}

	private List<Long> mapNextActions(Action action) {
		if (action.getNextActions() == null) {
			return new ArrayList<>();
		}
		return new ArrayList<>(action.getNextActions());
	}

	private ActionDetailsEntity mapDetails(Action action, Long applicationId, Long stageId) {
		return mapperFor(action).toEntity(action, applicationId, stageId);
	}

	public Action fromEntity(ActionEntity entity) {
		return fromEntity(entity, UnaryOperator.identity());
	}

	public Action fromEntity(ActionEntity entity, UnaryOperator<ValueComputationInstruction> instructionResolver) {
		ActionEntity detachedEntity = toDetachedEntity(entity);
		ActionDetailsEntity details = detachedEntity.getDetails();
		return mapperFor(details).fromEntity(detachedEntity, details, instructionResolver);
	}

	private ActionEntity toDetachedEntity(ActionEntity entity) {
		ActionEntity detachedEntity = new ActionEntity();
		detachedEntity.setId(entity.getId());
		detachedEntity.setActionId(entity.getActionId());
		detachedEntity.setTestPlanId(entity.getTestPlanId());
		detachedEntity.setLabel(entity.getLabel());
		detachedEntity.setDetails(entity.getDetails());
		detachedEntity.setPosition(entity.getPosition());
		detachedEntity
			.setNextActions(entity.getNextActions() == null ? List.of() : new ArrayList<>(entity.getNextActions()));
		return detachedEntity;
	}

	private ActionEntityVariantMapper mapperFor(Action action) {
		return resolvedActionMappers.computeIfAbsent(action.getClass(),
				actionType -> mapperFor(actionType, actionMappers, "Unhandled action type: " + actionType.getName()));
	}

	private ActionEntityVariantMapper mapperFor(ActionDetailsEntity details) {
		return resolvedDetailsMappers.computeIfAbsent(details.getClass(), detailsType -> mapperFor(detailsType,
				detailsMappers, "Unhandled ActionDetailsEntity type: " + detailsType.getName()));
	}

	private static <T> Map<Class<? extends T>, ActionEntityVariantMapper> indexVariantMappers(
			List<ActionEntityVariantMapper> variantMappers,
			Function<ActionEntityVariantMapper, Class<? extends T>> keyExtractor, String keyDescription) {
		Map<Class<? extends T>, ActionEntityVariantMapper> indexedMappers = new LinkedHashMap<>();
		for (ActionEntityVariantMapper mapper : variantMappers) {
			Class<? extends T> key = keyExtractor.apply(mapper);
			ActionEntityVariantMapper previous = indexedMappers.putIfAbsent(key, mapper);
			if (previous != null) {
				throw new IllegalStateException(
						"Multiple action entity mappers registered for " + keyDescription + " " + key.getName());
			}
		}
		return Map.copyOf(indexedMappers);
	}

	private static ActionEntityVariantMapper mapperFor(Class<?> type,
			Map<? extends Class<?>, ActionEntityVariantMapper> indexedMappers, String errorMessage) {
		ActionEntityVariantMapper exactMapper = indexedMappers.get(type);
		if (exactMapper != null) {
			return exactMapper;
		}
		return indexedMappers.entrySet()
			.stream()
			.filter(entry -> entry.getKey().isAssignableFrom(type))
			.map(Map.Entry::getValue)
			.findFirst()
			.orElseThrow(() -> new MappingException(errorMessage));
	}

}
