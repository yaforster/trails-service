package io.github.yaforster.trails.adapter.api.asyncapi;

import io.github.yaforster.trails.adapter.api.asyncapi.model.TestExecutionCompletedEventAsyncDTO;

public interface IReceiveTestExecutionCompletedEvents {

	void receiveTestExecutionCompletedEvents(TestExecutionCompletedEventAsyncDTO value);

}
