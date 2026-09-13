package io.github.yaforster.trails.adapter.api.rest.element;

import io.github.yaforster.trails.adapter.api.ResourceAuthorization;
import io.github.yaforster.trails.adapter.api.rest.deletion.DatabaseDeletionResultMapper;
import io.github.yaforster.trails.adapter.api.rest.deletion.DatabaseDeletionResultModelMapper;
import io.github.yaforster.trails.adapter.api.rest.common.PagedResultPageAdapter;
import io.github.yaforster.trails.adapter.api.rest.element.assembler.ElementModelAssembler;
import io.github.yaforster.trails.adapter.api.rest.element.assembler.PersistedElementContext;
import io.github.yaforster.trails.adapter.api.rest.element.mapper.*;
import io.github.yaforster.trails.adapter.api.rest.element.model.ElementModel;
import io.github.yaforster.trails.adapter.api.rest.hateoas.HATEOASFacade;
import io.github.yaforster.trails.adapter.api.rest.model.*;
import io.github.yaforster.trails.core.data.TrailsScreenshotFile;
import io.github.yaforster.trails.core.PagedResult;
import io.github.yaforster.trails.core.definition.ElementDefinition;
import io.github.yaforster.trails.core.deletion.DatabaseDeletionResult;
import io.github.yaforster.trails.core.persisted.PersistedElement;
import org.springframework.data.domain.Page;
import org.springframework.data.web.PagedResourcesAssembler;
import org.springframework.hateoas.PagedModel;
import org.springframework.stereotype.Component;

import static io.github.yaforster.trails.adapter.api.rest.hateoas.HATEOASLinks.put;
import static org.springframework.hateoas.server.core.DummyInvocationUtils.methodOn;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;

@Component
public class ElementHATEOASFacade extends HATEOASFacade {

	private final ElementDefinitionDTOMapper elementDefinitionDTOMapper;

	private final PersistedElementDTOMapper persistedElementDTOMapper;

	private final ElementDTOMapper elementMapper;

	private final ElementModelAssembler assembler;

	private final PagedResourcesAssembler<PersistedElementContext> pagedAssembler;

	private final PagedElementMapper pagedElementMapper;

	private final ScreenshotArtifactDTOMapper screenshotArtifactDTOMapper;

	private final ResourceAuthorization resourceAuthorization;

	public ElementHATEOASFacade(ElementDefinitionDTOMapper elementDefinitionDTOMapper,
			PersistedElementDTOMapper persistedElementDTOMapper, ElementDTOMapper elementMapper,
			ElementModelAssembler assembler, PagedResourcesAssembler<PersistedElementContext> pagedAssembler,
			PagedElementMapper pagedElementMapper, ScreenshotArtifactDTOMapper screenshotArtifactDTOMapper,
			ResourceAuthorization resourceAuthorization, DatabaseDeletionResultMapper databaseDeletionResultMapper,
			DatabaseDeletionResultModelMapper databaseDeletionResultModelMapper) {
		super(databaseDeletionResultMapper, databaseDeletionResultModelMapper);
		this.elementDefinitionDTOMapper = elementDefinitionDTOMapper;
		this.persistedElementDTOMapper = persistedElementDTOMapper;
		this.elementMapper = elementMapper;
		this.assembler = assembler;
		this.pagedAssembler = pagedAssembler;
		this.pagedElementMapper = pagedElementMapper;
		this.screenshotArtifactDTOMapper = screenshotArtifactDTOMapper;
		this.resourceAuthorization = resourceAuthorization;
	}

	protected ElementDefinition toDomain(ElementDefinitionDTO dto) {
		return elementDefinitionDTOMapper.toDomain(dto);
	}

	public PersistedElementDTO toDTO(PersistedElement persistedElement) {
		return persistedElementDTOMapper.toDTO(persistedElement);
	}

	public ArtifactDTO toDTO(Long applicationId, Long stageId, Long elementId, Long screenshotId,
			TrailsScreenshotFile file) {
		return screenshotArtifactDTOMapper.toDTO(applicationId, stageId, elementId, screenshotId, file);
	}

	public ElementDTO mapToElementDTO(PersistedElementContext context) {
		ElementModel model = assembler.toModel(context);
		return elementMapper.toDTO(model);
	}

	public PagedElementDTO mapContextsToPagedElementDTO(Long applicationId, Long stageId,
			PagedResult<PersistedElementContext> contexts) {
		PagedModel<ElementModel> model = pagedAssembler.toModel(PagedResultPageAdapter.toSpringPage(contexts),
				assembler);
		addPagingLinks(model, applicationId, stageId, contexts.page(), contexts.size(), contexts.totalPages());
		return pagedElementMapper.toPagedDto(model);
	}

	public DatabaseDeletionResultDTO toDTO(Long applicationId, Long stageId, DatabaseDeletionResult deletionResult) {
		return toDeletionDTO(deletionResult,
				deletionNotFoundModel -> deletionNotFoundModel
					.add(linkTo(methodOn(ElementController.class).listElements(applicationId, stageId, 0, 20, false))
						.withRel("collection")));
	}

	private void addPagingLinks(PagedModel<?> model, Long applicationId, Long stageId, int page, int size,
			int totalPages) {
		int lastPage = Math.max(totalPages - 1, 0);

		model.add(linkTo(methodOn(ElementController.class).listElements(applicationId, stageId, page, size, false))
			.withSelfRel());

		if (resourceAuthorization.canManageResources()) {
			model.add(put(linkTo(methodOn(ElementController.class).createNewElement(applicationId, stageId, null))
				.withRel("create")));
		}

		model.add(linkTo(methodOn(ElementController.class).listElements(applicationId, stageId, 0, size, false))
			.withRel("first"));

		model.add(linkTo(methodOn(ElementController.class).listElements(applicationId, stageId, lastPage, size, false))
			.withRel("last"));

		if (page > 0) {
			model.add(linkTo(
					methodOn(ElementController.class).listElements(applicationId, stageId, page - 1, size, false))
				.withRel("prev"));
		}

		if (page < lastPage) {
			model.add(linkTo(
					methodOn(ElementController.class).listElements(applicationId, stageId, page + 1, size, false))
				.withRel("next"));
		}
	}

}
