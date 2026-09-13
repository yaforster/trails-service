package io.github.yaforster.trails.adapter.api.rest.element;

import io.github.yaforster.trails.core.PagedResult;

import io.github.yaforster.trails.adapter.api.ResourceAuthorization;
import io.github.yaforster.trails.adapter.api.rest.common.PagedResultPageAdapter;
import io.github.yaforster.trails.adapter.api.rest.deletion.DatabaseDeletionResultMapper;
import io.github.yaforster.trails.adapter.api.rest.deletion.DatabaseDeletionResultModelMapper;
import io.github.yaforster.trails.adapter.api.rest.element.assembler.ElementModelAssembler;
import io.github.yaforster.trails.adapter.api.rest.element.assembler.PersistedElementContext;
import io.github.yaforster.trails.adapter.api.rest.element.mapper.*;
import io.github.yaforster.trails.adapter.api.rest.element.model.ElementModel;
import io.github.yaforster.trails.adapter.api.rest.model.*;
import io.github.yaforster.trails.core.TrailsTest;
import io.github.yaforster.trails.core.data.ElementType;
import io.github.yaforster.trails.core.data.TrailsScreenshotFile;
import io.github.yaforster.trails.core.definition.ElementDefinition;
import io.github.yaforster.trails.core.deletion.DeletionNotFound;
import io.github.yaforster.trails.core.persisted.PersistedElement;
import io.github.yaforster.trails.core.test.action.element.LocatorType;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.data.domain.Page;
import org.springframework.data.web.PagedResourcesAssembler;
import org.springframework.hateoas.PagedModel;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.same;
import static org.mockito.Mockito.*;

class ElementHATEOASFacadeTest extends TrailsTest {

	private final ElementDefinitionDTOMapper elementDefinitionDTOMapper = mock(ElementDefinitionDTOMapper.class);

	private final PersistedElementDTOMapper persistedElementDTOMapper = mock(PersistedElementDTOMapper.class);

	private final ElementDTOMapper elementMapper = mock(ElementDTOMapper.class);

	private final ElementModelAssembler assembler = mock(ElementModelAssembler.class);

	private final PagedResourcesAssembler<PersistedElementContext> pagedAssembler = mock(PagedResourcesAssembler.class);

	private final PagedElementMapper pagedElementMapper = mock(PagedElementMapper.class);

	private final ScreenshotArtifactDTOMapper screenshotArtifactDTOMapper = mock(ScreenshotArtifactDTOMapper.class);

	private final ResourceAuthorization resourceAuthorization = mock(ResourceAuthorization.class);

	private final DatabaseDeletionResultMapper databaseDeletionResultMapper = new DatabaseDeletionResultMapper();

	private final DatabaseDeletionResultModelMapper databaseDeletionResultModelMapper = new DatabaseDeletionResultModelMapper();

	private final ElementHATEOASFacade facade = new ElementHATEOASFacade(elementDefinitionDTOMapper,
			persistedElementDTOMapper, elementMapper, assembler, pagedAssembler, pagedElementMapper,
			screenshotArtifactDTOMapper, resourceAuthorization, databaseDeletionResultMapper,
			databaseDeletionResultModelMapper);

	@Test
	void toDomain_shouldDelegateMapper() {
		ElementDefinitionDTO dto = new ElementDefinitionDTO().label("element")
			.locatorString("#x")
			.locatorType(io.github.yaforster.trails.adapter.api.rest.model.LocatorTypeDTO.CSS)
			.type(io.github.yaforster.trails.adapter.api.rest.model.ElementTypeDTO.BUTTON);
		ElementDefinition definition = new ElementDefinition(ElementType.BUTTON, "element", "#x", LocatorType.CSS);
		when(elementDefinitionDTOMapper.toDomain(dto)).thenReturn(definition);

		ElementDefinition result = facade.toDomain(dto);

		assertSame(definition, result);
		verify(elementDefinitionDTOMapper).toDomain(dto);
	}

	@Test
	void toDTO_shouldDelegateMapper() {
		PersistedElement persisted = new PersistedElement(9L, "element", LocatorType.CSS, 1L, 2L, "#x",
				ElementType.BUTTON);
		PersistedElementDTO dto = new PersistedElementDTO().id(9L);
		when(persistedElementDTOMapper.toDTO(persisted)).thenReturn(dto);

		PersistedElementDTO result = facade.toDTO(persisted);

		assertSame(dto, result);
		verify(persistedElementDTOMapper).toDTO(persisted);
	}

	@Test
	void toDTO_shouldDelegateScreenshotArtifactDTOMapper() {
		TrailsScreenshotFile screenshotFile = screenshotFile("element.jpg", jpegBytes());
		ArtifactDTO dto = new ArtifactDTO().id(42L).type(ArtifactDTO.TypeEnum.SCREENSHOT).filename("element.jpg");
		when(screenshotArtifactDTOMapper.toDTO(1L, 2L, 9L, 42L, screenshotFile)).thenReturn(dto);

		ArtifactDTO result = facade.toDTO(1L, 2L, 9L, 42L, screenshotFile);

		assertSame(dto, result);
		verify(screenshotArtifactDTOMapper).toDTO(1L, 2L, 9L, 42L, screenshotFile);
	}

