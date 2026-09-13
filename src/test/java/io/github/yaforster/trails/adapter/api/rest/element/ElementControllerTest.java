package io.github.yaforster.trails.adapter.api.rest.element;

import io.github.yaforster.trails.core.PagedResult;
import io.github.yaforster.trails.core.BinaryContent;

import io.github.yaforster.trails.adapter.api.rest.element.assembler.PersistedElementContext;
import io.github.yaforster.trails.adapter.api.rest.common.mapper.ScreenshotFileRequestMapper;
import io.github.yaforster.trails.adapter.api.rest.model.*;
import io.github.yaforster.trails.app.services.ElementDatabaseService;
import io.github.yaforster.trails.app.services.ScreenshotDatabaseService;
import io.github.yaforster.trails.app.services.StageDatabaseService;
import io.github.yaforster.trails.core.TrailsTest;
import io.github.yaforster.trails.core.data.ElementType;
import io.github.yaforster.trails.core.data.Screenshot;
import io.github.yaforster.trails.core.data.TrailsScreenshotFile;
import io.github.yaforster.trails.core.definition.ElementDefinition;
import io.github.yaforster.trails.core.deletion.DatabaseDeletionResult;
import io.github.yaforster.trails.core.deletion.DeletionFailure;
import io.github.yaforster.trails.core.deletion.DeletionSuccess;
import io.github.yaforster.trails.core.persisted.PersistedElement;
import io.github.yaforster.trails.core.persisted.PersistedStage;
import io.github.yaforster.trails.core.test.action.element.LocatorType;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockMultipartFile;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ElementControllerTest extends TrailsTest {

	private final ElementDatabaseService elementDatabaseService = mock(ElementDatabaseService.class);

	private final ScreenshotDatabaseService screenshotDatabaseService = mock(ScreenshotDatabaseService.class);

	private final StageDatabaseService stageDatabaseService = mock(StageDatabaseService.class);

	private final ElementHATEOASFacade facade = mock(ElementHATEOASFacade.class);

	private final ScreenshotFileRequestMapper screenshotFileRequestMapper = mock(ScreenshotFileRequestMapper.class);

	private final ElementControllerValidator validator = mock(ElementControllerValidator.class);

	private final ElementController controller = new ElementController(elementDatabaseService,
			screenshotDatabaseService, stageDatabaseService, facade, screenshotFileRequestMapper, validator);

	@Test
	void createNewElement_shouldMapStoreAndReturnOk() {
		ElementDefinitionDTO definitionDTO = new ElementDefinitionDTO().label("Login Button")
			.locatorString("#login")
			.locatorType(io.github.yaforster.trails.adapter.api.rest.model.LocatorTypeDTO.CSS)
			.type(io.github.yaforster.trails.adapter.api.rest.model.ElementTypeDTO.BUTTON);
		ElementDefinition definition = new ElementDefinition(ElementType.BUTTON, "Login Button", "#login",
				LocatorType.CSS);
		PersistedElement persisted = new PersistedElement(9L, "Login Button", LocatorType.CSS, 1L, 2L, "#login",
				ElementType.BUTTON);
		PersistedElementDTO dto = new PersistedElementDTO().id(9L);
		when(facade.toDomain(definitionDTO)).thenReturn(definition);
		when(elementDatabaseService.storeElement(new ElementDatabaseService.ElementCreation(1L, 2L, definition)))
			.thenReturn(persisted);
		when(facade.toDTO(persisted)).thenReturn(dto);

		ResponseEntity<PersistedElementDTO> response = controller.createNewElement(1L, 2L, definitionDTO);

		assertEquals(HttpStatus.OK, response.getStatusCode());
		assertSame(dto, response.getBody());
		verify(validator).validateCreateNewElement(1L, 2L, definitionDTO);
		verify(facade).toDomain(definitionDTO);
		verify(elementDatabaseService).storeElement(new ElementDatabaseService.ElementCreation(1L, 2L, definition));
		verify(facade).toDTO(persisted);
	}

	@Test
	void getElement_shouldReturnOkWithMappedContext_whenFound() {
		PersistedElement persisted = new PersistedElement(9L, "Login Button", LocatorType.CSS, 1L, 2L, "#login",
				ElementType.BUTTON);
		ElementDTO dto = new ElementDTO().id(9L);
		when(elementDatabaseService.getElement(new ElementDatabaseService.ElementDetails(1L, 2L, 9L, false)))
			.thenReturn(Optional.of(persisted));
		when(screenshotDatabaseService.findIdByElementId(9L)).thenReturn(Optional.of(99L));
		when(facade.mapToElementDTO(any(PersistedElementContext.class))).thenReturn(dto);

		ResponseEntity<ElementDTO> response = controller.getElement(1L, 2L, 9L, false);

		assertEquals(HttpStatus.OK, response.getStatusCode());
		assertSame(dto, response.getBody());
		ArgumentCaptor<PersistedElementContext> contextCaptor = ArgumentCaptor.forClass(PersistedElementContext.class);
		verify(facade).mapToElementDTO(contextCaptor.capture());
		PersistedElementContext context = contextCaptor.getValue();
		assertEquals(1L, context.applicationID());
		assertEquals(2L, context.stageID());
		assertSame(persisted, context.persistedElement());
		assertEquals(Optional.of(99L), context.screenshotID());
		verify(validator).validateGetElement(1L, 2L, 9L);
	}

	@Test
	void deleteElement_shouldReturnStatusFromDeletionResult() {
		DatabaseDeletionResult deletionResult = new DeletionFailure(9L, "conflict", null);
		DatabaseDeletionResultDTO dto = new DatabaseDeletionResultDTO();
		when(elementDatabaseService.deleteElement(new ElementDatabaseService.ElementReference(1L, 2L, 9L)))
			.thenReturn(deletionResult);
		when(facade.toDTO(1L, 2L, deletionResult)).thenReturn(dto);
		when(facade.toHTTPStatus(deletionResult)).thenReturn(HttpStatus.CONFLICT);

		ResponseEntity<DatabaseDeletionResultDTO> response = controller.deleteElement(1L, 2L, 9L);

		assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
		assertSame(dto, response.getBody());
		verify(validator).validateDeleteElement(1L, 2L, 9L);
		verify(elementDatabaseService).deleteElement(new ElementDatabaseService.ElementReference(1L, 2L, 9L));
		verify(facade).toDTO(1L, 2L, deletionResult);
	}

	@Test
	void promoteElement_shouldReturnCreatedElementWithLocation_whenSourceAndTargetStagesExist() {
		PersistedStage sourceStage = new PersistedStage(2L, 1L, "dev", "https://dev.example");
		PersistedStage targetStage = new PersistedStage(3L, 1L, "test", "https://test.example");
		PersistedElement promoted = new PersistedElement(12L, "Login Button", LocatorType.CSS, 1L, 3L, "#login",
				ElementType.BUTTON);
		ElementDTO dto = new ElementDTO().id(12L);
		when(stageDatabaseService.getStage(new StageDatabaseService.StageDetails(1L, 2L, false)))
			.thenReturn(Optional.of(sourceStage));
		when(stageDatabaseService.getStage(new StageDatabaseService.StageDetails(1L, 3L, false)))
			.thenReturn(Optional.of(targetStage));
		when(elementDatabaseService.promoteElement(new ElementDatabaseService.ElementPromotion(1L, 2L, 9L, 3L)))
			.thenReturn(Optional.of(promoted));
		when(screenshotDatabaseService.findIdByElementId(12L)).thenReturn(Optional.of(99L));
		when(facade.mapToElementDTO(any(PersistedElementContext.class))).thenReturn(dto);

		ResponseEntity<ElementDTO> response = controller.promoteElement(1L, 2L, 9L, 3L);

		assertEquals(HttpStatus.CREATED, response.getStatusCode());
		assertEquals(java.net.URI.create("/api/applications/1/stages/3/elements/12"),
				response.getHeaders().getLocation());
		assertSame(dto, response.getBody());
		verify(validator).validatePromoteElement(1L, 2L, 9L, 3L);
		verify(elementDatabaseService).promoteElement(new ElementDatabaseService.ElementPromotion(1L, 2L, 9L, 3L));
	}

	@Test
	void promoteElement_shouldReturnNotFound_whenTargetStageIsMissing() {
		PersistedStage sourceStage = new PersistedStage(2L, 1L, "dev", "https://dev.example");
		when(stageDatabaseService.getStage(new StageDatabaseService.StageDetails(1L, 2L, false)))
			.thenReturn(Optional.of(sourceStage));
		when(stageDatabaseService.getStage(new StageDatabaseService.StageDetails(1L, 3L, false)))
			.thenReturn(Optional.empty());

		ResponseEntity<ElementDTO> response = controller.promoteElement(1L, 2L, 9L, 3L);

		assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
		verify(validator).validatePromoteElement(1L, 2L, 9L, 3L);
		verify(elementDatabaseService, never()).promoteElement(any());
	}

	@Test
	void deleteElementScreenshot_shouldReturnNotFoundDeletionResult_whenElementIsMissing() {
		DatabaseDeletionResultDTO dto = new DatabaseDeletionResultDTO();
		when(elementDatabaseService.getElement(new ElementDatabaseService.ElementDetails(1L, 2L, 9L, false)))
			.thenReturn(Optional.empty());
		when(facade.toDTO(eq(1L), eq(2L), any())).thenReturn(dto);
		when(facade.toHTTPStatus(any())).thenReturn(HttpStatus.NOT_FOUND);

		ResponseEntity<DatabaseDeletionResultDTO> response = controller.deleteElementScreenshot(1L, 2L, 9L);

		assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
		assertSame(dto, response.getBody());
		verify(validator).validateDeleteElementScreenshot(1L, 2L, 9L);
		verify(elementDatabaseService).getElement(new ElementDatabaseService.ElementDetails(1L, 2L, 9L, false));
		verifyNoInteractions(screenshotDatabaseService);
	}

	@Test
	void deleteElementScreenshot_shouldReturnStatusFromDeletionResult_whenElementExists() {
		PersistedElement persisted = new PersistedElement(9L, "Login Button", LocatorType.CSS, 1L, 2L, "#login",
				ElementType.BUTTON);
		DatabaseDeletionResult deletionResult = new DeletionSuccess(9L);
		DatabaseDeletionResultDTO dto = new DatabaseDeletionResultDTO();
		when(elementDatabaseService.getElement(new ElementDatabaseService.ElementDetails(1L, 2L, 9L, false)))
			.thenReturn(Optional.of(persisted));
		when(screenshotDatabaseService.deleteElementScreenshot(9L)).thenReturn(deletionResult);
		when(facade.toDTO(1L, 2L, deletionResult)).thenReturn(dto);
		when(facade.toHTTPStatus(deletionResult)).thenReturn(HttpStatus.OK);

		ResponseEntity<DatabaseDeletionResultDTO> response = controller.deleteElementScreenshot(1L, 2L, 9L);

		assertEquals(HttpStatus.OK, response.getStatusCode());
		assertSame(dto, response.getBody());
		verify(validator).validateDeleteElementScreenshot(1L, 2L, 9L);
		verify(screenshotDatabaseService).deleteElementScreenshot(9L);
	}

	@Test
	void getElement_shouldReturnNotFound_whenMissing() {
		when(elementDatabaseService.getElement(new ElementDatabaseService.ElementDetails(1L, 2L, 9L, false)))
			.thenReturn(Optional.empty());

		ResponseEntity<ElementDTO> response = controller.getElement(1L, 2L, 9L, false);

		assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
		assertNull(response.getBody());
		verify(validator).validateGetElement(1L, 2L, 9L);
		verify(elementDatabaseService).getElement(new ElementDatabaseService.ElementDetails(1L, 2L, 9L, false));
		verifyNoInteractions(screenshotDatabaseService, facade);
	}

	@Test
	@SuppressWarnings("unchecked")
	void listElements_shouldMapScreenshotIdsIntoContextsAndReturnOk() {
		PersistedElement first = new PersistedElement(9L, "First", LocatorType.CSS, 1L, 2L, "#one", ElementType.BUTTON);
		PersistedElement second = new PersistedElement(10L, "Second", LocatorType.XPATH, 1L, 2L, "//two",
				ElementType.INPUT);
		PagedResult<PersistedElement> page = new PagedResult<>(List.of(first, second), 1, 20, 80);
		when(elementDatabaseService.getElements(new ElementDatabaseService.ElementPage(1L, 2L, 1, 20, false)))
			.thenReturn(page);
		when(screenshotDatabaseService
			.findAllIdsByElementIds(new ScreenshotDatabaseService.ElementScreenshotIds(List.of(9L, 10L))))
			.thenReturn(Map.of(9L, 501L));
		PagedElementDTO dto = new PagedElementDTO();
		when(facade.mapContextsToPagedElementDTO(eq(1L), eq(2L), any(PagedResult.class))).thenReturn(dto);

		ResponseEntity<PagedElementDTO> response = controller.listElements(1L, 2L, 1, 20, false);

		assertEquals(HttpStatus.OK, response.getStatusCode());
		assertSame(dto, response.getBody());
		verify(elementDatabaseService).getElements(new ElementDatabaseService.ElementPage(1L, 2L, 1, 20, false));
		verify(screenshotDatabaseService)
			.findAllIdsByElementIds(new ScreenshotDatabaseService.ElementScreenshotIds(List.of(9L, 10L)));

		ArgumentCaptor<PagedResult<PersistedElementContext>> pageCaptor = (ArgumentCaptor<PagedResult<PersistedElementContext>>) (ArgumentCaptor<?>) ArgumentCaptor
			.forClass(PagedResult.class);
		verify(facade).mapContextsToPagedElementDTO(eq(1L), eq(2L), pageCaptor.capture());
		List<PersistedElementContext> contexts = pageCaptor.getValue().getContent();
		assertEquals(2, contexts.size());
		assertEquals(Optional.of(501L), contexts.getFirst().screenshotID());
		assertEquals(Optional.empty(), contexts.get(1).screenshotID());
		assertSame(first, contexts.getFirst().persistedElement());
		assertSame(second, contexts.get(1).persistedElement());
		verify(validator).validateListElements(1L, 2L, 1, 20);
	}

	@Test
	void getElementScreenshot_shouldReturnStoredContentType_whenElementAndScreenshotExist() {
		PersistedElement persisted = new PersistedElement(9L, "Login Button", LocatorType.CSS, 1L, 2L, "#login",
				ElementType.BUTTON);
		byte[] content = new byte[] { 1, 2, 3 };
		Screenshot screenshot = new Screenshot(new BinaryContent(content), "shot.png", MediaType.IMAGE_PNG_VALUE);
		when(elementDatabaseService.getElement(new ElementDatabaseService.ElementDetails(1L, 2L, 9L, false)))
			.thenReturn(Optional.of(persisted));
		when(screenshotDatabaseService.loadScreenshotByElementId(9L)).thenReturn(Optional.of(screenshot));

		ResponseEntity<Resource> response = controller.getElementScreenshot(1L, 2L, 9L);

		assertEquals(HttpStatus.OK, response.getStatusCode());
		assertEquals(MediaType.IMAGE_PNG, response.getHeaders().getContentType());
		assertEquals(content.length, response.getHeaders().getContentLength());
		assertEquals("inline; filename=\"shot.png\"", response.getHeaders().getFirst(HttpHeaders.CONTENT_DISPOSITION));
		assertNotNull(response.getBody());
		verify(validator).validateGetElementScreenshot(1L, 2L, 9L);
	}

	@Test
	void getElementScreenshot_shouldReturnNotFound_whenElementIsMissing() {
		when(elementDatabaseService.getElement(new ElementDatabaseService.ElementDetails(1L, 2L, 9L, false)))
			.thenReturn(Optional.empty());

		ResponseEntity<Resource> response = controller.getElementScreenshot(1L, 2L, 9L);

		assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
		verify(validator).validateGetElementScreenshot(1L, 2L, 9L);
		verifyNoInteractions(screenshotDatabaseService);
	}

	@Test
	void getElementScreenshot_shouldReturnNotFound_whenScreenshotIsMissing() {
		PersistedElement persisted = new PersistedElement(9L, "Login Button", LocatorType.CSS, 1L, 2L, "#login",
				ElementType.BUTTON);
		when(elementDatabaseService.getElement(new ElementDatabaseService.ElementDetails(1L, 2L, 9L, false)))
			.thenReturn(Optional.of(persisted));
		when(screenshotDatabaseService.loadScreenshotByElementId(9L)).thenReturn(Optional.empty());

		ResponseEntity<Resource> response = controller.getElementScreenshot(1L, 2L, 9L);

		assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
		verify(validator).validateGetElementScreenshot(1L, 2L, 9L);
	}

	@Test
	void uploadElementScreenshot_shouldStoreScreenshotAndReturnOk_whenElementExists() {
		PersistedElement persisted = new PersistedElement(9L, "Login Button", LocatorType.CSS, 1L, 2L, "#login",
				ElementType.BUTTON);
		MockMultipartFile file = new MockMultipartFile("file", "element.jpg", "image/jpeg", new byte[] { 1, 2, 3 });
		TrailsScreenshotFile screenshotFile = screenshotFile("element.jpg", jpegBytes());
		when(elementDatabaseService.getElement(new ElementDatabaseService.ElementDetails(1L, 2L, 9L, false)))
			.thenReturn(Optional.of(persisted));
		when(screenshotFileRequestMapper.from(file)).thenReturn(screenshotFile);
		when(screenshotDatabaseService.findIdByElementId(9L)).thenReturn(Optional.of(42L));
		ArtifactDTO artifactDTO = new ArtifactDTO().id(42L)
			.type(ArtifactDTO.TypeEnum.SCREENSHOT)
			.filename("element.jpg");
		when(facade.toDTO(1L, 2L, 9L, 42L, screenshotFile)).thenReturn(artifactDTO);

		ResponseEntity<ArtifactDTO> response = controller.uploadElementScreenshot(1L, 2L, 9L, file);

		assertEquals(HttpStatus.OK, response.getStatusCode());
		assertSame(artifactDTO, response.getBody());
		verify(validator).validateUploadElementScreenshot(1L, 2L, 9L, file);
		verify(elementDatabaseService).getElement(new ElementDatabaseService.ElementDetails(1L, 2L, 9L, false));
		verify(screenshotFileRequestMapper).from(file);
		verify(screenshotDatabaseService)
			.setScreenshot(new ScreenshotDatabaseService.ElementScreenshot(9L, screenshotFile));
		verify(screenshotDatabaseService).findIdByElementId(9L);
		verify(facade).toDTO(1L, 2L, 9L, 42L, screenshotFile);
	}

	@Test
	void uploadElementScreenshot_shouldReturnNotFoundAndNotStore_whenElementIsMissing() {
		MockMultipartFile file = new MockMultipartFile("file", "element.jpg", "image/jpeg", new byte[] { 1, 2, 3 });
		TrailsScreenshotFile screenshotFile = screenshotFile("element.jpg", jpegBytes());
		when(elementDatabaseService.getElement(new ElementDatabaseService.ElementDetails(1L, 2L, 9L, false)))
			.thenReturn(Optional.empty());
		when(screenshotFileRequestMapper.from(file)).thenReturn(screenshotFile);

		ResponseEntity<ArtifactDTO> response = controller.uploadElementScreenshot(1L, 2L, 9L, file);

		assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
		verify(validator).validateUploadElementScreenshot(1L, 2L, 9L, file);
		verify(screenshotFileRequestMapper).from(file);
		verify(elementDatabaseService).getElement(new ElementDatabaseService.ElementDetails(1L, 2L, 9L, false));
		verify(screenshotDatabaseService, never()).setScreenshot(any());
		verify(screenshotDatabaseService, never()).findIdByElementId(anyLong());
		verify(facade, never()).toDTO(anyLong(), anyLong(), anyLong(), any(), any());
	}

}
