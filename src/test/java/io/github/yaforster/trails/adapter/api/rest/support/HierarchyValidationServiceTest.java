package io.github.yaforster.trails.adapter.api.rest.support;

import io.github.yaforster.trails.adapter.api.rest.validation.HierarchyValidationServiceImpl;
import io.github.yaforster.trails.app.services.HierarchyValidationService;
import io.github.yaforster.trails.app.services.TestPathResultQueryService;
import io.github.yaforster.trails.app.services.TestSetResultQueryService;
import io.github.yaforster.trails.core.persisted.PersistedTestPathResult;
import io.github.yaforster.trails.core.persisted.PersistedTestSetResult;
import io.github.yaforster.trails.core.test.Browser;
import org.junit.jupiter.api.Test;

import java.time.OffsetDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

class HierarchyValidationServiceTest {

	private final TestSetResultQueryService testSetResultQueryService = mock(TestSetResultQueryService.class);

	private final TestPathResultQueryService testPathResultQueryService = mock(TestPathResultQueryService.class);

	private final HierarchyValidationServiceImpl service = new HierarchyValidationServiceImpl(testSetResultQueryService,
			testPathResultQueryService);

	@Test
	void testSetBelongsToRun_shouldReturnTrue_whenRunHierarchyMatches() {
		when(testSetResultQueryService.getTestSetResult(4L)).thenReturn(Optional.of(persistedSet(4L, 3L, 1L, 2L)));

		boolean result = service.testSetBelongsToRun(new HierarchyValidationService.TestSetHierarchy(1L, 2L, 3L, 4L));

		assertTrue(result);
	}

	@Test
	void pathBelongsToHierarchy_shouldReturnFalse_whenTestSetDoesNotBelongToRun() {
		when(testSetResultQueryService.getTestSetResult(4L)).thenReturn(Optional.empty());

		boolean result = service
			.pathBelongsToHierarchy(new HierarchyValidationService.TestPathHierarchy(1L, 2L, 3L, 4L, 9L));

		assertFalse(result);
		verifyNoInteractions(testPathResultQueryService);
	}

	@Test
	void pathBelongsToHierarchy_shouldReturnTrue_whenHierarchyMatches() {
		when(testSetResultQueryService.getTestSetResult(4L)).thenReturn(Optional.of(persistedSet(4L, 3L, 1L, 2L)));
		when(testPathResultQueryService.getPathResult(9L))
			.thenReturn(Optional.of(new PersistedTestPathResult(9L, 4L, "path")));

		boolean result = service
			.pathBelongsToHierarchy(new HierarchyValidationService.TestPathHierarchy(1L, 2L, 3L, 4L, 9L));

		assertTrue(result);
	}

	private PersistedTestSetResult persistedSet(Long id, Long testRunId, Long applicationId, Long stageId) {
		return new PersistedTestSetResult(id, testRunId, applicationId, stageId, 1, Browser.CHROME, "plan",
				OffsetDateTime.parse("2026-01-01T00:00:00Z"), Browser.CHROME);
	}

}
