package io.github.yaforster.trails.adapter.api.rest.actionresult;

import io.github.yaforster.trails.adapter.api.rest.actionresult.assembler.ActionResultModelAssembler;
import io.github.yaforster.trails.adapter.api.rest.actionresult.assembler.PersistedActionResultContext;
import io.github.yaforster.trails.adapter.api.rest.actionresult.mapper.PagedActionResultMapper;
import io.github.yaforster.trails.adapter.api.rest.actionresult.model.ActionResultModel;
import io.github.yaforster.trails.adapter.api.rest.model.ActionResultChainDTO;
import io.github.yaforster.trails.adapter.api.rest.model.ActionResultDTO;
import io.github.yaforster.trails.app.services.ActionResultScreenshotQueryService;
import io.github.yaforster.trails.core.persisted.ActionResultType;
import io.github.yaforster.trails.core.persisted.PersistedActionResult;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ActionResultHATEOASFacadeTest {

	private final ActionResultModelAssembler assembler = mock(ActionResultModelAssembler.class);

	private final PagedActionResultMapper pagedActionResultMapper = mock(PagedActionResultMapper.class);

	private final ActionResultScreenshotQueryService actionResultScreenshotQueryService = mock(
			ActionResultScreenshotQueryService.class);

	private final ActionResultHATEOASFacade facade = new ActionResultHATEOASFacade(assembler, pagedActionResultMapper,
			actionResultScreenshotQueryService);

	@Test
	void toChainDTOInHierarchy_shouldPreserveInputOrderAndExposeHierarchyLinks() {
		PersistedActionResult first = new PersistedActionResult(9L, 100L, 7L, "first", "ok", ActionResultType.SUCCESS,
				null);
		PersistedActionResult second = new PersistedActionResult(10L, 100L, 8L, "second", "ok",
				ActionResultType.SUCCESS, null);

		ActionResultModel firstModel = new ActionResultModel(9L, 7L, "first", "ok", ActionResultType.SUCCESS, null);
		ActionResultModel secondModel = new ActionResultModel(10L, 8L, "second", "ok", ActionResultType.SUCCESS, null);
		when(assembler.toModel(any(PersistedActionResultContext.class))).thenAnswer(invocation -> {
			PersistedActionResultContext context = invocation.getArgument(0);
			return context.persistedActionResult().id().equals(9L) ? firstModel : secondModel;
		});
		when(pagedActionResultMapper.toActionResultDto(any(ActionResultModel.class))).thenAnswer(
				invocation -> new ActionResultDTO().id(((ActionResultModel) invocation.getArgument(0)).getId()));
		when(actionResultScreenshotQueryService
			.findInPath(any(ActionResultScreenshotQueryService.PathActionScreenshot.class)))
			.thenReturn(Optional.empty());

		ActionResultChainDTO result = facade.toChainDTOInHierarchy(1L, 2L, 3L, 4L, 100L, List.of(first, second));

		assertEquals(List.of(9L, 10L), result.getItems().stream().map(ActionResultDTO::getId).toList());
		assertTrue(
				result.getLinks().get("self").getHref().contains("/testruns/1/2/3/browsers/4/paths/100/actions/chain"));
		assertTrue(result.getLinks().get("pathResult").getHref().contains("/testruns/1/2/3/browsers/4/paths/100"));
	}

}
