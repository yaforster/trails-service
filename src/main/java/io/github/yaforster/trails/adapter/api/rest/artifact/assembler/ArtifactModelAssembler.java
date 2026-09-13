package io.github.yaforster.trails.adapter.api.rest.artifact.assembler;

import io.github.yaforster.trails.adapter.api.rest.artifact.ArtifactController;
import io.github.yaforster.trails.adapter.api.rest.artifact.model.ArtifactModel;
import io.github.yaforster.trails.core.persisted.PersistedArtifact;
import org.jspecify.annotations.NonNull;
import org.springframework.hateoas.server.RepresentationModelAssembler;
import org.springframework.stereotype.Component;

import static org.springframework.hateoas.server.core.DummyInvocationUtils.methodOn;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;

@Component
public class ArtifactModelAssembler implements RepresentationModelAssembler<PersistedArtifactContext, ArtifactModel> {

	@Override
	public @NonNull ArtifactModel toModel(@NonNull PersistedArtifactContext context) {
		PersistedArtifact persistedArtifact = context.persistedArtifact();
		ArtifactModel model = new ArtifactModel(persistedArtifact.id(), ArtifactModel.Type.OTHER,
				persistedArtifact.filename());

		model.add(linkTo(methodOn(ArtifactController.class).downloadArtifactInTestPath(context.applicationId(),
				context.stageId(), context.testRunId(), context.testSetResultId(), context.pathResultId(),
				persistedArtifact.id()))
			.withSelfRel());

		model.add(linkTo(methodOn(ArtifactController.class).listArtifactsInTestPath(context.applicationId(),
				context.stageId(), context.testRunId(), context.testSetResultId(), context.pathResultId(), 0, 20))
			.withRel("collection"));

		return model;
	}

}
