package io.github.yaforster.trails.adapter.api.asyncapi.run;

import io.github.yaforster.trails.adapter.api.asyncapi.model.TestExecutionCompletedEventAsyncDTO;
import io.github.yaforster.trails.adapter.api.asyncapi.model.TestExecutionFailedEventAsyncDTO;
import io.github.yaforster.trails.adapter.api.asyncapi.model.TestExecutionStartedEventAsyncDTO;
import io.github.yaforster.trails.adapter.api.TestExecutionEventHub;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.mockito.Mockito.*;

class TestExecutionAsyncEventListenerTest {

	private final TestExecutionEventHub eventHub = mock(TestExecutionEventHub.class);

	private final TestExecutionEventListener listener = new TestExecutionEventListener(eventHub);

	@Test
	void receiveTestExecutionStartedEvents_shouldPublishWhenExecutionIdExists() {
		String executionId = UUID.randomUUID().toString();
		TestExecutionStartedEventAsyncDTO event = TestExecutionStartedEventAsyncDTO.builder()
			.executionId(executionId)
			.build();

		listener.receiveTestExecutionStartedEvents(event);

		verify(eventHub).publishStarted(UUID.fromString(executionId), event);
	}

	@Test
	void receiveTestExecutionStartedEvents_shouldIgnoreWhenExecutionIdMissing() {
		TestExecutionStartedEventAsyncDTO event = TestExecutionStartedEventAsyncDTO.builder().build();

		listener.receiveTestExecutionStartedEvents(event);

		verifyNoInteractions(eventHub);
	}

	@Test
	void receiveTestExecutionStartedEvents_shouldIgnoreWhenExecutionIdIsMalformed() {
		TestExecutionStartedEventAsyncDTO event = TestExecutionStartedEventAsyncDTO.builder()
			.executionId("not-a-uuid")
			.build();

		listener.receiveTestExecutionStartedEvents(event);

		verifyNoInteractions(eventHub);
	}

	@Test
	void receiveTestExecutionStartedEvents_shouldIgnoreWhenEventIsNull() {
		listener.receiveTestExecutionStartedEvents(null);

		verifyNoInteractions(eventHub);
	}

	@Test
	void receiveTestExecutionCompletedEvents_shouldPublishWhenExecutionIdExists() {
		String executionId = UUID.randomUUID().toString();
		TestExecutionCompletedEventAsyncDTO event = TestExecutionCompletedEventAsyncDTO.builder()
			.executionId(executionId)
			.build();

		listener.receiveTestExecutionCompletedEvents(event);

		verify(eventHub).publishCompleted(UUID.fromString(executionId), event);
	}

	@Test
	void receiveTestExecutionCompletedEvents_shouldIgnoreWhenExecutionIdMissing() {
		TestExecutionCompletedEventAsyncDTO event = TestExecutionCompletedEventAsyncDTO.builder().build();

		listener.receiveTestExecutionCompletedEvents(event);

		verifyNoInteractions(eventHub);
	}

	@Test
	void receiveTestExecutionCompletedEvents_shouldIgnoreWhenEventIsNull() {
		listener.receiveTestExecutionCompletedEvents(null);

		verifyNoInteractions(eventHub);
	}

	@Test
	void receiveTestExecutionFailedEvents_shouldPublishWhenExecutionIdExists() {
		String executionId = UUID.randomUUID().toString();
		TestExecutionFailedEventAsyncDTO event = TestExecutionFailedEventAsyncDTO.builder()
			.executionId(executionId)
			.build();

		listener.receiveTestExecutionFailedEvents(event);

		verify(eventHub).publishFailed(UUID.fromString(executionId), event);
	}

	@Test
	void receiveTestExecutionFailedEvents_shouldIgnoreWhenExecutionIdMissing() {
		TestExecutionFailedEventAsyncDTO event = TestExecutionFailedEventAsyncDTO.builder().build();

		listener.receiveTestExecutionFailedEvents(event);

		verifyNoInteractions(eventHub);
	}

	@Test
	void receiveTestExecutionFailedEvents_shouldIgnoreWhenEventIsNull() {
		listener.receiveTestExecutionFailedEvents(null);

		verifyNoInteractions(eventHub);
	}

}
