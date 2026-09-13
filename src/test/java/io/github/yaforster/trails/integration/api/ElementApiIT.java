package io.github.yaforster.trails.integration.api;

import io.github.yaforster.trails.core.PagedResult;
import io.github.yaforster.trails.core.BinaryContent;

import io.github.yaforster.trails.app.services.ElementDatabaseService;
import io.github.yaforster.trails.app.services.ScreenshotDatabaseService;
import io.github.yaforster.trails.core.data.ElementType;
import io.github.yaforster.trails.core.data.Screenshot;
import io.github.yaforster.trails.core.definition.ElementDefinition;
import io.github.yaforster.trails.core.deletion.DeletionFailure;
import io.github.yaforster.trails.core.deletion.DeletionNotFound;
import io.github.yaforster.trails.core.deletion.DeletionSuccess;
import io.github.yaforster.trails.core.deletion.ErrorDetails;
import io.github.yaforster.trails.core.persisted.PersistedElement;
import io.github.yaforster.trails.core.test.action.element.LocatorType;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.ResultActions;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.hamcrest.Matchers.containsString;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class ElementApiIT extends RestApiIntegrationTestBase {

	@MockitoBean
	private ElementDatabaseService elementDatabaseService;

	@MockitoBean
	private ScreenshotDatabaseService screenshotDatabaseService;

	@Test
	void createNewElement_shouldMapDefinitionAndReturnPersistedDto() throws Exception {
		PersistedElement persistedElement = new PersistedElement(901L, "Username", LocatorType.CSS, 77L, 88L,
				"#username", ElementType.INPUT);
		when(elementDatabaseService.storeElement(any(ElementDatabaseService.ElementCreation.class)))
			.thenReturn(persistedElement);

		performPutJson("/applications/77/stages/88/elements", """
				{
				  "type": "INPUT",
				  "label": "Username",
				  "locatorString": "#username",
				  "locatorType": "CSS"
				}
				""").andExpect(status().isOk())
			.andExpect(jsonPath("$.id").value(901))
			.andExpect(jsonPath("$.type").value("INPUT"))
			.andExpect(jsonPath("$.label").value("Username"))
			.andExpect(jsonPath("$.locator").value("#username"))
			.andExpect(jsonPath("$.locatorType").value("CSS"));

		ArgumentCaptor<ElementDatabaseService.ElementCreation> captor = ArgumentCaptor
			.forClass(ElementDatabaseService.ElementCreation.class);
		verify(elementDatabaseService).storeElement(captor.capture());
		assertEquals(ElementType.INPUT, captor.getValue().elementDefinition().type());
		assertEquals("Username", captor.getValue().elementDefinition().label());
		assertEquals("#username", captor.getValue().elementDefinition().locatorString());
		assertEquals(LocatorType.CSS, captor.getValue().elementDefinition().locatorType());
	}

	@Test
	void createNewElement_shouldReturnBadRequest_forMalformedJson() throws Exception {
		performPutJson("/applications/77/stages/88/elements", "{ invalid-json }").andExpect(status().isBadRequest());

		verifyNoInteractions(elementDatabaseService, screenshotDatabaseService);
	}

	@Test
	void createNewElement_shouldReturnValidationErrors_whenLabelAndLocatorAlreadyExist() throws Exception {
		when(elementDatabaseService.existsByLabel(new ElementDatabaseService.ElementLabel(77L, 88L, "Username")))
			.thenReturn(true);
		when(elementDatabaseService
			.existsByLocatorString(new ElementDatabaseService.ElementLocatorText(77L, 88L, "#username")))
			.thenReturn(true);

		performPutJson("/applications/77/stages/88/elements", """
				{
				  "type": "INPUT",
				  "label": "Username",
				  "locatorString": "#username",
				  "locatorType": "CSS"
				}
				""", MediaType.APPLICATION_PROBLEM_JSON).andExpect(status().isBadRequest())
			.andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
			.andExpect(jsonPath("$[0].code").value("ELEMENT_LABEL_ALREADY_EXISTS"))
			.andExpect(jsonPath("$[1].code").value("ELEMENT_LOCATOR_STRING_ALREADY_EXISTS"));

		verify(elementDatabaseService, never()).storeElement(any());
	}

	@Test
	void listElements_shouldReturnPagedResponseWithExpectedLinks() throws Exception {
		PersistedElement first = new PersistedElement(801L, "Username", LocatorType.CSS, 77L, 88L, "#username",
				ElementType.INPUT);
		PersistedElement second = new PersistedElement(802L, "Submit", LocatorType.XPATH, 77L, 88L,
				"//button[@type='submit']", ElementType.BUTTON);
		PagedResult<PersistedElement> pagedElements = new PagedResult<>(List.of(first, second), 1, 2, 5);

		when(elementDatabaseService.getElements(new ElementDatabaseService.ElementPage(77L, 88L, 1, 2, false)))
			.thenReturn(pagedElements);
		when(screenshotDatabaseService
			.findAllIdsByElementIds(new ScreenshotDatabaseService.ElementScreenshotIds(List.of(801L, 802L))))
			.thenReturn(Map.of(801L, 7001L));

		ResultActions response = performGet("/applications/77/stages/88/elements?page=1&size=2")
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.items[0].id").value(801))
			.andExpect(jsonPath("$.items[0].label").value("Username"))
			.andExpect(
					jsonPath("$.items[0]._links.self.href", containsString("/applications/77/stages/88/elements/801")))
			.andExpect(jsonPath("$.items[0]._links.screenshot.href",
					containsString("/applications/77/stages/88/elements/801/screenshot")))
			.andExpect(jsonPath("$.items[1].id").value(802))
			.andExpect(jsonPath("$.items[1].label").value("Submit"))
			.andExpect(
					jsonPath("$.items[1]._links.self.href", containsString("/applications/77/stages/88/elements/802")))
			.andExpect(jsonPath("$.items[1]._links.screenshot").doesNotExist())
			.andExpect(jsonPath("$._links.self.href", containsString("/applications/77/stages/88/elements")));

		expectPage(response, 1, 2, 5, 3);
		expectPagingLinks(response, 0, 2, 0, 2);
	}

	@Test
	void listElements_shouldUseDefaultPagingParameters_whenQueryParamsMissing() throws Exception {
		PersistedElement persistedElement = new PersistedElement(811L, "Email", LocatorType.CSS, 77L, 88L, "#email",
				ElementType.INPUT);
		PagedResult<PersistedElement> pagedElements = new PagedResult<>(List.of(persistedElement), 0, 20, 1);

		when(elementDatabaseService.getElements(new ElementDatabaseService.ElementPage(77L, 88L, 0, 20, false)))
			.thenReturn(pagedElements);
		when(screenshotDatabaseService
			.findAllIdsByElementIds(new ScreenshotDatabaseService.ElementScreenshotIds(List.of(811L))))
			.thenReturn(Collections.emptyMap());

		ResultActions response = performGet("/applications/77/stages/88/elements").andExpect(status().isOk());

		expectPage(response, DEFAULT_PAGE, DEFAULT_SIZE, 1, 1);
		expectDefaultSelfPagingParams(response);
	}

	@Test
	void listElements_shouldReturnEmptyPageWithoutPrevAndNext() throws Exception {
		PagedResult<PersistedElement> emptyPage = new PagedResult<>(Collections.emptyList(), 0, 20, 0);

		when(elementDatabaseService.getElements(new ElementDatabaseService.ElementPage(77L, 88L, 0, 20, false)))
			.thenReturn(emptyPage);
		when(screenshotDatabaseService
			.findAllIdsByElementIds(new ScreenshotDatabaseService.ElementScreenshotIds(List.of())))
			.thenReturn(Collections.emptyMap());

		ResultActions response = performGet("/applications/77/stages/88/elements?page=0&size=20")
			.andExpect(status().isOk());

		expectEmptyItems(response);
		expectPage(response, DEFAULT_PAGE, DEFAULT_SIZE, 0, 0);
		expectPagingLinks(response, 0, 0, null, null);
	}

	@Test
	void getElement_shouldReturnElementWithScreenshotLink_whenScreenshotExists() throws Exception {
		PersistedElement persistedElement = new PersistedElement(821L, "Password", LocatorType.CSS, 77L, 88L,
				"#password", ElementType.INPUT);
		when(elementDatabaseService.getElement(new ElementDatabaseService.ElementDetails(77L, 88L, 821L, false)))
			.thenReturn(Optional.of(persistedElement));
		when(screenshotDatabaseService.findIdByElementId(821L)).thenReturn(Optional.of(9001L));

		performGet("/applications/77/stages/88/elements/821").andExpect(status().isOk())
			.andExpect(jsonPath("$.id").value(821))
			.andExpect(jsonPath("$.type").value("INPUT"))
			.andExpect(jsonPath("$.label").value("Password"))
			.andExpect(jsonPath("$.locatorString").value("#password"))
			.andExpect(jsonPath("$.locatorType").value("CSS"))
			.andExpect(jsonPath("$._links.self.href", containsString("/applications/77/stages/88/elements/821")))
			.andExpect(jsonPath("$._links.collection.href", containsString("/applications/77/stages/88/elements")))
			.andExpect(jsonPath("$._links.screenshot.href",
					containsString("/applications/77/stages/88/elements/821/screenshot")));
	}

	@Test
	void deleteElement_shouldReturnOk_forDeletionSuccess() throws Exception {
		when(elementDatabaseService.deleteElement(new ElementDatabaseService.ElementReference(77L, 88L, 821L)))
			.thenReturn(new DeletionSuccess(821L));

		performDelete("/applications/77/stages/88/elements/821").andExpect(status().isOk())
			.andExpect(jsonPath("$.deletionResult").value(true))
			.andExpect(jsonPath("$.deletionRequestedForID").value(821));
	}

	@Test
	void deleteElement_shouldReturnNotFound_forDeletionNotFound() throws Exception {
		when(elementDatabaseService.deleteElement(new ElementDatabaseService.ElementReference(77L, 88L, 822L)))
			.thenReturn(new DeletionNotFound(822L));

		performDelete("/applications/77/stages/88/elements/822").andExpect(status().isNotFound())
			.andExpect(jsonPath("$.deletionResult").value(false))
			.andExpect(jsonPath("$.deletionRequestedForID").value(822))
			.andExpect(jsonPath("$._links.collection.href", containsString("/applications/77/stages/88/elements")));
	}

	@Test
	void deleteElement_shouldReturnConflict_forDeletionConflict() throws Exception {
		when(elementDatabaseService.deleteElement(new ElementDatabaseService.ElementReference(77L, 88L, 823L)))
			.thenReturn(new DeletionFailure(823L, "conflict", null));

		performDelete("/applications/77/stages/88/elements/823").andExpect(status().isConflict())
			.andExpect(jsonPath("$.deletionResult").value(false))
			.andExpect(jsonPath("$.deletionRequestedForID").value(823));
	}

	@Test
	void deleteElement_shouldReturnInternalServerError_forDeletionFailure() throws Exception {
		DeletionFailure failure = new DeletionFailure(824L, "boom",
				new ErrorDetails("RuntimeException", "boom", "trace"));
		when(elementDatabaseService.deleteElement(new ElementDatabaseService.ElementReference(77L, 88L, 824L)))
			.thenReturn(failure);

		performDelete("/applications/77/stages/88/elements/824").andExpect(status().isInternalServerError())
			.andExpect(jsonPath("$.deletionResult").value(false))
			.andExpect(jsonPath("$.deletionRequestedForID").value(824));
	}

	@Test
	void getElement_shouldReturnElementWithoutScreenshotLink_whenNoScreenshotExists() throws Exception {
		PersistedElement persistedElement = new PersistedElement(822L, "Remember Me", LocatorType.CSS, 77L, 88L,
				"#remember", ElementType.CHECKBOX);
		when(elementDatabaseService.getElement(new ElementDatabaseService.ElementDetails(77L, 88L, 822L, false)))
			.thenReturn(Optional.of(persistedElement));
		when(screenshotDatabaseService.findIdByElementId(822L)).thenReturn(Optional.empty());

		performGet("/applications/77/stages/88/elements/822").andExpect(status().isOk())
			.andExpect(jsonPath("$.id").value(822))
			.andExpect(jsonPath("$.label").value("Remember Me"))
			.andExpect(jsonPath("$._links.screenshot").doesNotExist());
	}

	@Test
	void getElement_shouldReturnNotFound_whenMissing() throws Exception {
		when(elementDatabaseService.getElement(new ElementDatabaseService.ElementDetails(77L, 88L, 999L, false)))
			.thenReturn(Optional.empty());

		performGet("/applications/77/stages/88/elements/999").andExpect(status().isNotFound());

		verifyNoInteractions(screenshotDatabaseService);
	}

	@Test
	void getElementScreenshot_shouldReturnStoredContentType_whenElementAndScreenshotExist() throws Exception {
		PersistedElement persistedElement = new PersistedElement(821L, "Password", LocatorType.CSS, 77L, 88L,
				"#password", ElementType.INPUT);
		byte[] bytes = new byte[] { 10, 20, 30, 40 };
		Screenshot screenshot = new Screenshot(new BinaryContent(bytes), "element-821.png", MediaType.IMAGE_PNG_VALUE);
		when(elementDatabaseService.getElement(new ElementDatabaseService.ElementDetails(77L, 88L, 821L, false)))
			.thenReturn(Optional.of(persistedElement));
		when(screenshotDatabaseService.loadScreenshotByElementId(821L)).thenReturn(Optional.of(screenshot));

		performGet("/applications/77/stages/88/elements/821/screenshot", MediaType.IMAGE_PNG).andExpect(status().isOk())
			.andExpect(content().contentType(MediaType.IMAGE_PNG))
			.andExpect(header().string("Content-Disposition", containsString("inline; filename=\"element-821.png\"")))
			.andExpect(header().longValue("Content-Length", bytes.length))
			.andExpect(content().bytes(bytes));
	}

	@Test
	void deleteElementScreenshot_shouldReturnOk_forDeletionSuccess() throws Exception {
		PersistedElement persistedElement = new PersistedElement(821L, "Password", LocatorType.CSS, 77L, 88L,
				"#password", ElementType.INPUT);
		when(elementDatabaseService.getElement(new ElementDatabaseService.ElementDetails(77L, 88L, 821L, false)))
			.thenReturn(Optional.of(persistedElement));
		when(screenshotDatabaseService.deleteElementScreenshot(821L)).thenReturn(new DeletionSuccess(821L));

		performDelete("/applications/77/stages/88/elements/821/screenshot").andExpect(status().isOk())
			.andExpect(jsonPath("$.deletionResult").value(true))
			.andExpect(jsonPath("$.deletionRequestedForID").value(821));
	}

	@Test
	void deleteElementScreenshot_shouldReturnNotFound_whenElementMissing() throws Exception {
		when(elementDatabaseService.getElement(new ElementDatabaseService.ElementDetails(77L, 88L, 999L, false)))
			.thenReturn(Optional.empty());

		performDelete("/applications/77/stages/88/elements/999/screenshot").andExpect(status().isNotFound())
			.andExpect(jsonPath("$.deletionResult").value(false))
			.andExpect(jsonPath("$.deletionRequestedForID").value(999));

		verify(screenshotDatabaseService, never()).deleteElementScreenshot(any(Long.class));
	}

	@Test
	void deleteElementScreenshot_shouldReturnNotFound_whenScreenshotMissing() throws Exception {
		PersistedElement persistedElement = new PersistedElement(825L, "Password", LocatorType.CSS, 77L, 88L,
				"#password", ElementType.INPUT);
		when(elementDatabaseService.getElement(new ElementDatabaseService.ElementDetails(77L, 88L, 825L, false)))
			.thenReturn(Optional.of(persistedElement));
		when(screenshotDatabaseService.deleteElementScreenshot(825L)).thenReturn(new DeletionNotFound(825L));

		performDelete("/applications/77/stages/88/elements/825/screenshot").andExpect(status().isNotFound())
			.andExpect(jsonPath("$.deletionResult").value(false))
			.andExpect(jsonPath("$.deletionRequestedForID").value(825));
	}

	@Test
	void getElementScreenshot_shouldReturnNotFound_whenElementMissing() throws Exception {
		when(elementDatabaseService.getElement(new ElementDatabaseService.ElementDetails(77L, 88L, 999L, false)))
			.thenReturn(Optional.empty());

		performGet("/applications/77/stages/88/elements/999/screenshot", MediaType.IMAGE_JPEG)
			.andExpect(status().isNotFound());

		verify(screenshotDatabaseService, never()).loadScreenshotByElementId(any(Long.class));
	}

	@Test
	void uploadElementScreenshot_shouldStoreScreenshotAndReturnOk_whenElementExists() throws Exception {
		PersistedElement persistedElement = new PersistedElement(821L, "Password", LocatorType.CSS, 77L, 88L,
				"#password", ElementType.INPUT);
		MockMultipartFile file = new MockMultipartFile("file", "element.jpg", "image/jpeg", jpegBytes());
		when(elementDatabaseService.getElement(new ElementDatabaseService.ElementDetails(77L, 88L, 821L, false)))
			.thenReturn(Optional.of(persistedElement));
		when(screenshotDatabaseService.findIdByElementId(821L)).thenReturn(Optional.of(7001L));

		performPutMultipart("/applications/77/stages/88/elements/821/screenshot", file).andExpect(status().isOk())
			.andExpect(jsonPath("$.id").value(7001))
			.andExpect(jsonPath("$.type").value("SCREENSHOT"))
			.andExpect(jsonPath("$.filename").value("element.jpg"))
			.andExpect(jsonPath("$._links.self.href",
					containsString("/applications/77/stages/88/elements/821/screenshot")));

		verify(screenshotDatabaseService).setScreenshot(any(ScreenshotDatabaseService.ElementScreenshot.class));
		verify(screenshotDatabaseService).findIdByElementId(821L);
	}

	@Test
	void uploadElementScreenshot_shouldReturnNotFound_whenElementMissing() throws Exception {
		MockMultipartFile file = new MockMultipartFile("file", "element.jpg", "image/jpeg", jpegBytes());
		when(elementDatabaseService.getElement(new ElementDatabaseService.ElementDetails(77L, 88L, 999L, false)))
			.thenReturn(Optional.empty());

		performPutMultipart("/applications/77/stages/88/elements/999/screenshot", file)
			.andExpect(status().isNotFound());

		verify(screenshotDatabaseService, never())
			.setScreenshot(any(ScreenshotDatabaseService.ElementScreenshot.class));
		verify(screenshotDatabaseService, never()).findIdByElementId(any(Long.class));
	}

	@Test
	void uploadElementScreenshot_shouldReturnBadRequest_whenFileIsNotSupportedImage() throws Exception {
		MockMultipartFile file = new MockMultipartFile("file", "notes.txt", "text/plain", "not an image".getBytes());

		performPutMultipart("/applications/77/stages/88/elements/821/screenshot", file)
			.andExpect(status().isBadRequest())
			.andExpect(jsonPath("$[0].code").value("ELEMENT_SCREENSHOT_FILE_TYPE_UNSUPPORTED"));

		verifyNoInteractions(elementDatabaseService, screenshotDatabaseService);
	}

}
