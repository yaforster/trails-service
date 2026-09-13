package io.github.yaforster.trails.integration.api;

import io.github.yaforster.trails.app.services.ActionResultPersistence;
import io.github.yaforster.trails.app.services.ActionResultQueryService;
import io.github.yaforster.trails.app.services.ActionResultScreenshotQueryService;
import io.github.yaforster.trails.app.services.TestPathResultQueryService;
import io.github.yaforster.trails.app.services.TestSetResultQueryService;
import io.github.yaforster.trails.core.persisted.*;
import io.github.yaforster.trails.core.test.Browser;
import org.junit.jupiter.api.Test;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.util.List;
import java.util.Optional;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class ActionResultApiIT extends RestApiIntegrationTestBase {

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

	private static PersistedTestSetResult set(Long id, Long testRunId, Long applicationId, Long stageId) {
		return new PersistedTestSetResult(id, testRunId, applicationId, stageId, 42, Browser.CHROME, "plan",
				PersistedTestSetResult.NO_TEST_CASE_TIMESTAMP, Browser.FIREFOX);
	}

	@Test
	void listActionResultsChainInTestPath_shouldReturnOrderedItems() throws Exception {
		PersistedActionResult first = new PersistedActionResult(4101L, 2201L, 1001L, "first", "ok",
				ActionResultType.SUCCESS, null);
		PersistedActionResult second = new PersistedActionResult(4102L, 2201L, 1002L, "second", "ok",
				ActionResultType.SUCCESS, null);
		when(testSetResultQueryService.getTestSetResult(1701L)).thenReturn(Optional.of(set(1701L, 99L, 77L, 88L)));
		when(testPathResultQueryService.getPathResult(2201L))
			.thenReturn(Optional.of(new PersistedTestPathResult(2201L, 1701L, "path")));
		when(actionResultQueryService.listActionResultChain(2201L)).thenReturn(Optional.of(List.of(first, second)));
		when(actionResultScreenshotQueryService
			.findInPath(new ActionResultScreenshotQueryService.PathActionScreenshot(2201L, 4101L)))
			.thenReturn(Optional.of(new PersistedArtifactFile(1L, "shot.png", "image/png", new byte[] { 1 })));
		when(actionResultScreenshotQueryService
			.findInPath(new ActionResultScreenshotQueryService.PathActionScreenshot(2201L, 4102L)))
			.thenReturn(Optional.empty());

		performGet("/testruns/77/88/99/browsers/1701/paths/2201/actions/chain").andExpect(status().isOk())
			.andExpect(jsonPath("$.items[0].id").value(4101))
			.andExpect(jsonPath("$.items[1].id").value(4102))
			.andExpect(jsonPath("$.items[0]._links.screenshot.href",
					containsString("/testruns/77/88/99/browsers/1701/paths/2201/actions/chain/screenshots/4101")))
			.andExpect(jsonPath("$.items[1]._links.screenshot").doesNotExist())
			.andExpect(jsonPath("$._links.self.href",
					containsString("/testruns/77/88/99/browsers/1701/paths/2201/actions/chain")))
			.andExpect(jsonPath("$._links.pathResult.href",
					containsString("/testruns/77/88/99/browsers/1701/paths/2201")));
	}

	@Test
	void listActionResultsChainInTestPath_shouldReturnNotFound_whenPathMissing() throws Exception {
		when(testSetResultQueryService.getTestSetResult(1701L)).thenReturn(Optional.of(set(1701L, 99L, 77L, 88L)));
		when(testPathResultQueryService.getPathResult(2202L)).thenReturn(Optional.empty());

		performGet("/testruns/77/88/99/browsers/1701/paths/2202/actions/chain").andExpect(status().isNotFound());
	}

}
