package io.github.yaforster.trails.adapter.api.rest.stage.assembler;

import io.github.yaforster.trails.adapter.api.ResourceAuthorization;
import io.github.yaforster.trails.adapter.api.rest.element.ElementController;
import io.github.yaforster.trails.adapter.api.rest.stage.StageController;
import io.github.yaforster.trails.adapter.api.rest.stage.model.StageModel;
import io.github.yaforster.trails.adapter.api.rest.testplan.TestPlanController;
import io.github.yaforster.trails.adapter.api.rest.testrun.TestRunController;
import io.github.yaforster.trails.core.persisted.PersistedStage;
import jakarta.annotation.Nonnull;
import lombok.AllArgsConstructor;
import org.jspecify.annotations.NonNull;
import org.springframework.hateoas.server.RepresentationModelAssembler;
import org.springframework.stereotype.Component;

import static io.github.yaforster.trails.adapter.api.rest.hateoas.HATEOASLinks.delete;
import static io.github.yaforster.trails.adapter.api.rest.hateoas.HATEOASLinks.post;
import static org.springframework.hateoas.server.core.DummyInvocationUtils.methodOn;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;

@Component
@AllArgsConstructor
public class StageModelAssembler implements RepresentationModelAssembler<PersistedStageContext, StageModel> {

	private final ResourceAuthorization resourceAuthorization;

	@Override
	public @Nonnull StageModel toModel(@NonNull PersistedStageContext stageContext) {
		PersistedStage persistedStage = stageContext.persistedStage();
		StageModel model = new StageModel(persistedStage.id(), persistedStage.label(), persistedStage.url(),
				persistedStage.retired());
		model.add(linkTo(methodOn(StageController.class).listStages(stageContext.applicationID(), 0, 20, false))
			.withRel("collection"));

		if (stageContext.hasElements()) {
			model.add(linkTo(methodOn(ElementController.class).listElements(stageContext.applicationID(),
					persistedStage.id(), 0, 20, false))
				.withRel("elements"));
		}

		model.add(linkTo(methodOn(TestPlanController.class).listTestPlans(stageContext.applicationID(),
				persistedStage.id(), 0, 20, false))
			.withRel("testPlans"));

		model.add(linkTo(methodOn(TestRunController.class).listTestRuns(stageContext.applicationID(),
				persistedStage.id(), 0, 20))
			.withRel("testRuns"));

		model.add(linkTo(
				methodOn(StageController.class).getStage(stageContext.applicationID(), persistedStage.id(), false))
			.withSelfRel());

		if (resourceAuthorization.canManageResources() && persistedStage.retired()) {
			model.add(post(linkTo(
					methodOn(StageController.class).restoreStage(stageContext.applicationID(), persistedStage.id()))
				.withRel("restore")));
		}
		else if (resourceAuthorization.canManageResources()) {
			model.add(delete(linkTo(
					methodOn(StageController.class).deleteStage(stageContext.applicationID(), persistedStage.id()))
				.withRel("delete")));
		}

		return model;
	}

}
