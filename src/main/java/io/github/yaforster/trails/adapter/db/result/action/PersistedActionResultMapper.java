package io.github.yaforster.trails.adapter.db.result.action;

import io.github.yaforster.trails.core.persisted.PersistedActionResult;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class PersistedActionResultMapper {

	private static final String UNKNOWN_TECHNICAL_FAILURE_MESSAGE = "The cause of the error could not be identified.";

	private final ActionResultTypeMapper actionResultTypeMapper;

	public PersistedActionResult fromEntity(ResultEntity entity) {
		return new PersistedActionResult(entity.getId(), entity.getTestPathResultId(), entity.getActionId(),
				entity.getLabel(), entity.getMessage(), actionResultTypeMapper.fromEntity(entity),
				mapExceptionMessage(entity));
	}

	private String mapExceptionMessage(ResultEntity entity) {
		return switch (entity) {
			case TechnicalFailureEntity technicalFailure -> getTechnicalFailureMessage(technicalFailure);
			default -> null;
		};
	}

	private String getTechnicalFailureMessage(TechnicalFailureEntity technicalFailure) {
		String exceptionMessage = technicalFailure.getExceptionMessageFromAction();
		return (exceptionMessage == null || exceptionMessage.isBlank()) ? UNKNOWN_TECHNICAL_FAILURE_MESSAGE
				: exceptionMessage;
	}

}
