package io.github.yaforster.trails.integration.api;

import io.github.yaforster.trails.core.PagedResult;

import io.github.yaforster.trails.app.services.ArtifactQueryService;
import io.github.yaforster.trails.app.services.ActionResultPersistence;
import io.github.yaforster.trails.app.services.DownloadedFilePersistence;
import io.github.yaforster.trails.app.services.ActionResultScreenshotQueryService;
import io.github.yaforster.trails.app.services.ActionResultQueryService;
import io.github.yaforster.trails.app.services.TestPathResultQueryService;
import io.github.yaforster.trails.app.services.TestSetResultQueryService;
import io.github.yaforster.trails.core.ArtifactType;
import io.github.yaforster.trails.core.persisted.PersistedArtifact;
import io.github.yaforster.trails.core.persisted.PersistedArtifactFile;
import io.github.yaforster.trails.core.persisted.PersistedTestPathResult;
import io.github.yaforster.trails.core.persisted.PersistedTestSetResult;
import io.github.yaforster.trails.core.test.Browser;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.ResultActions;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class ArtifactApiIT extends RestApiIntegrationTestBase {

	@MockitoBean
	private ArtifactQueryService artifactQueryService;

	@MockitoBean
	private DownloadedFilePersistence downloadedFilePersistence;

	@MockitoBean
	private ActionResultQueryService actionResultQueryService;

	@MockitoBean
	private ActionResultPersistence actionResultPersistence;

	@MockitoBean
	private ActionResultScreenshotQueryService actionResultScreenshotQueryService;

	@MockitoBean
	private TestPathResultQueryService testPathResultQueryService;

	@MockitoBean
	private TestSetResultQueryService testSetResultQueryService;

	@Test
	void downloadArtifactInTestPath_shouldReturnAttachmentBinary_whenArtifactExists() throws Exception {
		byte[] bytes = new byte[] { 1, 2, 3, 4, 5 };
		PersistedArtifactFile artifactFile = new PersistedArtifactFile(701L, "report.txt", "text/plain", bytes);
		mockHierarchy(77L, 88L, 99L, 1701L, 3001L);
		when(artifactQueryService.findArtifactInPath(new ArtifactQueryService.PathArtifact(3001L, 701L)))
			.thenReturn(Optional.of(artifactFile));

		performGet("/testruns/77/88/99/browsers/1701/paths/3001/artifacts/701", MediaType.APPLICATION_OCTET_STREAM)
			.andExpect(status().isOk())
			.andExpect(content().contentType(MediaType.APPLICATION_OCTET_STREAM))
			.andExpect(header().string("Content-Disposition", containsString("attachment; filename=\"report.txt\"")))
			.andExpect(header().longValue("Content-Length", bytes.length))
			.andExpect(content().bytes(bytes));
	}

	@Test
	void downloadArtifactInTestPath_shouldReturnNotFound_whenMissing() throws Exception {
		mockHierarchy(77L, 88L, 99L, 1701L, 3001L);
		when(artifactQueryService.findArtifactInPath(new ArtifactQueryService.PathArtifact(3001L, 702L)))
			.thenReturn(Optional.empty());

		performGet("/testruns/77/88/99/browsers/1701/paths/3001/artifacts/702", MediaType.APPLICATION_OCTET_STREAM)
			.andExpect(status().isNotFound());
	}

	@Test
	void downloadActionResultScreenshotInTestPath_shouldReturnInlineBinary_whenScreenshotExists() throws Exception {
		byte[] bytes = new byte[] { 9, 8, 7 };
		PersistedArtifactFile screenshotFile = new PersistedArtifactFile(4101L, "action-result-4101.png", "image/png",
				bytes);
		mockHierarchy(77L, 88L, 99L, 1701L, 3001L);
		when(actionResultScreenshotQueryService
			.findInPath(new ActionResultScreenshotQueryService.PathActionScreenshot(3001L, 4101L)))
			.thenReturn(Optional.of(screenshotFile));

		performGet("/testruns/77/88/99/browsers/1701/paths/3001/actions/chain/screenshots/4101",
				MediaType.APPLICATION_OCTET_STREAM)
			.andExpect(status().isOk())
			.andExpect(content().contentType(MediaType.IMAGE_PNG))
			.andExpect(header().string("Content-Disposition",
					containsString("inline; filename=\"action-result-4101.png\"")))
			.andExpect(header().longValue("Content-Length", bytes.length))
			.andExpect(content().bytes(bytes));
	}

	@Test
	void downloadActionResultScreenshotInTestPath_shouldReturnNotFound_whenScreenshotMissing() throws Exception {
		mockHierarchy(77L, 88L, 99L, 1701L, 3001L);
		when(actionResultScreenshotQueryService
			.findInPath(new ActionResultScreenshotQueryService.PathActionScreenshot(3001L, 4101L)))
			.thenReturn(Optional.empty());

		performGet("/testruns/77/88/99/browsers/1701/paths/3001/actions/chain/screenshots/4101",
				MediaType.APPLICATION_OCTET_STREAM)
			.andExpect(status().isNotFound());
	}

	@Test
	void listArtifactsInTestPath_shouldReturnPagedResponseWithExpectedLinks() throws Exception {
		PersistedArtifact first = new PersistedArtifact(801L, 3001L, ArtifactType.SCREENSHOT, "a.png", "image/png", 10,
				"key-a");
		PersistedArtifact second = new PersistedArtifact(802L, 3001L, ArtifactType.FILE, "b.log", "text/plain", 12,
				"key-b");
		PagedResult<PersistedArtifact> pagedArtifacts = new PagedResult<>(List.of(first, second), 1, 2, 5);

		mockHierarchy(77L, 88L, 99L, 1701L, 3001L);
		when(artifactQueryService.listArtifacts(new ArtifactQueryService.PathArtifactPage(3001L, 1, 2)))
			.thenReturn(pagedArtifacts);

		ResultActions response = performGet("/testruns/77/88/99/browsers/1701/paths/3001/artifacts?page=1&size=2")
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.items[0].id").value(801))
			.andExpect(jsonPath("$.items[0].filename").value("a.png"))
			.andExpect(jsonPath("$.items[0].type").value("OTHER"))
			.andExpect(jsonPath("$.items[0]._links.self.href",
					containsString("/testruns/77/88/99/browsers/1701/paths/3001/artifacts/801")))
			.andExpect(jsonPath("$.items[0]._links.collection.href",
					containsString("/testruns/77/88/99/browsers/1701/paths/3001/artifacts")))
			.andExpect(jsonPath("$.items[1].id").value(802))
			.andExpect(jsonPath("$.items[1].filename").value("b.log"))
			.andExpect(jsonPath("$._links.self.href",
					containsString("/testruns/77/88/99/browsers/1701/paths/3001/artifacts")));

		expectPage(response, 1, 2, 5, 3);
		expectPagingLinks(response, 0, 2, 0, 2);
	}

	@Test
	void listArtifactsInTestPath_shouldUseDefaultPagingParameters_whenQueryParamsMissing() throws Exception {
		PersistedArtifact persisted = new PersistedArtifact(811L, 3001L, ArtifactType.FILE, "default.bin",
				"application/octet-stream", 5, "key-default");
		PagedResult<PersistedArtifact> pagedArtifacts = new PagedResult<>(List.of(persisted), DEFAULT_PAGE,
				DEFAULT_SIZE, 1);

		mockHierarchy(77L, 88L, 99L, 1701L, 3001L);
		when(artifactQueryService
			.listArtifacts(new ArtifactQueryService.PathArtifactPage(3001L, DEFAULT_PAGE, DEFAULT_SIZE)))
			.thenReturn(pagedArtifacts);

		ResultActions response = performGet("/testruns/77/88/99/browsers/1701/paths/3001/artifacts")
			.andExpect(status().isOk());

		expectPage(response, DEFAULT_PAGE, DEFAULT_SIZE, 1, 1);
		expectDefaultSelfPagingParams(response);
	}

	@Test
	void listArtifactsInTestPath_shouldReturnEmptyPageWithoutPrevAndNext() throws Exception {
		PagedResult<PersistedArtifact> emptyPage = new PagedResult<>(Collections.emptyList(), DEFAULT_PAGE,
				DEFAULT_SIZE, 0);

		mockHierarchy(77L, 88L, 99L, 1701L, 3001L);
		when(artifactQueryService
			.listArtifacts(new ArtifactQueryService.PathArtifactPage(3001L, DEFAULT_PAGE, DEFAULT_SIZE)))
			.thenReturn(emptyPage);

		ResultActions response = performGet("/testruns/77/88/99/browsers/1701/paths/3001/artifacts?page=0&size=20")
			.andExpect(status().isOk());

		expectEmptyItems(response);
		expectPage(response, DEFAULT_PAGE, DEFAULT_SIZE, 0, 0);
		expectPagingLinks(response, 0, 0, null, null);
	}

	private void mockHierarchy(Long applicationId, Long stageId, Long testRunId, Long testSetResultId,
			Long pathResultId) {
		when(testSetResultQueryService.getTestSetResult(testSetResultId))
			.thenReturn(Optional.of(new PersistedTestSetResult(testSetResultId, testRunId, applicationId, stageId, 0,
					Browser.CHROME, "plan", PersistedTestSetResult.NO_TEST_CASE_TIMESTAMP, Browser.CHROME)));
		when(testPathResultQueryService.getPathResult(pathResultId))
			.thenReturn(Optional.of(new PersistedTestPathResult(pathResultId, testSetResultId, "path")));
	}

}
