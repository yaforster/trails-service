package io.github.yaforster.trails.adapter.api.rest.application;

import io.github.yaforster.trails.adapter.api.ResourceAuthorization;
import io.github.yaforster.trails.adapter.api.rest.application.assembler.ApplicationModelAssembler;
import io.github.yaforster.trails.adapter.api.rest.application.assembler.PersistedApplicationContext;
import io.github.yaforster.trails.adapter.api.rest.application.mapper.ApplicationDTOMapper;
import io.github.yaforster.trails.adapter.api.rest.application.mapper.ApplicationDefinitionDTOMapper;
import io.github.yaforster.trails.adapter.api.rest.application.mapper.PagedApplicationMapper;
import io.github.yaforster.trails.adapter.api.rest.application.mapper.PersistedApplicationDTOMapper;
import io.github.yaforster.trails.adapter.api.rest.application.model.ApplicationModel;
import io.github.yaforster.trails.adapter.api.rest.common.PagedResultPageAdapter;
import io.github.yaforster.trails.adapter.api.rest.deletion.DatabaseDeletionResultMapper;
import io.github.yaforster.trails.adapter.api.rest.deletion.DatabaseDeletionResultModelMapper;
import io.github.yaforster.trails.adapter.api.rest.hateoas.HATEOASFacade;
import io.github.yaforster.trails.adapter.api.rest.model.*;
import io.github.yaforster.trails.app.services.StageDatabaseService;
import io.github.yaforster.trails.core.definition.ApplicationDefinition;
import io.github.yaforster.trails.core.PagedResult;
import io.github.yaforster.trails.core.deletion.DatabaseDeletionResult;
import io.github.yaforster.trails.core.persisted.PersistedApplication;
import org.springframework.data.domain.Page;
import org.springframework.data.web.PagedResourcesAssembler;
import org.springframework.hateoas.PagedModel;
import org.springframework.stereotype.Component;

import java.util.Set;

import static io.github.yaforster.trails.adapter.api.rest.hateoas.HATEOASLinks.put;
import static org.springframework.hateoas.server.core.DummyInvocationUtils.methodOn;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;

@Component
@SuppressWarnings("OverlyCoupledClass")
public class ApplicationHATEOASFacade extends HATEOASFacade {

	private final ApplicationDefinitionDTOMapper definitionDTOMapper;

	private final PersistedApplicationDTOMapper persistedApplicationDTOMapper;

	private final ApplicationDTOMapper applicationMapper;

	private final ApplicationModelAssembler assembler;

	private final PagedResourcesAssembler<PersistedApplicationContext> pagedAssembler;

	private final StageDatabaseService stageDatabaseService;

	private final PagedApplicationMapper pagedApplicationMapper;

	private final ResourceAuthorization resourceAuthorization;

	public ApplicationHATEOASFacade(ApplicationDefinitionDTOMapper definitionDTOMapper,
			PersistedApplicationDTOMapper persistedApplicationDTOMapper, ApplicationDTOMapper applicationMapper,
			ApplicationModelAssembler assembler, PagedResourcesAssembler<PersistedApplicationContext> pagedAssembler,
			StageDatabaseService stageDatabaseService, PagedApplicationMapper pagedApplicationMapper,
			ResourceAuthorization resourceAuthorization, DatabaseDeletionResultMapper databaseDeletionResultMapper,
			DatabaseDeletionResultModelMapper databaseDeletionResultModelMapper) {
		super(databaseDeletionResultMapper, databaseDeletionResultModelMapper);
		this.definitionDTOMapper = definitionDTOMapper;
		this.persistedApplicationDTOMapper = persistedApplicationDTOMapper;
		this.applicationMapper = applicationMapper;
		this.assembler = assembler;
		this.pagedAssembler = pagedAssembler;
		this.stageDatabaseService = stageDatabaseService;
		this.pagedApplicationMapper = pagedApplicationMapper;
		this.resourceAuthorization = resourceAuthorization;
	}

	protected ApplicationDTO mapToApplicationDTO(PersistedApplication application) {
		boolean hasStages = stageDatabaseService
			.hasStages(new StageDatabaseService.ApplicationReference(application.id()));
		PersistedApplicationContext context = new PersistedApplicationContext(application, hasStages);
		ApplicationModel model = assembler.toModel(context);
		return applicationMapper.toDTO(model);
	}

	protected PagedApplicationDTO mapToPagedApplicationDTO(PagedResult<PersistedApplication> pagedApplications) {
		Set<Long> applicationIdsWithStages = stageDatabaseService
			.findApplicationIdsWithStages(new StageDatabaseService.ApplicationsWithStages(
					pagedApplications.items().stream().map(PersistedApplication::id).toList()));
		Page<PersistedApplicationContext> pagedContexts = PagedResultPageAdapter
			.toSpringPage(pagedApplications.map(application -> {
				boolean hasStages = applicationIdsWithStages.contains(application.id());
				return new PersistedApplicationContext(application, hasStages);
			}));
		PagedModel<ApplicationModel> model = pagedAssembler.toModel(pagedContexts, assembler);
		addPagingLinks(model, pagedApplications.page(), pagedApplications.size(), pagedApplications.totalPages());
		return pagedApplicationMapper.toPagedDto(model);
	}

	protected PersistedApplicationDTO toDTO(PersistedApplication persistedApplication) {
		return persistedApplicationDTOMapper.toDTO(persistedApplication);
	}

	protected ApplicationDefinition toDomain(ApplicationDefinitionDTO dto) {
		return definitionDTOMapper.toDomain(dto);
	}

	protected DatabaseDeletionResultDTO toDTO(DatabaseDeletionResult deletionResult) {
		return toDeletionDTO(deletionResult, deletionNotFoundModel -> deletionNotFoundModel
			.add(linkTo(methodOn(ApplicationController.class).listApplications(0, 20, false)).withRel("collection")));
	}

	private void addPagingLinks(PagedModel<?> model, int page, int size, int totalPages) {
		int lastPage = Math.max(totalPages - 1, 0);

		model.add(linkTo(methodOn(ApplicationController.class).listApplications(page, size, false)).withSelfRel());

		if (resourceAuthorization.canManageResources()) {
			model.add(put(linkTo(methodOn(ApplicationController.class).createNewApplication(null)).withRel("create")));
		}

		model.add(linkTo(methodOn(ApplicationController.class).listApplications(0, size, false)).withRel("first"));

		model
			.add(linkTo(methodOn(ApplicationController.class).listApplications(lastPage, size, false)).withRel("last"));

		if (page > 0) {
			model.add(linkTo(methodOn(ApplicationController.class).listApplications(page - 1, size, false))
				.withRel("prev"));
		}

		if (page < lastPage) {
			model.add(linkTo(methodOn(ApplicationController.class).listApplications(page + 1, size, false))
				.withRel("next"));
		}
	}

}
