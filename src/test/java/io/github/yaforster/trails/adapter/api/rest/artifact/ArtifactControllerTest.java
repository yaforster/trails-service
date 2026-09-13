package io.github.yaforster.trails.adapter.api.rest.artifact;

import io.github.yaforster.trails.core.PagedResult;

import io.github.yaforster.trails.adapter.api.rest.artifact.mapper.ArtifactResponseMapper;
import io.github.yaforster.trails.adapter.api.rest.model.PagedArtifactDTO;
import io.github.yaforster.trails.app.services.ActionResultScreenshotQueryService;
import io.github.yaforster.trails.app.services.ArtifactQueryService;
import io.github.yaforster.trails.app.services.HierarchyValidationService;
import io.github.yaforster.trails.core.persisted.PersistedArtifact;
import io.github.yaforster.trails.core.persisted.PersistedArtifactFile;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ArtifactControllerTest {

	private final ArtifactQueryService artifactQueryService = mock(ArtifactQueryService.class);

	private final ActionResultScreenshotQueryService actionResultScreenshotQueryService = mock(
			ActionResultScreenshotQueryService.class);

	private final HierarchyValidationService hierarchyValidationService = mock(HierarchyValidationService.class);

	private final ArtifactHATEOASFacade facade = mock(ArtifactHATEOASFacade.class);

	private final ArtifactResponseMapper artifactResponseMapper = new ArtifactResponseMapper();

	private final ArtifactControllerValidator validator = mock(ArtifactControllerValidator.class);

	private final ArtifactController controller = new ArtifactController(artifactQueryService,
			actionResultScreenshotQueryService, hierarchyValidationService, facade, artifactResponseMapper, validator);

	@Test
	void downloadArtifact_shouldReturnAttachment_whenFound() throws IOException {
		byte[] content = new byte[] { 10, 20, 30, 40 };
		PersistedArtifactFile file = new PersistedArtifactFile(11L, "report.txt", "text/plain", content);
		mockHierarchy(1L, 2L, 3L, 4L, 7L);
		when(artifactQueryService.findArtifactInPath(new ArtifactQueryService.PathArtifact(7L, 11L)))
			.thenReturn(Optional.of(file));

		ResponseEntity<Resource> response = controller.downloadArtifactInTestPath(1L, 2L, 3L, 4L, 7L, 11L);

		assertEquals(HttpStatus.OK, response.getStatusCode());
		assertEquals(MediaType.APPLICATION_OCTET_STREAM, response.getHeaders().getContentType());
		assertEquals(content.length, response.getHeaders().getContentLength());
		assertEquals("attachment; filename=\"report.txt\"",
				response.getHeaders().getFirst(HttpHeaders.CONTENT_DISPOSITION));
		assertNotNull(response.getBody());
		assertArrayEquals(content, response.getBody().getContentAsByteArray());
		verify(validator).validateDownloadArtifact(1L, 2L, 3L, 4L, 7L, 11L);
	}

	@Test
	void downloadArtifact_shouldReturnNotFound_whenMissing() {
		mockHierarchy(1L, 2L, 3L, 4L, 7L);
		when(artifactQueryService.findArtifactInPath(new ArtifactQueryService.PathArtifact(7L, 11L)))
			.thenReturn(Optional.empty());

		ResponseEntity<Resource> response = controller.downloadArtifactInTestPath(1L, 2L, 3L, 4L, 7L, 11L);

		assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
		assertNull(response.getBody());
		verify(validator).validateDownloadArtifact(1L, 2L, 3L, 4L, 7L, 11L);
	}

	@Test
	void downloadActionResultScreenshot_shouldReturnInlineImage_whenFound() throws IOException {
		byte[] content = new byte[] { 11, 22, 33 };
		PersistedArtifactFile screenshot = new PersistedArtifactFile(21L, "action-result-21.png", "image/png", content);
		mockHierarchy(1L, 2L, 3L, 4L, 7L);
		when(actionResultScreenshotQueryService
			.findInPath(new ActionResultScreenshotQueryService.PathActionScreenshot(7L, 21L)))
			.thenReturn(Optional.of(screenshot));

		ResponseEntity<Resource> response = controller.downloadActionResultScreenshotInTestPath(1L, 2L, 3L, 4L, 7L,
				21L);

		assertEquals(HttpStatus.OK, response.getStatusCode());
		assertEquals(MediaType.IMAGE_PNG, response.getHeaders().getContentType());
		assertEquals(content.length, response.getHeaders().getContentLength());
		assertEquals("inline; filename=\"action-result-21.png\"",
				response.getHeaders().getFirst(HttpHeaders.CONTENT_DISPOSITION));
		assertNotNull(response.getBody());
		assertArrayEquals(content, response.getBody().getContentAsByteArray());
		verify(validator).validateDownloadActionResultScreenshot(1L, 2L, 3L, 4L, 7L, 21L);
	}

	@Test
	void downloadActionResultScreenshot_shouldReturnNotFound_whenScreenshotMissing() {
		mockHierarchy(1L, 2L, 3L, 4L, 7L);
		when(actionResultScreenshotQueryService
			.findInPath(new ActionResultScreenshotQueryService.PathActionScreenshot(7L, 21L)))
			.thenReturn(Optional.empty());

		ResponseEntity<Resource> response = controller.downloadActionResultScreenshotInTestPath(1L, 2L, 3L, 4L, 7L,
				21L);

		assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
		verify(validator).validateDownloadActionResultScreenshot(1L, 2L, 3L, 4L, 7L, 21L);
	}

	@Test
	void listArtifacts_shouldDelegateToFacadeAndReturnOk() {
		PagedResult<PersistedArtifact> paged = new PagedResult<>(List.of(), 0, 20, 0);
		PagedArtifactDTO dto = new PagedArtifactDTO();
		mockHierarchy(1L, 2L, 3L, 4L, 77L);
		when(artifactQueryService.listArtifacts(new ArtifactQueryService.PathArtifactPage(77L, 0, 20)))
			.thenReturn(paged);
		when(facade.toPagedDTOInHierarchy(1L, 2L, 3L, 4L, 77L, paged)).thenReturn(dto);

		ResponseEntity<PagedArtifactDTO> response = controller.listArtifactsInTestPath(1L, 2L, 3L, 4L, 77L, 0, 20);

		assertEquals(HttpStatus.OK, response.getStatusCode());
		assertSame(dto, response.getBody());
		verify(validator).validateListArtifacts(1L, 2L, 3L, 4L, 77L, 0, 20);
		verify(artifactQueryService).listArtifacts(new ArtifactQueryService.PathArtifactPage(77L, 0, 20));
		verify(facade).toPagedDTOInHierarchy(1L, 2L, 3L, 4L, 77L, paged);
	}

	@Test
	void listArtifacts_shouldReturnNotFound_whenHierarchyMismatch() {
		when(hierarchyValidationService
			.pathBelongsToHierarchy(new HierarchyValidationService.TestPathHierarchy(1L, 2L, 3L, 4L, 77L)))
			.thenReturn(false);

		ResponseEntity<PagedArtifactDTO> response = controller.listArtifactsInTestPath(1L, 2L, 3L, 4L, 77L, 0, 20);

		assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
		verify(validator).validateListArtifacts(1L, 2L, 3L, 4L, 77L, 0, 20);
		verifyNoInteractions(artifactQueryService, facade);
	}

	@Test
	void downloadActionResultScreenshot_shouldReturnNotFound_whenHierarchyMismatch() {
		when(hierarchyValidationService
			.pathBelongsToHierarchy(new HierarchyValidationService.TestPathHierarchy(1L, 2L, 3L, 4L, 7L)))
			.thenReturn(false);

		ResponseEntity<Resource> response = controller.downloadActionResultScreenshotInTestPath(1L, 2L, 3L, 4L, 7L,
				21L);

		assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
		verify(validator).validateDownloadActionResultScreenshot(1L, 2L, 3L, 4L, 7L, 21L);
		verifyNoInteractions(actionResultScreenshotQueryService);
	}

	private void mockHierarchy(Long applicationId, Long stageId, Long testRunId, Long testSetResultId,
			Long pathResultId) {
		when(hierarchyValidationService.pathBelongsToHierarchy(new HierarchyValidationService.TestPathHierarchy(
				applicationId, stageId, testRunId, testSetResultId, pathResultId)))
			.thenReturn(true);
	}

}
