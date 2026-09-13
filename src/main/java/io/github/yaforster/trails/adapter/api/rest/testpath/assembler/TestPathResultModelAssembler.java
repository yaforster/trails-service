package io.github.yaforster.trails.adapter.api.rest.testpath.assembler;

import io.github.yaforster.trails.adapter.api.rest.actionresult.ActionResultController;
import io.github.yaforster.trails.adapter.api.rest.artifact.ArtifactController;
import io.github.yaforster.trails.adapter.api.rest.print.PrintController;
import io.github.yaforster.trails.adapter.api.rest.testpath.TestPathController;
import io.github.yaforster.trails.adapter.api.rest.testpath.model.TestPathResultModel;
import io.github.yaforster.trails.adapter.api.rest.testset.TestSetController;
import io.github.yaforster.trails.core.persisted.PersistedTestPathResult;
import org.jspecify.annotations.NonNull;
import org.springframework.hateoas.server.RepresentationModelAssembler;
import org.springframework.stereotype.Component;

import static org.springframework.hateoas.server.core.DummyInvocationUtils.methodOn;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;

@Component
public class TestPathResultModelAssembler
		implements RepresentationModelAssembler<PersistedTestPathResultContext, TestPathResultModel> {

	@Override
	public @NonNull TestPathResultModel toModel(@NonNull PersistedTestPathResultContext context) {
		PersistedTestPathResult persistedTestPathResult = context.persistedTestPathResult();
		TestPathResultModel model = new TestPathResultModel(persistedTestPathResult.id());

		model.add(linkTo(methodOn(TestPathController.class).getPathResultInTestSet(context.applicationId(),
				context.stageId(), context.testRunId(), context.testSetResultId(), persistedTestPathResult.id()))
			.withSelfRel());

		model.add(linkTo(methodOn(TestPathController.class).listPathResultsInTestSet(context.applicationId(),
				context.stageId(), context.testRunId(), context.testSetResultId(), 0, 20))
			.withRel("collection"));

		model.add(linkTo(methodOn(TestSetController.class).getBrowserResultInTestRun(context.testRunId(),
				context.applicationId(), context.stageId(), context.testSetResultId()))
			.withRel("testSetResult"));

		model
			.add(linkTo(methodOn(ActionResultController.class).listActionResultsChainInTestPath(context.applicationId(),
					context.stageId(), context.testRunId(), context.testSetResultId(), persistedTestPathResult.id()))
				.withRel("actionResults"));

		model.add(linkTo(methodOn(ArtifactController.class).listArtifactsInTestPath(context.applicationId(),
				context.stageId(), context.testRunId(), context.testSetResultId(), persistedTestPathResult.id(), 0, 20))
			.withRel("downloadedFiles"));

		model.add(linkTo(methodOn(PrintController.class).printPathResultInTestSet(context.applicationId(),
				context.stageId(), context.testRunId(), context.testSetResultId(), persistedTestPathResult.id()))
			.withRel("print"));

		return model;
	}

}
