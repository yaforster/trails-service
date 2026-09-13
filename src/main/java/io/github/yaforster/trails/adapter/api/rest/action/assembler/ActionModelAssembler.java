package io.github.yaforster.trails.adapter.api.rest.action.assembler;

import io.github.yaforster.trails.adapter.api.rest.action.ActionController;
import io.github.yaforster.trails.adapter.api.rest.action.model.ActionModel;
import io.github.yaforster.trails.adapter.api.rest.testplan.TestPlanController;
import io.github.yaforster.trails.core.persisted.PersistedAction;
import io.github.yaforster.trails.core.test.Action;
import org.jspecify.annotations.NonNull;
import org.springframework.hateoas.server.RepresentationModelAssembler;
import org.springframework.stereotype.Component;

import static org.springframework.hateoas.server.core.DummyInvocationUtils.methodOn;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;

@Component
public class ActionModelAssembler implements RepresentationModelAssembler<PersistedActionContext, ActionModel> {

	@Override
	public @NonNull ActionModel toModel(@NonNull PersistedActionContext context) {
		PersistedAction persistedAction = context.persistedAction();
		Action action = persistedAction.action();

		ActionModel model = new ActionModel(persistedAction.referenceId(), context.applicationId(), context.stageId(),
				action);

		model.add(linkTo(methodOn(ActionController.class).getActions(context.applicationId(), context.stageId(),
				context.testPlanId(), 0, 20))
			.withRel("collection"));

		model.add(linkTo(methodOn(TestPlanController.class).getTestPlan(context.applicationId(), context.stageId(),
				context.testPlanId(), false))
			.withRel("testPlan"));

		return model;
	}

}
