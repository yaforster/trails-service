package io.github.yaforster.trails.integration.api;

import io.github.yaforster.trails.core.PagedResult;

import io.github.yaforster.trails.app.services.ActionQueryService;
import io.github.yaforster.trails.core.persisted.PersistedAction;
import io.github.yaforster.trails.core.test.Position;
import io.github.yaforster.trails.core.test.action.value.FixedValueInstruction;
import io.github.yaforster.trails.core.test.action.browser.SwitchWebsiteAction;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.ResultActions;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class ActionApiIT extends RestApiIntegrationTestBase {

	@MockitoBean
	private ActionQueryService actionQueryService;

	private static SwitchWebsiteAction switchWebsiteAction(Long actionId, String label) {
		return SwitchWebsiteAction.builder()
			.actionID(actionId)
			.label(label)
			.nextActions(List.of())
			.position(new Position(BigDecimal.TEN, BigDecimal.ONE))
			.computationInstruction(new FixedValueInstruction("https://example.org/target"))
			.build();
	}

	@Test
	void getActions_shouldReturnPagedResponseWithExpectedLinks() throws Exception {
		PersistedAction first = new PersistedAction(1001L, 99L, 701L, switchWebsiteAction(1001L, "Open Checkout"));
		PersistedAction second = new PersistedAction(1002L, 99L, 702L, switchWebsiteAction(1002L, "Open Confirmation"));
		PagedResult<PersistedAction> pagedActions = new PagedResult<>(List.of(first, second), 1, 2, 5);

		when(actionQueryService.listActions(new ActionQueryService.ActionPage(77L, 88L, 99L, 1, 2)))
			.thenReturn(Optional.of(pagedActions));

		ResultActions response = performGet("/applications/77/stages/88/testPlans/99/actions?page=1&size=2")
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.items[0].actionID").value(1001))
			.andExpect(jsonPath("$.items[0].referenceID").value(701))
			.andExpect(jsonPath("$.items[0].label").value("Open Checkout"))
			.andExpect(jsonPath("$.items[0].details.detailsType").value("SWITCH_WEBSITE"))
			.andExpect(jsonPath("$.items[0].details.valueComputation.computation").value("FIXED"))
			.andExpect(jsonPath("$.items[0].details.valueComputation.value").value("https://example.org/target"))
			.andExpect(jsonPath("$.items[1].actionID").value(1002))
			.andExpect(jsonPath("$.items[1].referenceID").value(702))
			.andExpect(jsonPath("$.items[1].label").value("Open Confirmation"))
			.andExpect(
					jsonPath("$._links.self.href", containsString("/applications/77/stages/88/testPlans/99/actions")))
			.andExpect(jsonPath("$._links.testPlan.href", containsString("/applications/77/stages/88/testPlans/99")));

		expectPage(response, 1, 2, 5, 3);
		expectPagingLinks(response, 0, 2, 0, 2);
	}

	@Test
	void getActions_shouldUseDefaultPagingParameters_whenQueryParamsMissing() throws Exception {
		PersistedAction persistedAction = new PersistedAction(1011L, 99L, 711L,
				switchWebsiteAction(1011L, "Default Action"));
		PagedResult<PersistedAction> pagedActions = new PagedResult<>(List.of(persistedAction), DEFAULT_PAGE,
				DEFAULT_SIZE, 1);

		when(actionQueryService
			.listActions(new ActionQueryService.ActionPage(77L, 88L, 99L, DEFAULT_PAGE, DEFAULT_SIZE)))
			.thenReturn(Optional.of(pagedActions));

		ResultActions response = performGet("/applications/77/stages/88/testPlans/99/actions")
			.andExpect(status().isOk());

		expectPage(response, DEFAULT_PAGE, DEFAULT_SIZE, 1, 1);
		expectDefaultSelfPagingParams(response);
	}

	@Test
	void getActions_shouldReturnEmptyPageWithoutPrevAndNext() throws Exception {
		PagedResult<PersistedAction> emptyPage = new PagedResult<>(Collections.emptyList(), DEFAULT_PAGE, DEFAULT_SIZE,
				0);

		when(actionQueryService
			.listActions(new ActionQueryService.ActionPage(77L, 88L, 99L, DEFAULT_PAGE, DEFAULT_SIZE)))
			.thenReturn(Optional.of(emptyPage));

		ResultActions response = performGet("/applications/77/stages/88/testPlans/99/actions?page=0&size=20")
			.andExpect(status().isOk());

		expectEmptyItems(response);
		expectPage(response, DEFAULT_PAGE, DEFAULT_SIZE, 0, 0);
		expectPagingLinks(response, 0, 0, null, null);
	}

	@Test
	void getActions_shouldReturnNotFound_whenQueryServiceReturnsEmpty() throws Exception {
		when(actionQueryService
			.listActions(new ActionQueryService.ActionPage(77L, 88L, 99L, DEFAULT_PAGE, DEFAULT_SIZE)))
			.thenReturn(Optional.empty());

		performGet("/applications/77/stages/88/testPlans/99/actions").andExpect(status().isNotFound());
	}

}
