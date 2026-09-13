package io.github.yaforster.trails.adapter.api.rest.stage;

import io.github.yaforster.trails.adapter.api.ResourceAuthorization;
import io.github.yaforster.trails.adapter.api.rest.deletion.DatabaseDeletionResultMapper;
import io.github.yaforster.trails.adapter.api.rest.deletion.DatabaseDeletionResultModelMapper;
import io.github.yaforster.trails.adapter.api.rest.hateoas.HATEOASFacade;
import io.github.yaforster.trails.adapter.api.rest.model.*;
import io.github.yaforster.trails.adapter.api.rest.stage.assembler.PersistedStageContext;
import io.github.yaforster.trails.adapter.api.rest.stage.assembler.StageModelAssembler;
import io.github.yaforster.trails.adapter.api.rest.stage.mapper.PagedStageMapper;
import io.github.yaforster.trails.adapter.api.rest.stage.mapper.PersistedStageDTOMapper;
import io.github.yaforster.trails.adapter.api.rest.stage.mapper.StageDTOMapper;
import io.github.yaforster.trails.adapter.api.rest.stage.mapper.StageDefinitionDTOMapper;
import io.github.yaforster.trails.adapter.api.rest.stage.model.StageModel;
import io.github.yaforster.trails.adapter.api.rest.common.PagedResultPageAdapter;
import io.github.yaforster.trails.adapter.api.rest.testplan.TestPlanController;
import io.github.yaforster.trails.adapter.api.rest.testrun.TestRunController;
import io.github.yaforster.trails.app.services.ElementDatabaseService;
import io.github.yaforster.trails.core.definition.StageDefinition;
import io.github.yaforster.trails.core.deletion.DatabaseDeletionResult;
import io.github.yaforster.trails.core.PagedResult;
import io.github.yaforster.trails.core.persisted.PersistedStage;
import org.springframework.data.domain.Page;
import org.springframework.data.web.PagedResourcesAssembler;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.PagedModel;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import static io.github.yaforster.trails.adapter.api.rest.hateoas.HATEOASLinks.put;
import static org.springframework.hateoas.server.core.DummyInvocationUtils.methodOn;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;

/**
 * API composition facade for stage resources. It intentionally coordinates mapping, link
 * generation and paging assembly in one orchestration point so stage-related HATEOAS
 * responses are produced consistently across endpoints.
 */
@Component
@SuppressWarnings("OverlyCoupledClass")
public class StageHATEOASFacade extends HATEOASFacade {

	private final StageDefinitionDTOMapper stageDefinitionDTOMapper;

	private final PersistedStageDTOMapper persistedStageDTOMapper;

	private final StageDTOMapper stageMapper;

	private final StageModelAssembler assembler;

	private final PagedResourcesAssembler<PersistedStageContext> pagedAssembler;

	private final ElementDatabaseService elementDatabaseService;

	private final PagedStageMapper pagedStageMapper;

	private final ResourceAuthorization resourceAuthorization;

	public StageHATEOASFacade(StageDefinitionDTOMapper stageDefinitionDTOMapper,
			PersistedStageDTOMapper persistedStageDTOMapper, StageDTOMapper stageMapper, StageModelAssembler assembler,
			PagedResourcesAssembler<PersistedStageContext> pagedAssembler,
			ElementDatabaseService elementDatabaseService, PagedStageMapper pagedStageMapper,
			ResourceAuthorization resourceAuthorization, DatabaseDeletionResultMapper databaseDeletionResultMapper,
			DatabaseDeletionResultModelMapper databaseDeletionResultModelMapper) {
		super(databaseDeletionResultMapper, databaseDeletionResultModelMapper);
		this.stageDefinitionDTOMapper = stageDefinitionDTOMapper;
		this.persistedStageDTOMapper = persistedStageDTOMapper;
		this.stageMapper = stageMapper;
		this.assembler = assembler;
		this.pagedAssembler = pagedAssembler;
		this.elementDatabaseService = elementDatabaseService;
		this.pagedStageMapper = pagedStageMapper;
		this.resourceAuthorization = resourceAuthorization;
	}

	public StageDTO mapToStageDTO(Long applicationID, PersistedStage persistedStage, boolean hasElements) {
		PersistedStageContext stageContext = new PersistedStageContext(applicationID, persistedStage, hasElements);
		StageModel model = assembler.toModel(stageContext);
		return stageMapper.toDTO(model);
	}

	protected StageDefinition toDomain(StageDefinitionDTO dto) {
		return stageDefinitionDTOMapper.toDomain(dto);
	}

	public PersistedStageDTO toDTO(Long applicationId, PersistedStage persistedStage) {
		PersistedStageDTO dto = persistedStageDTOMapper.toDTO(persistedStage);
		Link testPlansLink = linkTo(
				methodOn(TestPlanController.class).listTestPlans(applicationId, persistedStage.id(), 0, 20, false))
			.withRel("testPlans");
		Link testRunsLink = linkTo(
				methodOn(TestRunController.class).listTestRuns(applicationId, persistedStage.id(), 0, 20))
			.withRel("testRuns");
		Map<String, LinkDTO> links = new HashMap<>();
		links.put("testPlans", toDTO(testPlansLink));
		links.put("testRuns", toDTO(testRunsLink));
		dto.links(links);
		return dto;
	}

	public DatabaseDeletionResultDTO toDTO(Long applicationId, DatabaseDeletionResult deletionResult) {
		return toDeletionDTO(deletionResult, deletionNotFoundModel -> deletionNotFoundModel.add(
				linkTo(methodOn(StageController.class).listStages(applicationId, 0, 20, false)).withRel("collection")));
	}

	public PagedStageDTO mapToPagedStageDTO(Long applicationID, PagedResult<PersistedStage> pagedStages) {
		Set<Long> stageIdsWithElements = elementDatabaseService
			.findStageIdsWithElements(new ElementDatabaseService.CandidateStages(applicationID,
					pagedStages.items().stream().map(PersistedStage::id).toList()));
		Page<PersistedStageContext> contexts = PagedResultPageAdapter.toSpringPage(pagedStages.map(stage -> {
			boolean hasElements = stageIdsWithElements.contains(stage.id());
			return new PersistedStageContext(applicationID, stage, hasElements);
		}));
		PagedModel<StageModel> model = pagedAssembler.toModel(contexts, assembler);
		addPagingLinks(model, applicationID, pagedStages.page(), pagedStages.size(), pagedStages.totalPages());
		return pagedStageMapper.toPagedDto(model);
	}

	private void addPagingLinks(PagedModel<?> model, Long applicationID, int page, int size, int totalPages) {
		int lastPage = Math.max(totalPages - 1, 0);

		model.add(linkTo(methodOn(StageController.class).listStages(applicationID, page, size, false)).withSelfRel());

		if (resourceAuthorization.canManageResources()) {
			model.add(
					put(linkTo(methodOn(StageController.class).createNewStage(applicationID, null)).withRel("create")));
		}

		model.add(linkTo(methodOn(StageController.class).listStages(applicationID, 0, size, false)).withRel("first"));

		model.add(linkTo(methodOn(StageController.class).listStages(applicationID, lastPage, size, false))
			.withRel("last"));

		if (page > 0) {
			model.add(linkTo(methodOn(StageController.class).listStages(applicationID, page - 1, size, false))
				.withRel("prev"));
		}

		if (page < lastPage) {
			model.add(linkTo(methodOn(StageController.class).listStages(applicationID, page + 1, size, false))
				.withRel("next"));
		}
	}

}
