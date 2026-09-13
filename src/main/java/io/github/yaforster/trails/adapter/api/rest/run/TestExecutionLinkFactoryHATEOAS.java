package io.github.yaforster.trails.adapter.api.rest.run;

import io.github.yaforster.trails.adapter.api.rest.testrun.TestRunController;
import io.github.yaforster.trails.core.persisted.PersistedTestRunResult;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.Links;
import org.springframework.stereotype.Component;

import java.util.UUID;

import static org.springframework.hateoas.server.core.DummyInvocationUtils.methodOn;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;

@Component
public class TestExecutionLinkFactoryHATEOAS {

	public Links startedLinks(UUID executionId) {
		return Links
			.of(linkTo(methodOn(TestExecutionController.class).streamTestExecutionEvents(executionId)).withSelfRel());
	}

	public Links completedLinks(UUID executionId, PersistedTestRunResult persistedResult) {
		return Links.of(
				linkTo(methodOn(TestExecutionController.class).streamTestExecutionEvents(executionId)).withSelfRel(),
				linkTo(methodOn(TestRunController.class).getTestRun(persistedResult.id(),
						persistedResult.applicationId(), persistedResult.stageId()))
					.withRel("result"));
	}

	public Links failedLinks(UUID executionId) {
		return Links.of(
				linkTo(methodOn(TestExecutionController.class).streamTestExecutionEvents(executionId)).withSelfRel(),
				Link.of("/api/testruns/{applicationId}/{stageId}/{testRunId}").withRel("result"));
	}

}
