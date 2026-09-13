package io.github.yaforster.trails.adapter.api.asyncapi.run;

import io.github.yaforster.trails.adapter.api.TestExecutionEventHub;
import io.github.yaforster.trails.adapter.api.asyncapi.model.TestExecutionFailedEventAsyncDTO;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

class AsyncAPIExceptionMapperTest {

	@Test
	void toFailedEvent_shouldPublishFailedEvent_whenUuidArgumentExists() {
		UUID executionId = UUID.randomUUID();
		RuntimeException exception = new RuntimeException("boom");
		TestExecutionFailedEventAsyncDTO failedEvent = TestExecutionFailedEventAsyncDTO.builder().build();
		TestExecutionEventFacade eventFacade = mock(TestExecutionEventFacade.class);
		TestExecutionEventHub eventHub = mock(TestExecutionEventHub.class);
		when(eventFacade.toFailedEventDTO(executionId, exception)).thenReturn(failedEvent);
		AsyncAPIExceptionMapper mapper = new AsyncAPIExceptionMapper(eventFacade, eventHub);

		mapper.toFailedEvent(new Object[] { executionId }, exception);

		verify(eventHub).publishFailed(executionId, failedEvent);
	}

	@Test
	void toFailedEvent_shouldReturnTrue_whenUuidArgumentExists() {
		UUID executionId = UUID.randomUUID();
		RuntimeException exception = new RuntimeException("boom");
		TestExecutionEventFacade eventFacade = mock(TestExecutionEventFacade.class);
		TestExecutionEventHub eventHub = mock(TestExecutionEventHub.class);
		when(eventFacade.toFailedEventDTO(executionId, exception))
			.thenReturn(TestExecutionFailedEventAsyncDTO.builder().build());
		AsyncAPIExceptionMapper mapper = new AsyncAPIExceptionMapper(eventFacade, eventHub);

		boolean mapped = mapper.toFailedEvent(new Object[] { executionId }, exception);

		assertTrue(mapped);
	}

	@Test
	void toFailedEvent_shouldReturnFalse_whenFindExecutionIDIsMissing() {
		AsyncAPIExceptionMapper mapper = new AsyncAPIExceptionMapper(mock(TestExecutionEventFacade.class),
				mock(TestExecutionEventHub.class));

		boolean mapped = mapper.toFailedEvent(new Object[] { new Object() }, new RuntimeException("boom"));

		assertFalse(mapped);
	}

	@Test
	void toFailedEvent_shouldReturnFalse_whenArgumentsAreNull() {
		AsyncAPIExceptionMapper mapper = new AsyncAPIExceptionMapper(mock(TestExecutionEventFacade.class),
				mock(TestExecutionEventHub.class));

		boolean mapped = mapper.toFailedEvent(null, new RuntimeException("boom"));

		assertFalse(mapped);
	}

	@Test
	void toFailedEvent_shouldPublishFailedEvent_whenArgumentHasExecutionIdGetter() {
		UUID executionId = UUID.randomUUID();
		RuntimeException exception = new RuntimeException("boom");
		TestExecutionFailedEventAsyncDTO failedEvent = TestExecutionFailedEventAsyncDTO.builder().build();
		TestExecutionEventFacade eventFacade = mock(TestExecutionEventFacade.class);
		TestExecutionEventHub eventHub = mock(TestExecutionEventHub.class);
		when(eventFacade.toFailedEventDTO(executionId, exception)).thenReturn(failedEvent);
		AsyncAPIExceptionMapper mapper = new AsyncAPIExceptionMapper(eventFacade, eventHub);

		mapper.toFailedEvent(new Object[] { new EventWithExecutionId(executionId.toString()) }, exception);

		verify(eventHub).publishFailed(executionId, failedEvent);
	}

	@Test
	void toFailedEvent_shouldReturnTrue_whenArgumentHasExecutionIdGetter() {
		UUID executionId = UUID.randomUUID();
		RuntimeException exception = new RuntimeException("boom");
		TestExecutionEventFacade eventFacade = mock(TestExecutionEventFacade.class);
		TestExecutionEventHub eventHub = mock(TestExecutionEventHub.class);
		when(eventFacade.toFailedEventDTO(executionId, exception))
			.thenReturn(TestExecutionFailedEventAsyncDTO.builder().build());
		AsyncAPIExceptionMapper mapper = new AsyncAPIExceptionMapper(eventFacade, eventHub);

		boolean mapped = mapper.toFailedEvent(new Object[] { new EventWithExecutionId(executionId.toString()) },
				exception);

		assertTrue(mapped);
	}

	@Test
	void toFailedEvent_shouldReturnFalse_whenExecutionIdGetterReturnsBlankString() {
		AsyncAPIExceptionMapper mapper = new AsyncAPIExceptionMapper(mock(TestExecutionEventFacade.class),
				mock(TestExecutionEventHub.class));

		boolean mapped = mapper.toFailedEvent(new Object[] { new EventWithExecutionId(" ") },
				new RuntimeException("boom"));

		assertFalse(mapped);
	}

	@Test
	void toFailedEvent_shouldReturnFalse_whenExecutionIdGetterReturnsInvalidUuid() {
		AsyncAPIExceptionMapper mapper = new AsyncAPIExceptionMapper(mock(TestExecutionEventFacade.class),
				mock(TestExecutionEventHub.class));

		boolean mapped = mapper.toFailedEvent(new Object[] { new EventWithExecutionId("invalid") },
				new RuntimeException("boom"));

		assertFalse(mapped);
	}

	@Test
	void toFailedEvent_shouldReturnFalse_whenExecutionIdGetterReturnsNonString() {
		AsyncAPIExceptionMapper mapper = new AsyncAPIExceptionMapper(mock(TestExecutionEventFacade.class),
				mock(TestExecutionEventHub.class));

		boolean mapped = mapper.toFailedEvent(new Object[] { new EventWithNonStringExecutionId() },
				new RuntimeException("boom"));

		assertFalse(mapped);
	}

	@Test
	void toFailedEvent_shouldUseFirstResolvableExecutionId_whenMultipleArgumentsExist() {
		UUID executionId = UUID.randomUUID();
		RuntimeException exception = new RuntimeException("boom");
		TestExecutionFailedEventAsyncDTO failedEvent = TestExecutionFailedEventAsyncDTO.builder().build();
		TestExecutionEventFacade eventFacade = mock(TestExecutionEventFacade.class);
		TestExecutionEventHub eventHub = mock(TestExecutionEventHub.class);
		when(eventFacade.toFailedEventDTO(executionId, exception)).thenReturn(failedEvent);
		AsyncAPIExceptionMapper mapper = new AsyncAPIExceptionMapper(eventFacade, eventHub);

		mapper.toFailedEvent(new Object[] { new Object(), executionId, UUID.randomUUID() }, exception);

		verify(eventHub).publishFailed(executionId, failedEvent);
	}

	private record EventWithExecutionId(String executionId) {

		public String getExecutionId() {
			return executionId;
		}
	}

	private static class EventWithNonStringExecutionId {

		public Long getExecutionId() {
			return 1L;
		}

	}

}
