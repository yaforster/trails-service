package io.github.yaforster.trails.adapter.api.asyncapi.run;

import io.github.yaforster.trails.adapter.api.asyncapi.model.TestExecutionCompletedEventAsyncDTO;
import io.github.yaforster.trails.adapter.api.asyncapi.model.TestExecutionFailedEventAsyncDTO;
import io.github.yaforster.trails.adapter.api.asyncapi.model.TestExecutionStartedEventAsyncDTO;
import io.github.yaforster.trails.adapter.api.TestExecutionEventHub;
import io.github.yaforster.trails.core.data.ResultIndicator;
import io.github.yaforster.trails.core.persisted.PersistedTestRunResult;
import org.junit.jupiter.api.Test;

import java.sql.Timestamp;
import java.util.UUID;

import static org.mockito.Mockito.*;

class TestExecutionAsyncEventPublisherTest {

	private final TestExecutionEventFacade facade = mock(TestExecutionEventFacade.class);

	private final TestExecutionEventHub eventHub = mock(TestExecutionEventHub.class);

	private final TestExecutionEventPublisherImpl publisher = new TestExecutionEventPublisherImpl(facade, eventHub);

	@Test
	void publishStarted_shouldCreateEventViaFacadeAndPublishToEventService() {
		UUID executionId = UUID.randomUUID();
		TestExecutionStartedEventAsyncDTO dto = TestExecutionStartedEventAsyncDTO.builder().build();
		when(facade.toStartedEventDTO(executionId)).thenReturn(dto);

		publisher.publishStarted(executionId);

		verify(facade).toStartedEventDTO(executionId);
		verify(eventHub).publishStarted(executionId, dto);
	}

	@Test
	void publishCompleted_shouldCreateEventViaFacadeAndPublishToEventService() {
		UUID executionId = UUID.randomUUID();
		PersistedTestRunResult result = new PersistedTestRunResult(1L, 2L, 3L, 4L,
				new Timestamp(System.currentTimeMillis()), ResultIndicator.SUCCESS, "name");
		TestExecutionCompletedEventAsyncDTO dto = TestExecutionCompletedEventAsyncDTO.builder().build();
		when(facade.toCompletedEventDTO(executionId, result)).thenReturn(dto);

		publisher.publishCompleted(executionId, result);

		verify(facade).toCompletedEventDTO(executionId, result);
		verify(eventHub).publishCompleted(executionId, dto);
	}

	@Test
	void publishFailed_shouldCreateEventViaFacadeAndPublishToEventService() {
		UUID executionId = UUID.randomUUID();
		TestExecutionFailedEventAsyncDTO dto = TestExecutionFailedEventAsyncDTO.builder().build();
		when(facade.toFailedEventDTO(executionId)).thenReturn(dto);

		publisher.publishFailed(executionId);

		verify(facade).toFailedEventDTO(executionId);
		verify(eventHub).publishFailed(executionId, dto);
	}

}
