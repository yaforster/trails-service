package io.github.yaforster.trails.adapter.api.asyncapi.run;

import io.github.yaforster.trails.adapter.api.TestExecutionEventHub;
import io.github.yaforster.trails.adapter.api.asyncapi.model.TestExecutionFailedEventAsyncDTO;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

import java.lang.reflect.InvocationTargetException;
import java.util.Optional;
import java.util.UUID;

@Component
@AllArgsConstructor
public class AsyncAPIExceptionMapper {

	private final TestExecutionEventFacade eventFacade;

	private final TestExecutionEventHub eventHub;

	public boolean toFailedEvent(Object[] arguments, Exception exception) {
		Optional<UUID> executionId = findExecutionID(arguments);
		if (executionId.isEmpty()) {
			return false;
		}

		TestExecutionFailedEventAsyncDTO failedEvent = eventFacade.toFailedEventDTO(executionId.get(), exception);
		eventHub.publishFailed(executionId.get(), failedEvent);
		return true;
	}

	private Optional<UUID> findExecutionID(Object[] arguments) {
		if (arguments == null) {
			return Optional.empty();
		}
		for (Object argument : arguments) {
			Optional<UUID> executionId = findExecutionID(argument);
			if (executionId.isPresent()) {
				return executionId;
			}
		}
		return Optional.empty();
	}

	private Optional<UUID> findExecutionID(Object argument) {
		return switch (argument) {
			case null -> Optional.empty();
			case UUID executionId -> Optional.of(executionId);
			default -> executionIdFromGetter(argument);
		};
	}

	private Optional<UUID> executionIdFromGetter(Object argument) {
		try {
			Object executionId = argument.getClass().getMethod("getExecutionId").invoke(argument);
			if (!(executionId instanceof String stringExecutionId) || stringExecutionId.isBlank()) {
				return Optional.empty();
			}
			return Optional.of(UUID.fromString(stringExecutionId));
		}
		catch (IllegalAccessException | NoSuchMethodException | InvocationTargetException
				| IllegalArgumentException exception) {
			return Optional.empty();
		}
	}

}
