package io.github.yaforster.trails.adapter.api.asyncapi;

import io.github.yaforster.trails.adapter.api.asyncapi.model.TestExecutionFailedEventAsyncDTO;

public interface IReceiveTestExecutionFailedEvents {

	void receiveTestExecutionFailedEvents(TestExecutionFailedEventAsyncDTO value);

}
