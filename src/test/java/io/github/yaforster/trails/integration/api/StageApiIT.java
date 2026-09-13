package io.github.yaforster.trails.integration.api;

import io.github.yaforster.trails.core.PagedResult;

import io.github.yaforster.trails.app.services.ElementDatabaseService;
import io.github.yaforster.trails.app.services.StageDatabaseService;
import io.github.yaforster.trails.core.definition.StageDefinition;
import io.github.yaforster.trails.core.deletion.DeletionFailure;
import io.github.yaforster.trails.core.deletion.DeletionNotFound;
import io.github.yaforster.trails.core.deletion.DeletionSuccess;
import io.github.yaforster.trails.core.deletion.ErrorDetails;
import io.github.yaforster.trails.core.persisted.PersistedStage;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.ResultActions;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.hamcrest.Matchers.containsString;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class StageApiIT extends RestApiIntegrationTestBase {

	@MockitoBean
	private StageDatabaseService stageDatabaseService;

	@MockitoBean
	private ElementDatabaseService elementDatabaseService;

	@Test
	void createNewStage_shouldMapDefinitionAndReturnPersistedDto() throws Exception {
		PersistedStage persistedStage = new PersistedStage(501L, 77L, "Checkout", "https://example.org/checkout");
		when(stageDatabaseService.storeStage(any(StageDatabaseService.StageCreation.class))).thenReturn(persistedStage);

		performPutJson("/applications/77/stages", """
				{
				  "label": "Checkout",
				  "url": "https://example.org/checkout"
				}
				""").andExpect(status().isOk())
			.andExpect(jsonPath("$.id").value(501))
			.andExpect(jsonPath("$.label").value("Checkout"))
			.andExpect(jsonPath("$.url").value("https://example.org/checkout"))
			.andExpect(jsonPath("$._links.testPlans.href", containsString("/applications/77/stages/501/testPlans")))
			.andExpect(jsonPath("$._links.testRuns.href", containsString("/applications/77/stages/501/testruns")));

		ArgumentCaptor<StageDatabaseService.StageCreation> captor = ArgumentCaptor
			.forClass(StageDatabaseService.StageCreation.class);
		verify(stageDatabaseService).storeStage(captor.capture());
		assertEquals("Checkout", captor.getValue().stageDefinition().label());
		assertEquals("https://example.org/checkout", captor.getValue().stageDefinition().url());
		assertTrue(captor.getValue().stageDefinition().availableElements().isEmpty());
	}

	@Test
	void createNewStage_shouldReturnBadRequest_forMalformedJson() throws Exception {
		performPutJson("/applications/77/stages", "{ invalid-json }").andExpect(status().isBadRequest());

		verifyNoInteractions(stageDatabaseService, elementDatabaseService);
	}

	@Test
	void createNewStage_shouldReturnValidationErrors_whenLabelAndUrlAlreadyExist() throws Exception {
		when(stageDatabaseService.existsByLabel(new StageDatabaseService.StageLabel(77L, "Checkout"))).thenReturn(true);
		when(stageDatabaseService.existsByUrl(new StageDatabaseService.StageUrl(77L, "https://example.org/checkout")))
			.thenReturn(true);

		performPutJson("/applications/77/stages", """
				{
				  "label": "Checkout",
				  "url": "https://example.org/checkout"
				}
				""", MediaType.APPLICATION_PROBLEM_JSON).andExpect(status().isBadRequest())
			.andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
			.andExpect(jsonPath("$[0].code").value("STAGE_LABEL_ALREADY_EXISTS"))
			.andExpect(jsonPath("$[1].code").value("STAGE_URL_ALREADY_EXISTS"));

		verify(stageDatabaseService, never()).storeStage(any());
	}

	@Test
	void createNewStage_shouldReturnValidationErrors_whenAcceptingProblemJson() throws Exception {
		performPutJson("/applications/77/stages", """
				{
				  "label": " "
				}
				""", MediaType.APPLICATION_PROBLEM_JSON).andExpect(status().isBadRequest())
			.andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
			.andExpect(jsonPath("$[0].code").value("STAGE_LABEL_EMPTY"))
			.andExpect(jsonPath("$[1].code").value("STAGE_URL_EMPTY"));

		verifyNoInteractions(stageDatabaseService, elementDatabaseService);
	}

	@Test
	void listStages_shouldReturnPagedResponseWithExpectedLinks() throws Exception {
		PersistedStage first = new PersistedStage(601L, 77L, "Login", "https://example.org/login");
		PersistedStage second = new PersistedStage(602L, 77L, "Checkout", "https://example.org/checkout");
		PagedResult<PersistedStage> pagedStages = new PagedResult<>(List.of(first, second), 1, 2, 5);

		when(stageDatabaseService.getStages(new StageDatabaseService.StagePage(77L, 1, 2, false)))
			.thenReturn(pagedStages);
		when(elementDatabaseService
			.findStageIdsWithElements(new ElementDatabaseService.CandidateStages(77L, List.of(601L, 602L))))
			.thenReturn(Set.of(601L));

		ResultActions response = performGet("/applications/77/stages?page=1&size=2").andExpect(status().isOk())
			.andExpect(jsonPath("$.items[0].id").value(601))
			.andExpect(jsonPath("$.items[0].label").value("Login"))
			.andExpect(jsonPath("$.items[0]._links.self.href", containsString("/applications/77/stages/601")))
			.andExpect(
					jsonPath("$.items[0]._links.elements.href", containsString("/applications/77/stages/601/elements")))
			.andExpect(jsonPath("$.items[0]._links.testPlans.href",
					containsString("/applications/77/stages/601/testPlans")))
			.andExpect(
					jsonPath("$.items[0]._links.testRuns.href", containsString("/applications/77/stages/601/testruns")))
			.andExpect(jsonPath("$.items[1].id").value(602))
			.andExpect(jsonPath("$.items[1].label").value("Checkout"))
			.andExpect(jsonPath("$.items[1]._links.self.href", containsString("/applications/77/stages/602")))
			.andExpect(jsonPath("$.items[1]._links.testPlans.href",
					containsString("/applications/77/stages/602/testPlans")))
			.andExpect(
					jsonPath("$.items[1]._links.testRuns.href", containsString("/applications/77/stages/602/testruns")))
			.andExpect(jsonPath("$.items[1]._links.elements").doesNotExist())
			.andExpect(jsonPath("$._links.self.href", containsString("/applications/77/stages")));

		expectPage(response, 1, 2, 5, 3);
		expectPagingLinks(response, 0, 2, 0, 2);
	}

	@Test
	void listStages_shouldUseDefaultPagingParameters_whenQueryParamsMissing() throws Exception {
		PersistedStage stage = new PersistedStage(611L, 77L, "Default", "https://example.org/default");
		PagedResult<PersistedStage> pagedStages = new PagedResult<>(List.of(stage), DEFAULT_PAGE, DEFAULT_SIZE, 1);

		when(stageDatabaseService.getStages(new StageDatabaseService.StagePage(77L, DEFAULT_PAGE, DEFAULT_SIZE, false)))
			.thenReturn(pagedStages);
		when(elementDatabaseService
			.findStageIdsWithElements(new ElementDatabaseService.CandidateStages(77L, List.of(611L))))
			.thenReturn(Set.of());

		ResultActions response = performGet("/applications/77/stages").andExpect(status().isOk());

		expectPage(response, DEFAULT_PAGE, DEFAULT_SIZE, 1, 1);
		expectDefaultSelfPagingParams(response);
	}

	@Test
	void listStages_shouldReturnEmptyPageWithoutPrevAndNext() throws Exception {
		PagedResult<PersistedStage> emptyPage = new PagedResult<>(Collections.emptyList(), DEFAULT_PAGE, DEFAULT_SIZE,
				0);

		when(stageDatabaseService.getStages(new StageDatabaseService.StagePage(77L, DEFAULT_PAGE, DEFAULT_SIZE, false)))
			.thenReturn(emptyPage);

		ResultActions response = performGet("/applications/77/stages?page=0&size=20").andExpect(status().isOk());

		expectEmptyItems(response);
		expectPage(response, DEFAULT_PAGE, DEFAULT_SIZE, 0, 0);
		expectPagingLinks(response, 0, 0, null, null);
	}

	@Test
	void getStage_shouldReturnStageWithElementsLink_whenElementsExist() throws Exception {
		PersistedStage persistedStage = new PersistedStage(701L, 77L, "Details", "https://example.org/details");
		when(stageDatabaseService.getStage(new StageDatabaseService.StageDetails(77L, 701L, false)))
			.thenReturn(Optional.of(persistedStage));
		when(elementDatabaseService.hasElements(new ElementDatabaseService.StageReference(77L, 701L))).thenReturn(true);

		performGet("/applications/77/stages/701").andExpect(status().isOk())
			.andExpect(jsonPath("$.id").value(701))
			.andExpect(jsonPath("$.label").value("Details"))
			.andExpect(jsonPath("$.url").value("https://example.org/details"))
			.andExpect(jsonPath("$._links.self.href", containsString("/applications/77/stages/701")))
			.andExpect(jsonPath("$._links.collection.href", containsString("/applications/77/stages")))
			.andExpect(jsonPath("$._links.elements.href", containsString("/applications/77/stages/701/elements")))
			.andExpect(jsonPath("$._links.testPlans.href", containsString("/applications/77/stages/701/testPlans")))
			.andExpect(jsonPath("$._links.testRuns.href", containsString("/applications/77/stages/701/testruns")));
	}

	@Test
	void deleteStage_shouldReturnOk_forDeletionSuccess() throws Exception {
		when(stageDatabaseService.deleteStage(new StageDatabaseService.StageReference(77L, 701L)))
			.thenReturn(new DeletionSuccess(701L));

		performDelete("/applications/77/stages/701").andExpect(status().isOk())
			.andExpect(jsonPath("$.deletionResult").value(true))
			.andExpect(jsonPath("$.deletionRequestedForID").value(701));
	}

	@Test
	void deleteStage_shouldReturnNotFound_forDeletionNotFound() throws Exception {
		when(stageDatabaseService.deleteStage(new StageDatabaseService.StageReference(77L, 702L)))
			.thenReturn(new DeletionNotFound(702L));

		performDelete("/applications/77/stages/702").andExpect(status().isNotFound())
			.andExpect(jsonPath("$.deletionResult").value(false))
			.andExpect(jsonPath("$.deletionRequestedForID").value(702))
			.andExpect(jsonPath("$._links.collection.href", containsString("/applications/77/stages")));
	}

	@Test
	void deleteStage_shouldReturnConflict_forDeletionConflict() throws Exception {
		when(stageDatabaseService.deleteStage(new StageDatabaseService.StageReference(77L, 703L)))
			.thenReturn(new DeletionFailure(703L, "conflict", null));

		performDelete("/applications/77/stages/703").andExpect(status().isConflict())
			.andExpect(jsonPath("$.deletionResult").value(false))
			.andExpect(jsonPath("$.deletionRequestedForID").value(703));
	}

	@Test
	void deleteStage_shouldReturnInternalServerError_forDeletionFailure() throws Exception {
		DeletionFailure failure = new DeletionFailure(704L, "boom",
				new ErrorDetails("RuntimeException", "boom", "trace"));
		when(stageDatabaseService.deleteStage(new StageDatabaseService.StageReference(77L, 704L))).thenReturn(failure);

		performDelete("/applications/77/stages/704").andExpect(status().isInternalServerError())
			.andExpect(jsonPath("$.deletionResult").value(false))
			.andExpect(jsonPath("$.deletionRequestedForID").value(704));
	}

	@Test
	void getStage_shouldReturnStageWithoutElementsLink_whenNoElementsExist() throws Exception {
		PersistedStage persistedStage = new PersistedStage(702L, 77L, "Summary", "https://example.org/summary");
		when(stageDatabaseService.getStage(new StageDatabaseService.StageDetails(77L, 702L, false)))
			.thenReturn(Optional.of(persistedStage));
		when(elementDatabaseService.hasElements(new ElementDatabaseService.StageReference(77L, 702L)))
			.thenReturn(false);

		performGet("/applications/77/stages/702").andExpect(status().isOk())
			.andExpect(jsonPath("$.id").value(702))
			.andExpect(jsonPath("$.label").value("Summary"))
			.andExpect(jsonPath("$._links.testPlans.href", containsString("/applications/77/stages/702/testPlans")))
			.andExpect(jsonPath("$._links.testRuns.href", containsString("/applications/77/stages/702/testruns")))
			.andExpect(jsonPath("$._links.elements").doesNotExist());
	}

	@Test
	void getStage_shouldReturnNotFound_whenMissing() throws Exception {
		when(stageDatabaseService.getStage(new StageDatabaseService.StageDetails(77L, 999L, false)))
			.thenReturn(Optional.empty());

		performGet("/applications/77/stages/999").andExpect(status().isNotFound());

		verifyNoInteractions(elementDatabaseService);
	}

}
