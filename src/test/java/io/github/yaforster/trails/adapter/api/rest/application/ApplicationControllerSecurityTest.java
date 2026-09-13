package io.github.yaforster.trails.adapter.api.rest.application;

import io.github.yaforster.trails.adapter.api.rest.hateoas.HATEOASFacade;
import io.github.yaforster.trails.adapter.api.ResourceAuthorization;
import io.github.yaforster.trails.adapter.api.rest.model.PersistedApplicationDTO;
import io.github.yaforster.trails.adapter.api.security.AuthorizationDeniedEventLogger;
import io.github.yaforster.trails.adapter.api.security.AuthorizationDeniedEventLoggingConfiguration;
import io.github.yaforster.trails.adapter.api.security.SecurityConfiguration;
import io.github.yaforster.trails.app.services.ApplicationDatabaseService;
import io.github.yaforster.trails.core.SecurityTestConfiguration;
import io.github.yaforster.trails.core.definition.ApplicationDefinition;
import io.github.yaforster.trails.core.persisted.PersistedApplication;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.openapitools.api.ApplicationApi;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.system.CapturedOutput;
import org.springframework.boot.test.system.OutputCaptureExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.web.servlet.MockMvc;

import static io.github.yaforster.trails.core.SecurityTestConfiguration.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(OutputCaptureExtension.class)
@SpringBootTest(classes = { SecurityTestConfiguration.class, SecurityConfiguration.class,
		AuthorizationDeniedEventLoggingConfiguration.class, ApplicationControllerSecurityTestConfiguration.class,
		AuthorizationDeniedEventLogger.class, ResourceAuthorization.class, ApplicationController.class },
		properties = { OAUTH2_AUTHENTICATION_ENABLED, CSRF_DISABLED, TRAILS_TEST_MANAGER_ROLE_PROPERTY,
				TRAILS_TESTER_ROLE_PROPERTY, TRAILS_ADMIN_ROLE_PROPERTY })
@AutoConfigureMockMvc
class ApplicationControllerSecurityTest {

	@Autowired
	private ApplicationApi applicationApi;

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private ApplicationDatabaseService applicationDatabaseService;

	@Autowired
	private ApplicationHATEOASFacade hateoasFacade;

	@Test
	void createNewApplication_shouldReturnOk_whenUserHasTrailsTestManagerRole() {
		ApplicationDefinition applicationDefinition = new ApplicationDefinition("app");
		PersistedApplication persistedApplication = new PersistedApplication(1L, "app");
		PersistedApplicationDTO persistedApplicationDTO = new PersistedApplicationDTO().id(1L).label("app");
		when(hateoasFacade.toDomain(any())).thenReturn(applicationDefinition);
		when(applicationDatabaseService.storeApplication(applicationDefinition)).thenReturn(persistedApplication);
		when(hateoasFacade.toDTO(persistedApplication)).thenReturn(persistedApplicationDTO);
		authenticateWithRole("ROLE_trails-test-manager");

		ResponseEntity<PersistedApplicationDTO> response;
		try {
			response = applicationApi.createNewApplication(null);
		}
		finally {
			SecurityContextHolder.clearContext();
		}

		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
	}

	@Test
	void createNewApplication_shouldReturnOk_whenUserHasTrailsAdminRole() {
		ApplicationDefinition applicationDefinition = new ApplicationDefinition("app");
		PersistedApplication persistedApplication = new PersistedApplication(1L, "app");
		PersistedApplicationDTO persistedApplicationDTO = new PersistedApplicationDTO().id(1L).label("app");
		when(hateoasFacade.toDomain(any())).thenReturn(applicationDefinition);
		when(applicationDatabaseService.storeApplication(applicationDefinition)).thenReturn(persistedApplication);
		when(hateoasFacade.toDTO(persistedApplication)).thenReturn(persistedApplicationDTO);
		authenticateWithRole("ROLE_trails-admin");

		ResponseEntity<PersistedApplicationDTO> response;
		try {
			response = applicationApi.createNewApplication(null);
		}
		finally {
			SecurityContextHolder.clearContext();
		}

		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
	}

	@Test
	void createNewApplication_shouldDenyAccess_whenUserOnlyHasTesterRole() {
		authenticateWithRole("ROLE_trails-tester");

		try {
			assertThatThrownBy(() -> applicationApi.createNewApplication(null))
				.isInstanceOf(AccessDeniedException.class);
		}
		finally {
			SecurityContextHolder.clearContext();
		}
	}

	@Test
	void createNewApplication_shouldLogDeniedAccess_whenUserOnlyHasTesterRole(CapturedOutput output) {
		authenticateWithRole("ROLE_trails-tester");

		try {
			assertThatThrownBy(() -> applicationApi.createNewApplication(null))
				.isInstanceOf(AccessDeniedException.class);
		}
		finally {
			SecurityContextHolder.clearContext();
		}

		assertThat(output.getOut()).contains(
				"Denied access to 'secured method ApplicationController.createNewApplication' for principal 'user' "
						+ "with authorities '[ROLE_trails-tester]'");
	}

	@Test
	void createNewApplication_shouldLogDeniedAccess_whenUserHasNoRoles(CapturedOutput output) {
		authenticateWithoutRoles();

		try {
			assertThatThrownBy(() -> applicationApi.createNewApplication(null))
				.isInstanceOf(AccessDeniedException.class);
		}
		finally {
			SecurityContextHolder.clearContext();
		}

		assertThat(output.getOut()).contains(
				"Denied access to 'secured method ApplicationController.createNewApplication' for principal 'user' "
						+ "with authorities '[]'");
	}

	@Test
	void createNewApplication_shouldLogControllerMethod_whenAnonymousRequestIsDenied(CapturedOutput output)
			throws Exception {
		mockMvc.perform(put("/api/applications").contentType(APPLICATION_JSON).content("{\"label\":\"app\"}"))
			.andExpect(status().isUnauthorized());

		assertThat(output.getOut())
			.contains("Denied access to 'secured method ApplicationController.createNewApplication' for principal "
					+ "'anonymousUser' with authorities '[ROLE_ANONYMOUS]'");
	}

	private void authenticateWithRole(String authority) {
		UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken("user", "token",
				java.util.List.of(new SimpleGrantedAuthority(authority)));
		SecurityContextHolder.getContext().setAuthentication(authentication);
	}

	private void authenticateWithoutRoles() {
		UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken("user", "token",
				java.util.List.of());
		SecurityContextHolder.getContext().setAuthentication(authentication);
	}

}
