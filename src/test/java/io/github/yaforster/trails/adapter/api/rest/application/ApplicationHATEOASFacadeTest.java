package io.github.yaforster.trails.adapter.api.rest.application;

import io.github.yaforster.trails.core.PagedResult;

import io.github.yaforster.trails.adapter.api.ResourceAuthorization;
import io.github.yaforster.trails.adapter.api.rest.deletion.DatabaseDeletionResultMapper;
import io.github.yaforster.trails.adapter.api.rest.deletion.DatabaseDeletionResultModelMapper;
import io.github.yaforster.trails.adapter.api.rest.application.assembler.ApplicationModelAssembler;
import io.github.yaforster.trails.adapter.api.rest.application.assembler.PersistedApplicationContext;
import io.github.yaforster.trails.adapter.api.rest.application.mapper.ApplicationDTOMapper;
import io.github.yaforster.trails.adapter.api.rest.application.mapper.ApplicationDefinitionDTOMapper;
import io.github.yaforster.trails.adapter.api.rest.application.mapper.PagedApplicationMapper;
import io.github.yaforster.trails.adapter.api.rest.application.mapper.PersistedApplicationDTOMapper;
import io.github.yaforster.trails.adapter.api.rest.application.model.ApplicationModel;
import io.github.yaforster.trails.adapter.api.rest.model.*;
import io.github.yaforster.trails.app.services.StageDatabaseService;
import io.github.yaforster.trails.core.definition.ApplicationDefinition;
import io.github.yaforster.trails.core.deletion.DeletionNotFound;
import io.github.yaforster.trails.core.persisted.PersistedApplication;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.web.PagedResourcesAssembler;
import org.springframework.hateoas.PagedModel;

import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.same;
import static org.mockito.Mockito.*;

class ApplicationHATEOASFacadeTest {

	private final ApplicationDefinitionDTOMapper definitionDTOMapper = mock(ApplicationDefinitionDTOMapper.class);

	private final PersistedApplicationDTOMapper persistedApplicationDTOMapper = mock(
			PersistedApplicationDTOMapper.class);

	private final ApplicationDTOMapper applicationMapper = mock(ApplicationDTOMapper.class);

	private final ApplicationModelAssembler assembler = mock(ApplicationModelAssembler.class);

	private final PagedResourcesAssembler<PersistedApplicationContext> pagedAssembler = mock(
			PagedResourcesAssembler.class);

	private final StageDatabaseService stageDatabaseService = mock(StageDatabaseService.class);

	private final PagedApplicationMapper pagedApplicationMapper = mock(PagedApplicationMapper.class);

	private final ResourceAuthorization resourceAuthorization = mock(ResourceAuthorization.class);

	private final DatabaseDeletionResultMapper databaseDeletionResultMapper = new DatabaseDeletionResultMapper();

	private final DatabaseDeletionResultModelMapper databaseDeletionResultModelMapper = new DatabaseDeletionResultModelMapper();

	private final ApplicationHATEOASFacade facade = new ApplicationHATEOASFacade(definitionDTOMapper,
			persistedApplicationDTOMapper, applicationMapper, assembler, pagedAssembler, stageDatabaseService,
			pagedApplicationMapper, resourceAuthorization, databaseDeletionResultMapper,
			databaseDeletionResultModelMapper);

	@Test
	void toDomain_shouldDelegateMapper() {
		ApplicationDefinitionDTO dto = new ApplicationDefinitionDTO().label("app");
		ApplicationDefinition definition = new ApplicationDefinition("app");
		when(definitionDTOMapper.toDomain(dto)).thenReturn(definition);

		ApplicationDefinition result = facade.toDomain(dto);

		assertSame(definition, result);
		verify(definitionDTOMapper).toDomain(dto);
	}

	@Test
	void toDTO_shouldDelegateMapper() {
		PersistedApplication persisted = new PersistedApplication(9L, "app");
		PersistedApplicationDTO dto = new PersistedApplicationDTO().id(9L);
		when(persistedApplicationDTOMapper.toDTO(persisted)).thenReturn(dto);

		PersistedApplicationDTO result = facade.toDTO(persisted);

		assertSame(dto, result);
		verify(persistedApplicationDTOMapper).toDTO(persisted);
	}

	@Test
	void toDTO_shouldMapDeletionResultAndAddCollectionLinkWhenApplicationIsMissing() {
		DatabaseDeletionResultDTO result = facade.toDTO(new DeletionNotFound(9L));

		assertFalse(result.getDeletionResult());
		assertEquals(9L, result.getDeletionRequestedForID());
		assertTrue(result.getLinks().get("collection").getHref().contains("/applications"));
	}

