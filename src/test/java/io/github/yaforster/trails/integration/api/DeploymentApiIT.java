package io.github.yaforster.trails.integration.api;

import io.github.yaforster.trails.core.PagedResult;

import io.github.yaforster.trails.app.services.DeploymentDatabaseService;
import io.github.yaforster.trails.core.persisted.PersistedDeployment;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.ResultActions;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.hamcrest.Matchers.containsString;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class DeploymentApiIT extends RestApiIntegrationTestBase {

	@MockitoBean
	private DeploymentDatabaseService deploymentDatabaseService;

	@Test
	void listDeployments_shouldReturnPagedDeploymentResources() throws Exception {
		Instant deployedAt = Instant.parse("2026-05-24T12:15:30Z");
		PersistedDeployment deployment = new PersistedDeployment(55L, 7L, 9L, "1.2.3", deployedAt);
		when(deploymentDatabaseService
			.listDeployments(new DeploymentDatabaseService.StageDeploymentPage(7L, 9L, 0, 20)))
			.thenReturn(Optional.of(new PagedResult<>(List.of(deployment), 0, 20, 1)));

		ResultActions actions = performGet("/applications/7/stages/9/deployments");

		expectPage(actions, 0, 20, 1, 1).andExpect(status().isOk())
			.andExpect(jsonPath("$.items[0].id").value(55))
			.andExpect(jsonPath("$.items[0].applicationId").value(7))
			.andExpect(jsonPath("$.items[0].stageId").value(9))
			.andExpect(jsonPath("$.items[0].version").value("1.2.3"))
			.andExpect(jsonPath("$.items[0].deployedAt").value("2026-05-24T12:15:30Z"))
			.andExpect(jsonPath("$.items[0]._links.self.href",
					containsString("/api/applications/7/stages/9/deployments/55")))
			.andExpect(jsonPath("$._links.create.href", containsString("/api/applications/7/stages/9/deployments")));
		expectDefaultSelfPagingParams(actions);
		expectPagingLinks(actions, 0, 0, null, null);
	}

	@Test
	void getDeployment_shouldReturnDeploymentResource() throws Exception {
		Instant deployedAt = Instant.parse("2026-05-24T12:15:30Z");
		PersistedDeployment deployment = new PersistedDeployment(55L, 7L, 9L, "1.2.3", deployedAt);
		when(deploymentDatabaseService.getDeployment(new DeploymentDatabaseService.DeploymentDetails(7L, 9L, 55L)))
			.thenReturn(Optional.of(deployment));

		performGet("/applications/7/stages/9/deployments/55").andExpect(status().isOk())
			.andExpect(jsonPath("$.id").value(55))
			.andExpect(jsonPath("$.applicationId").value(7))
			.andExpect(jsonPath("$.stageId").value(9))
			.andExpect(jsonPath("$.version").value("1.2.3"))
			.andExpect(jsonPath("$.deployedAt").value("2026-05-24T12:15:30Z"))
			.andExpect(
					jsonPath("$._links.collection.href", containsString("/api/applications/7/stages/9/deployments")));
	}

	@Test
	void reportDeployment_shouldStoreDeploymentAndReturnCreatedResource() throws Exception {
		Instant deployedAt = Instant.parse("2026-05-24T12:15:30Z");
		PersistedDeployment persisted = new PersistedDeployment(55L, 7L, 9L, "1.2.3", deployedAt);
		when(deploymentDatabaseService.storeDeployment(org.mockito.ArgumentMatchers.any()))
			.thenReturn(Optional.of(persisted));

		performPutJson("/applications/7/stages/9/deployments", """
				{
				  "version": "1.2.3",
				  "deployedAt": "2026-05-24T12:15:30Z"
				}
				""").andExpect(status().isCreated())
			.andExpect(header().string("Location", containsString("/api/applications/7/stages/9/deployments/55")))
			.andExpect(jsonPath("$.id").value(55))
			.andExpect(jsonPath("$.applicationId").value(7))
			.andExpect(jsonPath("$.stageId").value(9))
			.andExpect(jsonPath("$.version").value("1.2.3"))
			.andExpect(jsonPath("$.deployedAt").value("2026-05-24T12:15:30Z"))
			.andExpect(
					jsonPath("$._links.collection.href", containsString("/api/applications/7/stages/9/deployments")));

		ArgumentCaptor<DeploymentDatabaseService.StageDeployment> captor = ArgumentCaptor
			.forClass(DeploymentDatabaseService.StageDeployment.class);
		verify(deploymentDatabaseService).storeDeployment(captor.capture());
		assertEquals("1.2.3", captor.getValue().definition().version());
		assertEquals(deployedAt, captor.getValue().definition().deployedAt());
	}

	@Test
	void reportDeployment_shouldReturnNotFound_whenApplicationStageCombinationIsMissing() throws Exception {
		when(deploymentDatabaseService.storeDeployment(org.mockito.ArgumentMatchers.any()))
			.thenReturn(Optional.empty());

		performPutJson("/applications/7/stages/99/deployments", """
				{
				  "version": "1.2.3",
				  "deployedAt": "2026-05-24T12:15:30Z"
				}
				""").andExpect(status().isNotFound());
	}

	@Test
	void reportDeployment_shouldReturnValidationErrors_whenVersionIsBlank() throws Exception {
		performPutJson("/applications/7/stages/9/deployments", """
				{
				  "version": " ",
				  "deployedAt": "2026-05-24T12:15:30Z"
				}
				""", MediaType.APPLICATION_PROBLEM_JSON).andExpect(status().isBadRequest())
			.andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
			.andExpect(jsonPath("$[0].code").value("DEPLOYMENT_VERSION_EMPTY"));

		verifyNoInteractions(deploymentDatabaseService);
	}

}
