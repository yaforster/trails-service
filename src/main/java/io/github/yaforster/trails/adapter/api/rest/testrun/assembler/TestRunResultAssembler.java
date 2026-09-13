package io.github.yaforster.trails.adapter.api.rest.testrun.assembler;

import io.github.yaforster.trails.adapter.api.rest.print.PrintController;
import io.github.yaforster.trails.adapter.api.rest.testrun.TestRunController;
import io.github.yaforster.trails.adapter.api.rest.testrun.model.TestRunResultModel;
import io.github.yaforster.trails.adapter.api.rest.testset.TestSetController;
import io.github.yaforster.trails.core.persisted.PersistedTestRunResult;
import org.jspecify.annotations.NonNull;
import org.springframework.hateoas.server.RepresentationModelAssembler;
import org.springframework.stereotype.Component;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;

import static org.springframework.hateoas.server.core.DummyInvocationUtils.methodOn;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;

@Component
public class TestRunResultAssembler implements RepresentationModelAssembler<TestRunResultContext, TestRunResultModel> {

	private static OffsetDateTime toOffsetDateTime(java.sql.Timestamp timestamp) {
		if (timestamp == null) {
			return null;
		}
		return timestamp.toInstant().atOffset(ZoneOffset.UTC);
	}

	@Override
	public @NonNull TestRunResultModel toModel(@NonNull TestRunResultContext context) {
		TestRunResultModel model = new TestRunResultModel(context.resultId(), context.timestamp(), context.indicator(),
				context.applicationId(), context.stageId(), context.label());
		model.add(linkTo(methodOn(TestRunController.class).getTestRun(context.resultId(), context.applicationId(),
				context.stageId()))
			.withSelfRel());
		model.add(linkTo(
				methodOn(TestRunController.class).listTestRuns(context.applicationId(), context.stageId(), 0, 20))
			.withRel("collection"));
		model.add(linkTo(methodOn(TestSetController.class).listBrowserResults(context.resultId(),
				context.applicationId(), context.stageId(), 0, 20))
			.withRel("testSets"));
		model.add(linkTo(methodOn(PrintController.class).printTestRun(context.applicationId(), context.stageId(),
				context.resultId()))
			.withRel("print"));
		return model;
	}

	public TestRunResultContext toContext(PersistedTestRunResult result) {
		return new TestRunResultContext(result.applicationId(), result.stageId(), result.id(),
				toOffsetDateTime(result.timestamp()), result.status(), result.label());
	}

}
