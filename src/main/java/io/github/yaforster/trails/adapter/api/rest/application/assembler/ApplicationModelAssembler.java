package io.github.yaforster.trails.adapter.api.rest.application.assembler;

import io.github.yaforster.trails.adapter.api.ResourceAuthorization;
import io.github.yaforster.trails.adapter.api.rest.application.ApplicationController;
import io.github.yaforster.trails.adapter.api.rest.application.model.ApplicationModel;
import io.github.yaforster.trails.adapter.api.rest.stage.StageController;
import io.github.yaforster.trails.core.persisted.PersistedApplication;
import jakarta.annotation.Nonnull;
import lombok.AllArgsConstructor;
import org.springframework.hateoas.server.RepresentationModelAssembler;
import org.springframework.stereotype.Component;

import static io.github.yaforster.trails.adapter.api.rest.hateoas.HATEOASLinks.delete;
import static io.github.yaforster.trails.adapter.api.rest.hateoas.HATEOASLinks.post;
import static org.springframework.hateoas.server.core.DummyInvocationUtils.methodOn;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;

@Component
@AllArgsConstructor
public class ApplicationModelAssembler
		implements RepresentationModelAssembler<PersistedApplicationContext, ApplicationModel> {

	private final ResourceAuthorization resourceAuthorization;

	@Override
	public @Nonnull ApplicationModel toModel(@Nonnull PersistedApplicationContext context) {
		PersistedApplication persistedApplication = context.persistedApplication();
		ApplicationModel model = new ApplicationModel(persistedApplication.id(), persistedApplication.label(),
				persistedApplication.retired());

		model.add(linkTo(methodOn(ApplicationController.class).listApplications(0, 20, false)).withRel("collection"));

		if (context.hasStages()) {
			model.add(linkTo(methodOn(StageController.class).listStages(persistedApplication.id(), 0, 20, false))
				.withRel("stages"));
		}

		model.add(linkTo(methodOn(ApplicationController.class).getApplication(persistedApplication.id(), false))
			.withSelfRel());

		if (resourceAuthorization.canManageResources() && persistedApplication.retired()) {
			model.add(post(linkTo(methodOn(ApplicationController.class).restoreApplication(persistedApplication.id()))
				.withRel("restore")));
		}
		else if (resourceAuthorization.canManageResources()) {
			model.add(delete(linkTo(methodOn(ApplicationController.class).deleteApplication(persistedApplication.id()))
				.withRel("delete")));
		}

		return model;
	}

}
