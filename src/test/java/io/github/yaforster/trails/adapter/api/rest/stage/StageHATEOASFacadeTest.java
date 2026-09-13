package io.github.yaforster.trails.adapter.api.rest.stage;

import io.github.yaforster.trails.core.PagedResult;

import io.github.yaforster.trails.adapter.api.ResourceAuthorization;
import io.github.yaforster.trails.adapter.api.rest.deletion.DatabaseDeletionResultMapper;
import io.github.yaforster.trails.adapter.api.rest.deletion.DatabaseDeletionResultModelMapper;
import io.github.yaforster.trails.adapter.api.rest.model.*;
import io.github.yaforster.trails.adapter.api.rest.stage.assembler.PersistedStageContext;
import io.github.yaforster.trails.adapter.api.rest.stage.assembler.StageModelAssembler;
import io.github.yaforster.trails.adapter.api.rest.stage.mapper.PagedStageMapper;
import io.github.yaforster.trails.adapter.api.rest.stage.mapper.PersistedStageDTOMapper;
import io.github.yaforster.trails.adapter.api.rest.stage.mapper.StageDTOMapper;
import io.github.yaforster.trails.adapter.api.rest.stage.mapper.StageDefinitionDTOMapper;
import io.github.yaforster.trails.adapter.api.rest.stage.model.StageModel;
import io.github.yaforster.trails.app.services.ElementDatabaseService;
import io.github.yaforster.trails.core.definition.StageDefinition;
import io.github.yaforster.trails.core.deletion.DeletionNotFound;
import io.github.yaforster.trails.core.persisted.PersistedStage;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.web.PagedResourcesAssembler;
import org.springframework.hateoas.PagedModel;

import java.net.URI;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.same;
import static org.mockito.Mockito.*;

class StageHATEOASFacadeTest {

	private final StageDefinitionDTOMapper stageDefinitionDTOMapper = mock(StageDefinitionDTOMapper.class);

	private final PersistedStageDTOMapper persistedStageDTOMapper = mock(PersistedStageDTOMapper.class);

	private final StageDTOMapper stageMapper = mock(StageDTOMapper.class);

	private final StageModelAssembler assembler = mock(StageModelAssembler.class);

	private final PagedResourcesAssembler<PersistedStageContext> pagedAssembler = mock(PagedResourcesAssembler.class);

	private final ElementDatabaseService elementDatabaseService = mock(ElementDatabaseService.class);

	private final PagedStageMapper pagedStageMapper = mock(PagedStageMapper.class);

	private final ResourceAuthorization resourceAuthorization = mock(ResourceAuthorization.class);

	private final DatabaseDeletionResultMapper databaseDeletionResultMapper = new DatabaseDeletionResultMapper();

	private final DatabaseDeletionResultModelMapper databaseDeletionResultModelMapper = new DatabaseDeletionResultModelMapper();

	private final StageHATEOASFacade facade = new StageHATEOASFacade(stageDefinitionDTOMapper, persistedStageDTOMapper,
			stageMapper, assembler, pagedAssembler, elementDatabaseService, pagedStageMapper, resourceAuthorization,
			databaseDeletionResultMapper, databaseDeletionResultModelMapper);

	@Test
	void mapToStageDTO_shouldBuildContextAndDelegate() {
		PersistedStage persisted = new PersistedStage(9L, 1L, "stage", "https://example.org");
		StageModel model = new StageModel(9L, "stage", "https://example.org");
		StageDTO dto = new StageDTO().id(9L);
		when(assembler.toModel(any(PersistedStageContext.class))).thenReturn(model);
		when(stageMapper.toDTO(model)).thenReturn(dto);

		StageDTO result = facade.mapToStageDTO(1L, persisted, true);

		ArgumentCaptor<PersistedStageContext> contextCaptor = ArgumentCaptor.forClass(PersistedStageContext.class);
		verify(assembler).toModel(contextCaptor.capture());
		PersistedStageContext captured = contextCaptor.getValue();
		assertEquals(1L, captured.applicationID());
		assertSame(persisted, captured.persistedStage());
		assertTrue(captured.hasElements());
		assertSame(dto, result);
	}

	@Test
	void toDomain_shouldDelegateMapper() {
		StageDefinitionDTO dto = new StageDefinitionDTO().label("stage").url(URI.create("https://example.org"));
		StageDefinition definition = new StageDefinition("stage", "https://example.org", List.of());
		when(stageDefinitionDTOMapper.toDomain(dto)).thenReturn(definition);

		StageDefinition result = facade.toDomain(dto);

		assertSame(definition, result);
		verify(stageDefinitionDTOMapper).toDomain(dto);
	}

	@Test
	void toDTO_shouldDelegateMapper() {
		PersistedStage persisted = new PersistedStage(9L, 1L, "stage", "https://example.org");
		PersistedStageDTO dto = new PersistedStageDTO().id(9L);
		when(persistedStageDTOMapper.toDTO(persisted)).thenReturn(dto);

		PersistedStageDTO result = facade.toDTO(1L, persisted);

		assertSame(dto, result);
		verify(persistedStageDTOMapper).toDTO(persisted);
		assertTrue(result.getLinks().containsKey("testPlans"));
		assertTrue(result.getLinks().get("testPlans").getHref().contains("/applications/1/stages/9/testPlans"));
		assertTrue(result.getLinks().containsKey("testRuns"));
		assertTrue(result.getLinks().get("testRuns").getHref().contains("/applications/1/stages/9/testruns"));
	}

