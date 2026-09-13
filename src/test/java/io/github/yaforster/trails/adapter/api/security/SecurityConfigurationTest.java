package io.github.yaforster.trails.adapter.api.security;

import io.github.yaforster.trails.adapter.api.WebConfiguration;
import io.github.yaforster.trails.core.SecurityTestConfiguration;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpHeaders;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

import static io.github.yaforster.trails.core.SecurityTestConfiguration.CSRF_DISABLED;
import static io.github.yaforster.trails.core.SecurityTestConfiguration.CSRF_ENABLED;
import static io.github.yaforster.trails.core.SecurityTestConfiguration.OAUTH2_AUTHENTICATION_DISABLED;
import static io.github.yaforster.trails.core.SecurityTestConfiguration.OAUTH2_AUTHENTICATION_ENABLED;
import static io.github.yaforster.trails.core.SecurityTestConfiguration.TRAILS_ADMIN_ROLE_PROPERTY;
import static io.github.yaforster.trails.core.SecurityTestConfiguration.TRAILS_TESTER_ROLE_PROPERTY;
import static io.github.yaforster.trails.core.SecurityTestConfiguration.TRAILS_TEST_MANAGER_ROLE_PROPERTY;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.options;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(
		classes = { SecurityTestConfiguration.class, SecurityConfiguration.class,
				SecurityConfigurationTestControllerConfiguration.class },
		properties = { OAUTH2_AUTHENTICATION_DISABLED, CSRF_DISABLED })
@AutoConfigureMockMvc
class SecurityConfigurationOAuthDisabledCsrfDisabledTest {

	@Autowired
	private MockMvc mockMvc;

	@Test
	void securityFilterChain_shouldPermitApiRequest_whenOAuthIsDisabled() throws Exception {
		mockMvc.perform(get("/api/security-test")).andExpect(status().isOk());
	}

	@Test
	void securityFilterChain_shouldPermitPostRequest_whenCsrfIsDisabled() throws Exception {
		mockMvc.perform(post("/security-test")).andExpect(status().isOk());
	}

}

@SpringBootTest(
		classes = { SecurityTestConfiguration.class, SecurityConfiguration.class,
				SecurityConfigurationTestControllerConfiguration.class },
		properties = { OAUTH2_AUTHENTICATION_DISABLED, CSRF_ENABLED })
@AutoConfigureMockMvc
class SecurityConfigurationOAuthDisabledCsrfEnabledTest {

	@Autowired
	private MockMvc mockMvc;

	@Test
	void securityFilterChain_shouldRejectPostRequest_whenCsrfIsEnabled() throws Exception {
		mockMvc.perform(post("/security-test")).andExpect(status().isForbidden());
	}

}

@SpringBootTest(
		classes = { SecurityTestConfiguration.class, SecurityConfiguration.class,
				SecurityConfigurationTestControllerConfiguration.class },
		properties = { OAUTH2_AUTHENTICATION_ENABLED, CSRF_DISABLED, TRAILS_TEST_MANAGER_ROLE_PROPERTY,
				TRAILS_TESTER_ROLE_PROPERTY, TRAILS_ADMIN_ROLE_PROPERTY })
@AutoConfigureMockMvc
class SecurityConfigurationOAuthEnabledCsrfDisabledTest {

	@Autowired
	private MockMvc mockMvc;

	@Test
	void securityFilterChain_shouldRequireAuthenticationForApiRequest_whenOAuthIsEnabled() throws Exception {
		mockMvc.perform(get("/api/security-test")).andExpect(status().isUnauthorized());
	}

	@Test
	void securityFilterChain_shouldPermitCapabilitiesRequest_whenOAuthIsEnabled() throws Exception {
		mockMvc.perform(get("/api/capabilities")).andExpect(status().isOk());
	}

	@Test
	void securityFilterChain_shouldRequireAuthenticationForUserRequest_whenOAuthIsEnabled() throws Exception {
		mockMvc.perform(get("/user/security-test")).andExpect(status().isUnauthorized());
	}

	@Test
	void securityFilterChain_shouldPermitNonApiRequest_whenOAuthIsEnabled() throws Exception {
		mockMvc.perform(get("/security-test")).andExpect(status().isOk());
	}

}

@SpringBootTest(
		classes = { SecurityTestConfiguration.class, SecurityConfiguration.class, WebConfiguration.class,
				SecurityConfigurationTestControllerConfiguration.class },
		properties = { OAUTH2_AUTHENTICATION_ENABLED, CSRF_DISABLED, TRAILS_TEST_MANAGER_ROLE_PROPERTY,
				TRAILS_TESTER_ROLE_PROPERTY, TRAILS_ADMIN_ROLE_PROPERTY })
@AutoConfigureMockMvc
class SecurityConfigurationOAuthEnabledCorsTest {

	@Autowired
	private MockMvc mockMvc;

	@Test
	void securityFilterChain_shouldPermitCapabilitiesPreflightWithoutCredentials() throws Exception {
		String origin = "http://localhost:4200";

		mockMvc
			.perform(options("/api/capabilities").header(HttpHeaders.ORIGIN, origin)
				.header(HttpHeaders.ACCESS_CONTROL_REQUEST_METHOD, "GET")
				.header(HttpHeaders.ACCESS_CONTROL_REQUEST_HEADERS, "authorization"))
			.andExpect(status().isOk())
			.andExpect(header().string(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN, origin))
			.andExpect(header().doesNotExist(HttpHeaders.ACCESS_CONTROL_ALLOW_CREDENTIALS));
	}

	@Test
	void securityFilterChain_shouldPermitPublicCapabilitiesRequestWithInvalidBearerToken() throws Exception {
		String origin = "http://localhost:4200";

		mockMvc
			.perform(get("/api/capabilities").header(HttpHeaders.ORIGIN, origin)
				.header(HttpHeaders.AUTHORIZATION, "Bearer invalid-token"))
			.andExpect(status().isOk())
			.andExpect(header().string(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN, origin))
			.andExpect(header().doesNotExist(HttpHeaders.ACCESS_CONTROL_ALLOW_CREDENTIALS));
	}

	@Test
	void securityFilterChain_shouldReturnCorsHeadersForProtectedApiBearerTokenFailure() throws Exception {
		String origin = "http://localhost:4200";

		mockMvc
			.perform(get("/api/security-test").header(HttpHeaders.ORIGIN, origin)
				.header(HttpHeaders.AUTHORIZATION, "Bearer invalid-token"))
			.andExpect(status().isUnauthorized())
			.andExpect(header().string(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN, origin))
			.andExpect(header().doesNotExist(HttpHeaders.ACCESS_CONTROL_ALLOW_CREDENTIALS));
	}

}

@TestConfiguration
@EnableWebMvc
class SecurityConfigurationTestControllerConfiguration {

	@Bean
	SecurityConfigurationTestController securityConfigurationTestController() {
		return new SecurityConfigurationTestController();
	}

}

@RestController
class SecurityConfigurationTestController {

	@GetMapping({ "/api/security-test", "/api/capabilities", "/user/security-test", "/security-test" })
	void getSecurityTest() {
	}

	@PostMapping("/security-test")
	void postSecurityTest() {
	}

}
