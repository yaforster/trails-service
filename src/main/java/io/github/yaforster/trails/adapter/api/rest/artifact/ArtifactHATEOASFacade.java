package io.github.yaforster.trails.adapter.api.rest.artifact;

import io.github.yaforster.trails.adapter.api.rest.artifact.assembler.ArtifactModelAssembler;
import io.github.yaforster.trails.adapter.api.rest.artifact.assembler.PersistedArtifactContext;
import io.github.yaforster.trails.adapter.api.rest.artifact.mapper.PagedArtifactMapper;
import io.github.yaforster.trails.adapter.api.rest.artifact.model.ArtifactModel;
import io.github.yaforster.trails.adapter.api.rest.common.PagedResultPageAdapter;
import io.github.yaforster.trails.adapter.api.rest.model.PagedArtifactDTO;
import io.github.yaforster.trails.core.persisted.PersistedArtifact;
import io.github.yaforster.trails.core.PagedResult;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.web.PagedResourcesAssembler;
import org.springframework.hateoas.PagedModel;
import org.springframework.stereotype.Component;

import static org.springframework.hateoas.server.core.DummyInvocationUtils.methodOn;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;

@Component
@AllArgsConstructor
public class ArtifactHATEOASFacade {

	private final ArtifactModelAssembler assembler;

	private final PagedResourcesAssembler<PersistedArtifactContext> pagedAssembler;

	private final PagedArtifactMapper pagedArtifactMapper;

	public PagedArtifactDTO toPagedDTOInHierarchy(Long applicationId, Long stageId, Long testRunId,
			Long testSetResultId, Long pathResultId, PagedResult<PersistedArtifact> pagedFiles) {
		Page<PersistedArtifactContext> contexts = PagedResultPageAdapter
			.toSpringPage(pagedFiles.map(entity -> new PersistedArtifactContext(applicationId, stageId, testRunId,
					testSetResultId, pathResultId, entity)));
		PagedModel<ArtifactModel> model = pagedAssembler.toModel(contexts, assembler);
		addPagingLinks(model, applicationId, stageId, testRunId, testSetResultId, pathResultId, pagedFiles.page(),
				pagedFiles.size(), pagedFiles.totalPages());
		return pagedArtifactMapper.toPagedDto(model);
	}

	private void addPagingLinks(PagedModel<?> model, Long applicationId, Long stageId, Long testRunId,
			Long testSetResultId, Long pathResultId, int page, int size, int totalPages) {
		int lastPage = Math.max(totalPages - 1, 0);

		model.add(linkTo(methodOn(ArtifactController.class).listArtifactsInTestPath(applicationId, stageId, testRunId,
				testSetResultId, pathResultId, page, size))
			.withSelfRel());

		model.add(linkTo(methodOn(ArtifactController.class).listArtifactsInTestPath(applicationId, stageId, testRunId,
				testSetResultId, pathResultId, 0, size))
			.withRel("first"));

		model.add(linkTo(methodOn(ArtifactController.class).listArtifactsInTestPath(applicationId, stageId, testRunId,
				testSetResultId, pathResultId, lastPage, size))
			.withRel("last"));

		if (page > 0) {
			model.add(linkTo(methodOn(ArtifactController.class).listArtifactsInTestPath(applicationId, stageId,
					testRunId, testSetResultId, pathResultId, page - 1, size))
				.withRel("prev"));
		}

		if (page < lastPage) {
			model.add(linkTo(methodOn(ArtifactController.class).listArtifactsInTestPath(applicationId, stageId,
					testRunId, testSetResultId, pathResultId, page + 1, size))
				.withRel("next"));
		}
	}

}
