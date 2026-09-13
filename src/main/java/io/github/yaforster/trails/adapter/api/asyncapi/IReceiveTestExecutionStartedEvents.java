package io.github.yaforster.trails.adapter.api.asyncapi;

import io.github.yaforster.trails.adapter.api.asyncapi.model.TestExecutionStartedEventAsyncDTO;

public interface IReceiveTestExecutionStartedEvents {

	void receiveTestExecutionStartedEvents(TestExecutionStartedEventAsyncDTO value);

}
