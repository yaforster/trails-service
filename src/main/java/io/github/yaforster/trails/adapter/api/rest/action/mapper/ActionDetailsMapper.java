package io.github.yaforster.trails.adapter.api.rest.action.mapper;

import io.github.yaforster.trails.adapter.api.rest.common.mapper.PositionMapper;
import io.github.yaforster.trails.adapter.api.rest.common.mapper.ValueComputationInstructionMapper;
import io.github.yaforster.trails.adapter.api.rest.model.ActionDefinitionDTO;
import io.github.yaforster.trails.adapter.api.rest.model.ActionDetailsDTO;
import io.github.yaforster.trails.app.services.ElementDatabaseService;
import io.github.yaforster.trails.core.MappingException;
import io.github.yaforster.trails.core.test.Action;
import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

@Component
public class ActionDetailsMapper {

	private final ActionDetailsMappingContext context;

	private final Map<Class<? extends Action>, ActionDetailsVariantMapper> actionMappers;

	private final Map<Class<? extends ActionDetailsDTO>, ActionDetailsVariantMapper> detailsMappers;

	private final Map<Class<?>, ActionDetailsVariantMapper> resolvedActionMappers = new ConcurrentHashMap<>();

	private final Map<Class<?>, ActionDetailsVariantMapper> resolvedDetailsMappers = new ConcurrentHashMap<>();

	public ActionDetailsMapper(ElementDatabaseService elementDatabaseService, PositionMapper positionMapper,
			ValueComputationInstructionMapper instructionMapper, List<ActionDetailsVariantMapper> variantMappers) {
		this.context = new ActionDetailsMappingContext(elementDatabaseService, positionMapper, instructionMapper, null,
				null);
		this.actionMappers = indexVariantMappers(variantMappers, ActionDetailsVariantMapper::actionType, "action type");
		this.detailsMappers = indexVariantMappers(variantMappers, ActionDetailsVariantMapper::detailsType,
				"details type");
	}

	public ActionDetailsDTO toDTO(Action action, Long applicationId, Long stageId) {
		return mapperFor(action).toDTO(action, context.scoped(applicationId, stageId));
	}

	public Action fromDTO(ActionDefinitionDTO dto) {
		ActionDetailsDTO details = dto.getDetails();
		return mapperFor(details).fromDTO(dto, details, context);
	}

	private ActionDetailsVariantMapper mapperFor(Action action) {
		return resolvedActionMappers.computeIfAbsent(action.getClass(),
				actionType -> mapperFor(actionType, actionMappers,
						"Error when mapping an unexpected action type " + actionType.getName() + " to REST DTO"));
	}

	private ActionDetailsVariantMapper mapperFor(ActionDetailsDTO details) {
		return resolvedDetailsMappers.computeIfAbsent(details.getClass(),
				detailsType -> mapperFor(detailsType, detailsMappers,
						"Error when mapping an unexpected action type " + detailsType.getName() + " to domain action"));
	}

	private static <T> Map<Class<? extends T>, ActionDetailsVariantMapper> indexVariantMappers(
			List<ActionDetailsVariantMapper> variantMappers,
			Function<ActionDetailsVariantMapper, Class<? extends T>> keyExtractor, String keyDescription) {
		Map<Class<? extends T>, ActionDetailsVariantMapper> indexedMappers = new LinkedHashMap<>();
		for (ActionDetailsVariantMapper mapper : variantMappers) {
			Class<? extends T> key = keyExtractor.apply(mapper);
			ActionDetailsVariantMapper previous = indexedMappers.putIfAbsent(key, mapper);
			if (previous != null) {
				throw new IllegalStateException(
						"Multiple action details mappers registered for " + keyDescription + " " + key.getName());
			}
		}
		return Map.copyOf(indexedMappers);
	}

	private static ActionDetailsVariantMapper mapperFor(Class<?> type,
			Map<? extends Class<?>, ActionDetailsVariantMapper> indexedMappers, String errorMessage) {
		ActionDetailsVariantMapper exactMapper = indexedMappers.get(type);
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
