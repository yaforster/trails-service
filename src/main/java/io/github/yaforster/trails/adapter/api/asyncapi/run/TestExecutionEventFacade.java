package io.github.yaforster.trails.adapter.api.asyncapi.run;

import io.github.yaforster.trails.adapter.api.ApiErrorHandlingProperties;
import io.github.yaforster.trails.adapter.api.asyncapi.model.ErrorAsyncDTO;
import io.github.yaforster.trails.adapter.api.asyncapi.model.TestExecutionCompletedEventAsyncDTO;
import io.github.yaforster.trails.adapter.api.asyncapi.model.TestExecutionFailedEventAsyncDTO;
import io.github.yaforster.trails.adapter.api.asyncapi.model.TestExecutionStartedEventAsyncDTO;
import io.github.yaforster.trails.adapter.api.asyncapi.run.mapper.TestExecutionEventDTOMapper;
import io.github.yaforster.trails.adapter.api.rest.run.TestExecutionLinkFactoryHATEOAS;
import io.github.yaforster.trails.core.persisted.PersistedTestRunResult;
import lombok.AllArgsConstructor;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.springframework.hateoas.Links;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@AllArgsConstructor
public class TestExecutionEventFacade {

	private static final String DEFAULT_ERROR_MESSAGE = "Async API processing failed";

	private static final String GENERIC_FAILURE_MESSAGE = "Test execution failed";

	private final TestExecutionEventDTOMapper mapper;

	private final TestExecutionLinkFactoryHATEOAS linkFactory;

	private final ApiErrorHandlingProperties properties;

	public TestExecutionStartedEventAsyncDTO toStartedEventDTO(UUID executionId) {
		Links links = linkFactory.startedLinks(executionId);
		return mapper.toStartedDTO(executionId, TestExecutionStartedEventAsyncDTO.Status.RUNNING,
				"Test execution started", links);
	}

	public TestExecutionCompletedEventAsyncDTO toCompletedEventDTO(UUID executionId,
			PersistedTestRunResult persistedResult) {
		Links links = linkFactory.completedLinks(executionId, persistedResult);
		return mapper.toCompletedDTO(executionId, TestExecutionCompletedEventAsyncDTO.Status.COMPLETED,
				"Test execution completed", persistedResult.id(), links);
	}

	public TestExecutionFailedEventAsyncDTO toFailedEventDTO(UUID executionId) {
		Links links = linkFactory.failedLinks(executionId);
		return mapper.toFailedDTO(executionId, TestExecutionFailedEventAsyncDTO.Status.FAILED, GENERIC_FAILURE_MESSAGE,
				null, links);
	}

	public TestExecutionFailedEventAsyncDTO toFailedEventDTO(UUID executionId, Exception exception) {
		Links links = linkFactory.failedLinks(executionId);
		ErrorAsyncDTO error = toErrorDTO(exception);
		return mapper.toFailedDTO(executionId, TestExecutionFailedEventAsyncDTO.Status.FAILED, error.getMessage(),
				error, links);
	}

	private ErrorAsyncDTO toErrorDTO(Exception exception) {
		ErrorAsyncDTO error = ErrorAsyncDTO.builder().message(errorMessage(exception)).build();
		if (properties.addStacktraceToResponse()) {
			error.setProblematicElement(exception.getClass().getSimpleName());
			error.setStacktrace(ExceptionUtils.getStackTrace(exception));
		}
		return error;
	}

	private String errorMessage(Exception exception) {
		return DEFAULT_ERROR_MESSAGE;
	}

}
