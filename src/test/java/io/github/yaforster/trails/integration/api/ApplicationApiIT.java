package io.github.yaforster.trails.integration.api;

import io.github.yaforster.trails.core.PagedResult;

import io.github.yaforster.trails.app.services.ApplicationDatabaseService;
import io.github.yaforster.trails.app.services.StageDatabaseService;
import io.github.yaforster.trails.core.definition.ApplicationDefinition;
import io.github.yaforster.trails.core.deletion.DeletionFailure;
import io.github.yaforster.trails.core.deletion.DeletionNotFound;
import io.github.yaforster.trails.core.deletion.DeletionSuccess;
import io.github.yaforster.trails.core.deletion.ErrorDetails;
import io.github.yaforster.trails.core.persisted.PersistedApplication;
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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class ApplicationApiIT extends RestApiIntegrationTestBase {

	@MockitoBean
	private ApplicationDatabaseService applicationDatabaseService;

	@MockitoBean
	private StageDatabaseService stageDatabaseService;

	@Test
	void listApplications_shouldReturnPagedResponseWithExpectedLinks() throws Exception {
		PersistedApplication first = new PersistedApplication(101L, "App One");
		PersistedApplication second = new PersistedApplication(102L, "App Two");
		PagedResult<PersistedApplication> pagedApplications = new PagedResult<>(List.of(first, second), 1, 2, 5);

		when(applicationDatabaseService.getApplications(new ApplicationDatabaseService.ApplicationPage(1, 2, false)))
			.thenReturn(pagedApplications);
		when(stageDatabaseService
			.findApplicationIdsWithStages(new StageDatabaseService.ApplicationsWithStages(List.of(101L, 102L))))
			.thenReturn(Set.of(101L));

		ResultActions response = performGet("/applications?page=1&size=2").andExpect(status().isOk())
			.andExpect(jsonPath("$.items[0].id").value(101))
			.andExpect(jsonPath("$.items[0].label").value("App One"))
			.andExpect(jsonPath("$.items[0]._links.self.href", containsString("/applications/101")))
			.andExpect(jsonPath("$.items[0]._links.stages.href", containsString("/applications/101/stages")))
			.andExpect(jsonPath("$.items[1].id").value(102))
			.andExpect(jsonPath("$.items[1].label").value("App Two"))
			.andExpect(jsonPath("$.items[1]._links.self.href", containsString("/applications/102")))
			.andExpect(jsonPath("$.items[1]._links.stages").doesNotExist())
			.andExpect(jsonPath("$._links.self.href", containsString("/applications")));

		expectPage(response, 1, 2, 5, 3);
		expectPagingLinks(response, 0, 2, 0, 2);
	}

	@Test
	void listApplications_shouldUseDefaultPagingParameters_whenQueryParamsMissing() throws Exception {
		PersistedApplication app = new PersistedApplication(201L, "Default Paging App");
		PagedResult<PersistedApplication> pagedApplications = new PagedResult<>(List.of(app), DEFAULT_PAGE,
				DEFAULT_SIZE, 1);

		when(applicationDatabaseService
			.getApplications(new ApplicationDatabaseService.ApplicationPage(DEFAULT_PAGE, DEFAULT_SIZE, false)))
			.thenReturn(pagedApplications);
		when(stageDatabaseService
			.findApplicationIdsWithStages(new StageDatabaseService.ApplicationsWithStages(List.of(201L))))
			.thenReturn(Set.of());

		ResultActions response = performGet("/applications").andExpect(status().isOk());

		expectPage(response, DEFAULT_PAGE, DEFAULT_SIZE, 1, 1);
		expectDefaultSelfPagingParams(response);
	}

	@Test
	void listApplications_shouldReturnEmptyPageWithoutPrevAndNext() throws Exception {
		PagedResult<PersistedApplication> emptyPage = new PagedResult<>(Collections.emptyList(), DEFAULT_PAGE,
				DEFAULT_SIZE, 0);
		when(applicationDatabaseService
			.getApplications(new ApplicationDatabaseService.ApplicationPage(DEFAULT_PAGE, DEFAULT_SIZE, false)))
			.thenReturn(emptyPage);

		ResultActions response = performGet("/applications?page=0&size=20").andExpect(status().isOk());

		expectEmptyItems(response);
		expectPage(response, DEFAULT_PAGE, DEFAULT_SIZE, 0, 0);
		expectPagingLinks(response, 0, 0, null, null);
	}

	@Test
	void getApplication_shouldReturnApplicationWithStagesLink_whenStagesExist() throws Exception {
		PersistedApplication persisted = new PersistedApplication(301L, "Has Stages");
		when(applicationDatabaseService.getApplication(new ApplicationDatabaseService.ApplicationDetails(301L, false)))
			.thenReturn(Optional.of(persisted));
		when(stageDatabaseService.hasStages(new StageDatabaseService.ApplicationReference(301L))).thenReturn(true);

		performGet("/applications/301").andExpect(status().isOk())
			.andExpect(jsonPath("$.id").value(301))
			.andExpect(jsonPath("$.label").value("Has Stages"))
			.andExpect(jsonPath("$._links.self.href", containsString("/applications/301")))
			.andExpect(jsonPath("$._links.collection.href", containsString("/applications")))
			.andExpect(jsonPath("$._links.stages.href", containsString("/applications/301/stages")));
	}

	@Test
	void getApplication_shouldReturnApplicationWithoutStagesLink_whenNoStagesExist() throws Exception {
		PersistedApplication persisted = new PersistedApplication(302L, "No Stages");
		when(applicationDatabaseService.getApplication(new ApplicationDatabaseService.ApplicationDetails(302L, false)))
			.thenReturn(Optional.of(persisted));
		when(stageDatabaseService.hasStages(new StageDatabaseService.ApplicationReference(302L))).thenReturn(false);

		performGet("/applications/302").andExpect(status().isOk())
			.andExpect(jsonPath("$.id").value(302))
			.andExpect(jsonPath("$.label").value("No Stages"))
			.andExpect(jsonPath("$._links.stages").doesNotExist());
	}

	@Test
	void getApplication_shouldReturnNotFound_whenMissing() throws Exception {
		when(applicationDatabaseService.getApplication(new ApplicationDatabaseService.ApplicationDetails(999L, false)))
			.thenReturn(Optional.empty());

		performGet("/applications/999").andExpect(status().isNotFound());

		verifyNoInteractions(stageDatabaseService);
	}

	@Test
	void createNewApplication_shouldMapDefinitionAndReturnPersistedDto() throws Exception {
		PersistedApplication persisted = new PersistedApplication(401L, "Created App");
		when(applicationDatabaseService.storeApplication(any(ApplicationDefinition.class))).thenReturn(persisted);

		performPutJson("/applications", """
				{
				  "label": "Created App"
				}
				""").andExpect(status().isOk())
			.andExpect(jsonPath("$.id").value(401))
			.andExpect(jsonPath("$.label").value("Created App"));

		ArgumentCaptor<ApplicationDefinition> captor = ArgumentCaptor.forClass(ApplicationDefinition.class);
		verify(applicationDatabaseService).storeApplication(captor.capture());
		assertEquals("Created App", captor.getValue().label());
	}

	@Test
	void createNewApplication_shouldReturnBadRequest_forMalformedJson() throws Exception {
		performPutJson("/applications", "{ not-valid-json }").andExpect(status().isBadRequest());

		verifyNoInteractions(applicationDatabaseService, stageDatabaseService);
	}

	@Test
	void createNewApplication_shouldReturnValidationError_whenLabelAlreadyExists() throws Exception {
		when(applicationDatabaseService.existsByLabel("Created App")).thenReturn(true);

		performPutJson("/applications", """
				{
				  "label": "Created App"
				}
				""", MediaType.APPLICATION_PROBLEM_JSON).andExpect(status().isBadRequest())
			.andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
			.andExpect(jsonPath("$[0].code").value("APPLICATION_LABEL_ALREADY_EXISTS"));

		verify(applicationDatabaseService, never()).storeApplication(any(ApplicationDefinition.class));
	}

	@Test
	void createNewApplication_shouldReturnValidationError_whenAcceptingProblemJson() throws Exception {
		performPutJson("/applications", """
				{
				  "label": " "
				}
				""", MediaType.APPLICATION_PROBLEM_JSON).andExpect(status().isBadRequest())
			.andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
			.andExpect(jsonPath("$[0].code").value("APPLICATION_LABEL_EMPTY"));

		verifyNoInteractions(applicationDatabaseService, stageDatabaseService);
	}

	@Test
	void deleteApplication_shouldReturnOk_forDeletionSuccess() throws Exception {
		when(applicationDatabaseService.deleteApplication(501L)).thenReturn(new DeletionSuccess(501L));

		performDelete("/applications/501").andExpect(status().isOk())
			.andExpect(jsonPath("$.deletionRequestedForID").value(501))
			.andExpect(jsonPath("$.deletionResult").value(true));
	}

	@Test
	void deleteApplication_shouldReturnNotFound_forDeletionNotFound() throws Exception {
		when(applicationDatabaseService.deleteApplication(502L)).thenReturn(new DeletionNotFound(502L));

		performDelete("/applications/502").andExpect(status().isNotFound())
			.andExpect(jsonPath("$.deletionRequestedForID").value(502))
			.andExpect(jsonPath("$.deletionResult").value(false))
			.andExpect(jsonPath("$._links.collection.href", containsString("/applications")));
	}

	@Test
	void deleteApplication_shouldReturnConflict_forDeletionConflict() throws Exception {
		when(applicationDatabaseService.deleteApplication(503L))
			.thenReturn(new DeletionFailure(503L, "conflict", null));

		performDelete("/applications/503").andExpect(status().isConflict())
			.andExpect(jsonPath("$.deletionRequestedForID").value(503))
			.andExpect(jsonPath("$.deletionResult").value(false));
	}

	@Test
	void deleteApplication_shouldReturnInternalServerError_forDeletionFailure() throws Exception {
		DeletionFailure failure = new DeletionFailure(504L, "boom",
				new ErrorDetails("RuntimeException", "boom", "stacktrace"));
		when(applicationDatabaseService.deleteApplication(504L)).thenReturn(failure);

		performDelete("/applications/504").andExpect(status().isInternalServerError())
			.andExpect(jsonPath("$.deletionRequestedForID").value(504))
			.andExpect(jsonPath("$.deletionResult").value(false))
			.andExpect(jsonPath("$.message").value("boom"));
	}

}