	@Test
	void toDTO_shouldMapDeletionResultAndAddCollectionLinkWhenStageIsMissing() {
		DatabaseDeletionResultDTO result = facade.toDTO(1L, new DeletionNotFound(9L));

		assertFalse(result.getDeletionResult());
		assertEquals(9L, result.getDeletionRequestedForID());
		assertTrue(result.getLinks().containsKey("collection"));
		assertTrue(result.getLinks().get("collection").getHref().contains("/applications/1/stages"));
	}

	@Test
	@SuppressWarnings("unchecked")
	void mapToPagedStageDTO_shouldMapContextsAndAddPagingLinks() {
		Long applicationId = 1L;
		PersistedStage first = new PersistedStage(9L, 101L, "first", "https://one.example");
		PersistedStage second = new PersistedStage(10L, 102L, "second", "https://two.example");
		PagedResult<PersistedStage> pagedStages = new PagedResult<>(List.of(first, second), 1, 20, 80);
		when(elementDatabaseService
			.findStageIdsWithElements(new ElementDatabaseService.CandidateStages(applicationId, List.of(9L, 10L))))
			.thenReturn(Set.of(9L));

		PagedModel<StageModel> model = PagedModel.of(List.of(new StageModel(9L, "first", "https://one.example")),
				new PagedModel.PageMetadata(20, 1, 80, 4));
		PagedStageDTO dto = new PagedStageDTO();
		when(pagedAssembler.toModel(any(Page.class), same(assembler))).thenReturn(model);
		when(pagedStageMapper.toPagedDto(any(PagedModel.class))).thenReturn(dto);

		PagedStageDTO result = facade.mapToPagedStageDTO(applicationId, pagedStages);

		ArgumentCaptor<Page<PersistedStageContext>> pageCaptor = (ArgumentCaptor<Page<PersistedStageContext>>) (ArgumentCaptor<?>) ArgumentCaptor
			.forClass(Page.class);
		verify(pagedAssembler).toModel(pageCaptor.capture(), same(assembler));
		assertEquals(2, pageCaptor.getValue().getContent().size());
		PersistedStageContext firstContext = pageCaptor.getValue().getContent().getFirst();
		PersistedStageContext secondContext = pageCaptor.getValue().getContent().get(1);
		assertEquals(applicationId, firstContext.applicationID());
		assertEquals(applicationId, secondContext.applicationID());
		assertSame(first, firstContext.persistedStage());
		assertSame(second, secondContext.persistedStage());
		assertTrue(firstContext.hasElements());
		assertFalse(secondContext.hasElements());
		verify(elementDatabaseService)
			.findStageIdsWithElements(new ElementDatabaseService.CandidateStages(applicationId, List.of(9L, 10L)));
		verify(elementDatabaseService, never()).hasElements(any());

		ArgumentCaptor<PagedModel<StageModel>> modelCaptor = (ArgumentCaptor<PagedModel<StageModel>>) (ArgumentCaptor<?>) ArgumentCaptor
			.forClass(PagedModel.class);
		verify(pagedStageMapper).toPagedDto(modelCaptor.capture());
		PagedModel<StageModel> capturedModel = modelCaptor.getValue();
		assertTrue(capturedModel.getLink("self").isPresent());
		assertTrue(capturedModel.getLink("first").isPresent());
		assertTrue(capturedModel.getLink("last").isPresent());
		assertTrue(capturedModel.getLink("prev").isPresent());
		assertTrue(capturedModel.getLink("next").isPresent());
		assertTrue(capturedModel.getRequiredLink("self").getHref().contains("/applications/1/stages"));
		assertTrue(capturedModel.getRequiredLink("self").getHref().contains("page=1"));
		assertTrue(capturedModel.getRequiredLink("self").getHref().contains("size=20"));
		assertSame(dto, result);
	}

	@Test
	@SuppressWarnings("unchecked")
	void mapToPagedStageDTO_shouldNotAddPrevAndNextForSinglePage() {
		Long applicationId = 1L;
		PersistedStage stage = new PersistedStage(9L, 101L, "only", "https://one.example");
		PagedResult<PersistedStage> pagedStages = new PagedResult<>(List.of(stage), 0, 20, 1);
		when(elementDatabaseService
			.findStageIdsWithElements(new ElementDatabaseService.CandidateStages(applicationId, List.of(9L))))
			.thenReturn(Set.of(9L));

		PagedModel<StageModel> model = PagedModel.of(List.of(new StageModel(9L, "only", "https://one.example")),
				new PagedModel.PageMetadata(20, 0, 1, 1));
		when(pagedAssembler.toModel(any(Page.class), same(assembler))).thenReturn(model);
		when(pagedStageMapper.toPagedDto(any(PagedModel.class))).thenReturn(new PagedStageDTO());

		facade.mapToPagedStageDTO(applicationId, pagedStages);

		ArgumentCaptor<PagedModel<StageModel>> modelCaptor = (ArgumentCaptor<PagedModel<StageModel>>) (ArgumentCaptor<?>) ArgumentCaptor
			.forClass(PagedModel.class);
		verify(pagedStageMapper).toPagedDto(modelCaptor.capture());
		PagedModel<StageModel> captured = modelCaptor.getValue();
		assertFalse(captured.getLink("prev").isPresent());
		assertFalse(captured.getLink("next").isPresent());
		assertTrue(captured.getRequiredLink("last").getHref().contains("page=0"));
	}

}