	@Test
	void mapToApplicationDTO_shouldBuildContextAndDelegate() {
		PersistedApplication persisted = new PersistedApplication(9L, "app");
		ApplicationModel model = new ApplicationModel(9L, "app");
		ApplicationDTO dto = new ApplicationDTO().id(9L);
		when(stageDatabaseService.hasStages(new StageDatabaseService.ApplicationReference(9L))).thenReturn(true);
		when(assembler.toModel(any(PersistedApplicationContext.class))).thenReturn(model);
		when(applicationMapper.toDTO(model)).thenReturn(dto);

		ApplicationDTO result = facade.mapToApplicationDTO(persisted);

		ArgumentCaptor<PersistedApplicationContext> contextCaptor = ArgumentCaptor
			.forClass(PersistedApplicationContext.class);
		verify(assembler).toModel(contextCaptor.capture());
		PersistedApplicationContext context = contextCaptor.getValue();
		assertSame(persisted, context.persistedApplication());
		assertTrue(context.hasStages());
		verify(stageDatabaseService).hasStages(new StageDatabaseService.ApplicationReference(9L));
		assertSame(dto, result);
	}

	@Test
	@SuppressWarnings("unchecked")
	void mapToPagedApplicationDTO_shouldMapContextsAndAddPagingLinks() {
		PersistedApplication first = new PersistedApplication(9L, "first");
		PersistedApplication second = new PersistedApplication(10L, "second");
		PagedResult<PersistedApplication> paged = new PagedResult<>(List.of(first, second), 1, 20, 80);
		when(stageDatabaseService
			.findApplicationIdsWithStages(new StageDatabaseService.ApplicationsWithStages(List.of(9L, 10L))))
			.thenReturn(Set.of(9L));

		PagedModel<ApplicationModel> model = PagedModel.of(List.of(new ApplicationModel(9L, "first")),
				new PagedModel.PageMetadata(20, 1, 80, 4));
		PagedApplicationDTO dto = new PagedApplicationDTO();
		when(pagedAssembler.toModel(any(Page.class), same(assembler))).thenReturn(model);
		when(pagedApplicationMapper.toPagedDto(any(PagedModel.class))).thenReturn(dto);

		PagedApplicationDTO result = facade.mapToPagedApplicationDTO(paged);

		ArgumentCaptor<Page<PersistedApplicationContext>> pageCaptor = (ArgumentCaptor<Page<PersistedApplicationContext>>) (ArgumentCaptor<?>) ArgumentCaptor
			.forClass(Page.class);
		verify(pagedAssembler).toModel(pageCaptor.capture(), same(assembler));
		List<PersistedApplicationContext> contexts = pageCaptor.getValue().getContent();
		assertEquals(2, contexts.size());
		assertSame(first, contexts.getFirst().persistedApplication());
		assertSame(second, contexts.get(1).persistedApplication());
		assertTrue(contexts.getFirst().hasStages());
		assertFalse(contexts.get(1).hasStages());
		verify(stageDatabaseService)
			.findApplicationIdsWithStages(new StageDatabaseService.ApplicationsWithStages(List.of(9L, 10L)));
		verify(stageDatabaseService, never()).hasStages(any());

		ArgumentCaptor<PagedModel<ApplicationModel>> modelCaptor = (ArgumentCaptor<PagedModel<ApplicationModel>>) (ArgumentCaptor<?>) ArgumentCaptor
			.forClass(PagedModel.class);
		verify(pagedApplicationMapper).toPagedDto(modelCaptor.capture());
		PagedModel<ApplicationModel> captured = modelCaptor.getValue();
		assertTrue(captured.getLink("self").isPresent());
		assertTrue(captured.getLink("first").isPresent());
		assertTrue(captured.getLink("last").isPresent());
		assertTrue(captured.getLink("prev").isPresent());
		assertTrue(captured.getLink("next").isPresent());
		assertTrue(captured.getRequiredLink("self").getHref().contains("/applications"));
		assertTrue(captured.getRequiredLink("self").getHref().contains("page=1"));
		assertTrue(captured.getRequiredLink("self").getHref().contains("size=20"));
		assertSame(dto, result);
	}

	@Test
	@SuppressWarnings("unchecked")
	void mapToPagedApplicationDTO_shouldOmitPrevAndNextForSinglePage() {
		PersistedApplication persisted = new PersistedApplication(9L, "only");
		PagedResult<PersistedApplication> paged = new PagedResult<>(List.of(persisted), 0, 20, 1);
		when(stageDatabaseService
			.findApplicationIdsWithStages(new StageDatabaseService.ApplicationsWithStages(List.of(9L))))
			.thenReturn(Set.of(9L));

		PagedModel<ApplicationModel> model = PagedModel.of(List.of(new ApplicationModel(9L, "only")),
				new PagedModel.PageMetadata(20, 0, 1, 1));
		when(pagedAssembler.toModel(any(Page.class), same(assembler))).thenReturn(model);
		when(pagedApplicationMapper.toPagedDto(any(PagedModel.class))).thenReturn(new PagedApplicationDTO());

		facade.mapToPagedApplicationDTO(paged);

		ArgumentCaptor<PagedModel<ApplicationModel>> modelCaptor = (ArgumentCaptor<PagedModel<ApplicationModel>>) (ArgumentCaptor<?>) ArgumentCaptor
			.forClass(PagedModel.class);
		verify(pagedApplicationMapper).toPagedDto(modelCaptor.capture());
		PagedModel<ApplicationModel> captured = modelCaptor.getValue();
		assertFalse(captured.getLink("prev").isPresent());
		assertFalse(captured.getLink("next").isPresent());
		assertTrue(captured.getRequiredLink("last").getHref().contains("page=0"));
	}

}
