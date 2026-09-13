package io.github.yaforster.trails.adapter.api.rest.actionresult.assembler;

import io.github.yaforster.trails.adapter.api.rest.actionresult.ActionResultController;
import io.github.yaforster.trails.adapter.api.rest.actionresult.model.ActionResultModel;
import io.github.yaforster.trails.adapter.api.rest.artifact.ArtifactController;
import io.github.yaforster.trails.adapter.api.rest.testpath.TestPathController;
import io.github.yaforster.trails.core.persisted.PersistedActionResult;
import org.jspecify.annotations.NonNull;
import org.springframework.hateoas.server.RepresentationModelAssembler;
import org.springframework.stereotype.Component;

import static org.springframework.hateoas.server.core.DummyInvocationUtils.methodOn;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;

@Component
public class ActionResultModelAssembler
		implements RepresentationModelAssembler<PersistedActionResultContext, ActionResultModel> {

	@Override
	public @NonNull ActionResultModel toModel(@NonNull PersistedActionResultContext context) {
		PersistedActionResult persistedActionResult = context.persistedActionResult();

		ActionResultModel model = new ActionResultModel(persistedActionResult.id(), persistedActionResult.actionId(),
				persistedActionResult.label(), persistedActionResult.message(), persistedActionResult.resultType(),
				persistedActionResult.exceptionMessageFromAction());

		model
			.add(linkTo(methodOn(ActionResultController.class).listActionResultsChainInTestPath(context.applicationId(),
					context.stageId(), context.testRunId(), context.testSetResultId(), context.pathResultId()))
				.withRel("collection"));

		model.add(linkTo(methodOn(TestPathController.class).getPathResultInTestSet(context.applicationId(),
				context.stageId(), context.testRunId(), context.testSetResultId(), context.pathResultId()))
			.withRel("pathResult"));

		if (context.hasScreenshot()) {
			model.add(linkTo(methodOn(ArtifactController.class).downloadActionResultScreenshotInTestPath(
					context.applicationId(), context.stageId(), context.testRunId(), context.testSetResultId(),
					context.pathResultId(), persistedActionResult.id()))
				.withRel("screenshot"));
		}

		return model;
	}

}
