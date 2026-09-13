package io.github.yaforster.trails.adapter.api.asyncapi.run;

import io.github.yaforster.trails.adapter.api.ApiErrorHandlingProperties;
import io.github.yaforster.trails.adapter.api.asyncapi.model.ErrorAsyncDTO;
import io.github.yaforster.trails.adapter.api.asyncapi.model.TestExecutionCompletedEventAsyncDTO;
import io.github.yaforster.trails.adapter.api.asyncapi.model.TestExecutionFailedEventAsyncDTO;
import io.github.yaforster.trails.adapter.api.asyncapi.model.TestExecutionStartedEventAsyncDTO;
import io.github.yaforster.trails.adapter.api.asyncapi.run.mapper.TestExecutionEventDTOMapper;
import io.github.yaforster.trails.adapter.api.rest.run.TestExecutionLinkFactoryHATEOAS;
import io.github.yaforster.trails.core.data.ResultIndicator;
import io.github.yaforster.trails.core.persisted.PersistedTestRunResult;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.hateoas.Links;

import java.sql.Timestamp;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class TestExecutionEventFacadeTest {

	@Test
	void toFailedEventDTO_shouldMapExceptionToStructuredError() {
		UUID executionId = UUID.randomUUID();
		RuntimeException exception = new RuntimeException("boom");
		TestExecutionEventDTOMapper mapper = mock(TestExecutionEventDTOMapper.class);
		TestExecutionLinkFactoryHATEOAS linkFactory = mock(TestExecutionLinkFactoryHATEOAS.class);
		ApiErrorHandlingProperties settings = mock(ApiErrorHandlingProperties.class);
		when(linkFactory.failedLinks(executionId)).thenReturn(Links.NONE);
		TestExecutionEventFacade facade = new TestExecutionEventFacade(mapper, linkFactory, settings);

		facade.toFailedEventDTO(executionId, exception);

		verify(mapper).toFailedDTO(eq(executionId), eq(TestExecutionFailedEventAsyncDTO.Status.FAILED),
				eq("Async API processing failed"), any(ErrorAsyncDTO.class), eq(Links.NONE));
	}

	@Test
	void toStartedEventDTO_shouldMapStartedEvent() {
		UUID executionId = UUID.randomUUID();
		TestExecutionEventDTOMapper mapper = mock(TestExecutionEventDTOMapper.class);
		TestExecutionLinkFactoryHATEOAS linkFactory = mock(TestExecutionLinkFactoryHATEOAS.class);
		ApiErrorHandlingProperties settings = mock(ApiErrorHandlingProperties.class);
		when(linkFactory.startedLinks(executionId)).thenReturn(Links.NONE);
		TestExecutionEventFacade facade = new TestExecutionEventFacade(mapper, linkFactory, settings);

		facade.toStartedEventDTO(executionId);

		verify(mapper).toStartedDTO(executionId, TestExecutionStartedEventAsyncDTO.Status.RUNNING,
				"Test execution started", Links.NONE);
	}

	@Test
	void toCompletedEventDTO_shouldMapCompletedEvent() {
		UUID executionId = UUID.randomUUID();
		PersistedTestRunResult persistedResult = persistedResult(41L);
		TestExecutionEventDTOMapper mapper = mock(TestExecutionEventDTOMapper.class);
		TestExecutionLinkFactoryHATEOAS linkFactory = mock(TestExecutionLinkFactoryHATEOAS.class);
		ApiErrorHandlingProperties settings = mock(ApiErrorHandlingProperties.class);
		when(linkFactory.completedLinks(executionId, persistedResult)).thenReturn(Links.NONE);
		TestExecutionEventFacade facade = new TestExecutionEventFacade(mapper, linkFactory, settings);

		facade.toCompletedEventDTO(executionId, persistedResult);

		verify(mapper).toCompletedDTO(executionId, TestExecutionCompletedEventAsyncDTO.Status.COMPLETED,
				"Test execution completed", 41L, Links.NONE);
	}

	@Test
	void toFailedEventDTO_shouldUseGenericFailureMessage() {
		UUID executionId = UUID.randomUUID();
		TestExecutionEventDTOMapper mapper = mock(TestExecutionEventDTOMapper.class);
		TestExecutionLinkFactoryHATEOAS linkFactory = mock(TestExecutionLinkFactoryHATEOAS.class);
		ApiErrorHandlingProperties settings = mock(ApiErrorHandlingProperties.class);
		when(linkFactory.failedLinks(executionId)).thenReturn(Links.NONE);
		TestExecutionEventFacade facade = new TestExecutionEventFacade(mapper, linkFactory, settings);

		facade.toFailedEventDTO(executionId);

		verify(mapper).toFailedDTO(executionId, TestExecutionFailedEventAsyncDTO.Status.FAILED, "Test execution failed",
				null, Links.NONE);
	}

	@Test
	void toFailedEventDTO_shouldUseDefaultExceptionMessage_whenExceptionMessageIsNull() {
		UUID executionId = UUID.randomUUID();
		ArgumentCaptor<ErrorAsyncDTO> errorCaptor = ArgumentCaptor.forClass(ErrorAsyncDTO.class);
		TestExecutionEventDTOMapper mapper = mock(TestExecutionEventDTOMapper.class);
		TestExecutionLinkFactoryHATEOAS linkFactory = mock(TestExecutionLinkFactoryHATEOAS.class);
		ApiErrorHandlingProperties settings = mock(ApiErrorHandlingProperties.class);
		when(linkFactory.failedLinks(executionId)).thenReturn(Links.NONE);
		TestExecutionEventFacade facade = new TestExecutionEventFacade(mapper, linkFactory, settings);

		facade.toFailedEventDTO(executionId, new RuntimeException());

		verify(mapper).toFailedDTO(eq(executionId), eq(TestExecutionFailedEventAsyncDTO.Status.FAILED),
				eq("Async API processing failed"), errorCaptor.capture(), eq(Links.NONE));
	}

	@Test
	void toFailedEventDTO_shouldSetProblematicElementToExceptionClassName() {
		UUID executionId = UUID.randomUUID();
		ArgumentCaptor<ErrorAsyncDTO> errorCaptor = ArgumentCaptor.forClass(ErrorAsyncDTO.class);
		TestExecutionEventDTOMapper mapper = mock(TestExecutionEventDTOMapper.class);
		TestExecutionLinkFactoryHATEOAS linkFactory = mock(TestExecutionLinkFactoryHATEOAS.class);
		ApiErrorHandlingProperties settings = mock(ApiErrorHandlingProperties.class);
		when(linkFactory.failedLinks(executionId)).thenReturn(Links.NONE);
		TestExecutionEventFacade facade = new TestExecutionEventFacade(mapper, linkFactory, settings);

		facade.toFailedEventDTO(executionId, new IllegalArgumentException("bad"));
		verify(mapper).toFailedDTO(eq(executionId), eq(TestExecutionFailedEventAsyncDTO.Status.FAILED),
				eq("Async API processing failed"), errorCaptor.capture(), eq(Links.NONE));

		assertEquals("IllegalArgumentException", errorCaptor.getValue().getProblematicElement());
	}

	@Test
	void toFailedEventDTO_shouldAddStacktrace_whenStacktracesAreEnabled() {
		UUID executionId = UUID.randomUUID();
		ArgumentCaptor<ErrorAsyncDTO> errorCaptor = ArgumentCaptor.forClass(ErrorAsyncDTO.class);
		TestExecutionEventDTOMapper mapper = mock(TestExecutionEventDTOMapper.class);
		TestExecutionLinkFactoryHATEOAS linkFactory = mock(TestExecutionLinkFactoryHATEOAS.class);
		ApiErrorHandlingProperties settings = mock(ApiErrorHandlingProperties.class);
		when(settings.addStacktraceToResponse()).thenReturn(true);
		when(linkFactory.failedLinks(executionId)).thenReturn(Links.NONE);
		TestExecutionEventFacade facade = new TestExecutionEventFacade(mapper, linkFactory, settings);

		facade.toFailedEventDTO(executionId, new IllegalArgumentException("bad"));
		verify(mapper).toFailedDTO(eq(executionId), eq(TestExecutionFailedEventAsyncDTO.Status.FAILED),
				eq("Async API processing failed"), errorCaptor.capture(), eq(Links.NONE));

		assertNotNull(errorCaptor.getValue().getStacktrace());
	}

	private PersistedTestRunResult persistedResult(Long id) {
		return new PersistedTestRunResult(id, 1L, 2L, 3L, Timestamp.valueOf("2026-04-28 10:15:30"),
				ResultIndicator.SUCCESS, "run");
	}

}