	@Test
	void mapToElementDTO_shouldDelegateAssemblerAndMapper() {
		PersistedElement persisted = new PersistedElement(9L, "element", LocatorType.CSS, 1L, 2L, "#x",
				ElementType.BUTTON);
		PersistedElementContext context = new PersistedElementContext(1L, 2L, persisted, Optional.of(99L));
		ElementModel model = new ElementModel(9L, ElementType.BUTTON, "element", "#x", LocatorType.CSS);
		ElementDTO dto = new ElementDTO().id(9L);
		when(assembler.toModel(context)).thenReturn(model);
		when(elementMapper.toDTO(model)).thenReturn(dto);

		ElementDTO result = facade.mapToElementDTO(context);

		assertSame(dto, result);
		verify(assembler).toModel(context);
		verify(elementMapper).toDTO(model);
	}

	@Test
	@SuppressWarnings("unchecked")
	void mapContextsToPagedElementDTO_shouldAddPagingLinks() {
		PersistedElement first = new PersistedElement(9L, "first", LocatorType.CSS, 1L, 2L, "#one", ElementType.BUTTON);
		PersistedElement second = new PersistedElement(10L, "second", LocatorType.XPATH, 1L, 2L, "//two",
				ElementType.INPUT);
		PagedResult<PersistedElementContext> contexts = new PagedResult<>(
				List.of(new PersistedElementContext(1L, 2L, first, Optional.of(501L)),
						new PersistedElementContext(1L, 2L, second, Optional.empty())),
				1, 20, 80);

		PagedModel<ElementModel> model = PagedModel.of(
				List.of(new ElementModel(9L, ElementType.BUTTON, "first", "#one", LocatorType.CSS)),
				new PagedModel.PageMetadata(20, 1, 80, 4));
		PagedElementDTO dto = new PagedElementDTO();
		when(pagedAssembler.toModel(any(Page.class), same(assembler))).thenReturn(model);
		when(pagedElementMapper.toPagedDto(any(PagedModel.class))).thenReturn(dto);

		PagedElementDTO result = facade.mapContextsToPagedElementDTO(1L, 2L, contexts);

		verify(pagedAssembler).toModel(eq(PagedResultPageAdapter.toSpringPage(contexts)), same(assembler));
		ArgumentCaptor<PagedModel<ElementModel>> modelCaptor = (ArgumentCaptor<PagedModel<ElementModel>>) (ArgumentCaptor<?>) ArgumentCaptor
			.forClass(PagedModel.class);
		verify(pagedElementMapper).toPagedDto(modelCaptor.capture());
		PagedModel<ElementModel> captured = modelCaptor.getValue();
		assertTrue(captured.getLink("self").isPresent());
		assertTrue(captured.getLink("first").isPresent());
		assertTrue(captured.getLink("last").isPresent());
		assertTrue(captured.getLink("prev").isPresent());
		assertTrue(captured.getLink("next").isPresent());
		assertTrue(captured.getRequiredLink("self").getHref().contains("/applications/1/stages/2/elements"));
		assertTrue(captured.getRequiredLink("self").getHref().contains("page=1"));
		assertTrue(captured.getRequiredLink("self").getHref().contains("size=20"));
		assertSame(dto, result);
	}

	@Test
	void toDTO_shouldMapDeletionResultAndAddCollectionLinkWhenElementIsMissing() {
		DatabaseDeletionResultDTO result = facade.toDTO(1L, 2L, new DeletionNotFound(9L));

		assertFalse(result.getDeletionResult());
		assertEquals(9L, result.getDeletionRequestedForID());
		assertTrue(result.getLinks().containsKey("collection"));
		assertTrue(result.getLinks().get("collection").getHref().contains("/applications/1/stages/2/elements"));
	}

	@Test
	@SuppressWarnings("unchecked")
	void mapContextsToPagedElementDTO_shouldOmitPrevAndNextForSinglePage() {
		PersistedElement persisted = new PersistedElement(9L, "only", LocatorType.CSS, 1L, 2L, "#one",
				ElementType.BUTTON);
		PagedResult<PersistedElementContext> contexts = new PagedResult<>(
				List.of(new PersistedElementContext(1L, 2L, persisted, Optional.of(501L))), 0, 20, 1);
		PagedModel<ElementModel> model = PagedModel.of(
				List.of(new ElementModel(9L, ElementType.BUTTON, "only", "#one", LocatorType.CSS)),
				new PagedModel.PageMetadata(20, 0, 1, 1));
		when(pagedAssembler.toModel(any(Page.class), same(assembler))).thenReturn(model);
		when(pagedElementMapper.toPagedDto(any(PagedModel.class))).thenReturn(new PagedElementDTO());

		facade.mapContextsToPagedElementDTO(1L, 2L, contexts);

		ArgumentCaptor<PagedModel<ElementModel>> modelCaptor = (ArgumentCaptor<PagedModel<ElementModel>>) (ArgumentCaptor<?>) ArgumentCaptor
			.forClass(PagedModel.class);
		verify(pagedElementMapper).toPagedDto(modelCaptor.capture());
		PagedModel<ElementModel> captured = modelCaptor.getValue();
		assertFalse(captured.getLink("prev").isPresent());
		assertFalse(captured.getLink("next").isPresent());
		assertTrue(captured.getRequiredLink("last").getHref().contains("page=0"));
	}

}
