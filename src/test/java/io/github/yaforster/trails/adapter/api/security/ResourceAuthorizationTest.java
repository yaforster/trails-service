package io.github.yaforster.trails.adapter.api.security;

import io.github.yaforster.trails.adapter.api.ResourceAuthorization;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ResourceAuthorizationTest {

	@Test
	void canPromoteResources_shouldReturnTrue_whenOauthIsDisabled() {
		ApiSecurityProperties settings = mock(ApiSecurityProperties.class);
		when(settings.oauth2AuthenticationEnabled()).thenReturn(false);
		ResourceAuthorization authorization = new ResourceAuthorization(settings);

		boolean result = authorization.canPromoteResources();

		assertThat(result).isTrue();
	}

	@Test
	void canManageResources_shouldReturnTrue_whenOauthIsDisabled() {
		ApiSecurityProperties settings = mock(ApiSecurityProperties.class);
		when(settings.oauth2AuthenticationEnabled()).thenReturn(false);
		ResourceAuthorization authorization = new ResourceAuthorization(settings);

		boolean result = authorization.canManageResources(null);

		assertThat(result).isTrue();
	}

	@Test
	void canReportDeployments_shouldReturnTrue_whenOauthIsDisabled() {
		ApiSecurityProperties settings = mock(ApiSecurityProperties.class);
		when(settings.oauth2AuthenticationEnabled()).thenReturn(false);
		ResourceAuthorization authorization = new ResourceAuthorization(settings);

		boolean result = authorization.canReportDeployments(null);

		assertThat(result).isTrue();
	}

	@Test
	void canManageUsers_shouldReturnTrue_whenOauthIsDisabled() {
		ApiSecurityProperties settings = mock(ApiSecurityProperties.class);
		when(settings.oauth2AuthenticationEnabled()).thenReturn(false);
		ResourceAuthorization authorization = new ResourceAuthorization(settings);

		boolean result = authorization.canManageUsers(null);

		assertThat(result).isTrue();
	}

	@Test
	void canManageUsers_shouldReturnTrue_whenUserHasAdminAuthority() {
		ApiSecurityProperties settings = mock(ApiSecurityProperties.class);
		when(settings.oauth2AuthenticationEnabled()).thenReturn(true);
		when(settings.trailsAdminAuthority()).thenReturn("ROLE_trails-admin");
		ResourceAuthorization authorization = new ResourceAuthorization(settings);

		boolean result = authorization.canManageUsers(authentication("ROLE_trails-admin"));

		assertThat(result).isTrue();
	}

	@Test
	void canManageUsers_shouldReturnFalse_whenUserLacksAdminAuthority() {
		ApiSecurityProperties settings = mock(ApiSecurityProperties.class);
		when(settings.oauth2AuthenticationEnabled()).thenReturn(true);
		when(settings.trailsAdminAuthority()).thenReturn("ROLE_trails-admin");
		ResourceAuthorization authorization = new ResourceAuthorization(settings);

		boolean result = authorization.canManageUsers(authentication("ROLE_trails-tester"));

		assertThat(result).isFalse();
	}

	@Test
	void canExecuteTests_shouldReturnTrue_whenUserHasTesterAuthority() {
		ApiSecurityProperties settings = mock(ApiSecurityProperties.class);
		when(settings.oauth2AuthenticationEnabled()).thenReturn(true);
		when(settings.trailsTesterAuthority()).thenReturn("ROLE_trails-tester");
		when(settings.trailsTestManagerAuthority()).thenReturn("ROLE_trails-test-manager");
		when(settings.trailsAdminAuthority()).thenReturn("ROLE_trails-admin");
		ResourceAuthorization authorization = new ResourceAuthorization(settings);

		boolean result = authorization.canExecuteTests(authentication("ROLE_trails-tester"));

		assertThat(result).isTrue();
	}

	@Test
	void canExecuteTests_shouldReturnFalse_whenAuthenticationIsMissing() {
		ApiSecurityProperties settings = mock(ApiSecurityProperties.class);
		when(settings.oauth2AuthenticationEnabled()).thenReturn(true);
		ResourceAuthorization authorization = new ResourceAuthorization(settings);

		boolean result = authorization.canExecuteTests(null);

		assertThat(result).isFalse();
	}

	@Test
	void canPromoteResources_shouldReturnTrue_whenUserHasTestManagerAuthority() {
		ApiSecurityProperties settings = mock(ApiSecurityProperties.class);
		when(settings.oauth2AuthenticationEnabled()).thenReturn(true);
		when(settings.trailsTestManagerAuthority()).thenReturn("ROLE_trails-test-manager");
		when(settings.trailsAdminAuthority()).thenReturn("ROLE_trails-admin");
		ResourceAuthorization authorization = new ResourceAuthorization(settings);
		authenticateWith("ROLE_trails-test-manager");

		try {
			boolean result = authorization.canPromoteResources();

			assertThat(result).isTrue();
		}
		finally {
			SecurityContextHolder.clearContext();
		}
	}

	@Test
	void canPromoteResources_shouldReturnTrue_whenUserHasAdminAuthority() {
		ApiSecurityProperties settings = mock(ApiSecurityProperties.class);
		when(settings.oauth2AuthenticationEnabled()).thenReturn(true);
		when(settings.trailsTestManagerAuthority()).thenReturn("ROLE_trails-test-manager");
		when(settings.trailsAdminAuthority()).thenReturn("ROLE_trails-admin");
		ResourceAuthorization authorization = new ResourceAuthorization(settings);
		authenticateWith("ROLE_trails-admin");

		try {
			boolean result = authorization.canPromoteResources();

			assertThat(result).isTrue();
		}
		finally {
			SecurityContextHolder.clearContext();
		}
	}

	@Test
	void canPromoteResources_shouldReturnFalse_whenUserHasDifferentAuthority() {
		ApiSecurityProperties settings = mock(ApiSecurityProperties.class);
		when(settings.oauth2AuthenticationEnabled()).thenReturn(true);
		when(settings.trailsTestManagerAuthority()).thenReturn("ROLE_trails-test-manager");
		when(settings.trailsAdminAuthority()).thenReturn("ROLE_trails-admin");
		ResourceAuthorization authorization = new ResourceAuthorization(settings);
		authenticateWith("ROLE_trails-tester");

		try {
			boolean result = authorization.canPromoteResources();

			assertThat(result).isFalse();
		}
		finally {
			SecurityContextHolder.clearContext();
		}
	}

	@Test
	void canReportDeployments_shouldReturnTrue_whenJwtContainsDeploymentRoleInConfiguredPath() {
		ApiSecurityProperties settings = mock(ApiSecurityProperties.class);
		when(settings.oauth2AuthenticationEnabled()).thenReturn(true);
		when(settings.trailsAdminAuthority()).thenReturn("ROLE_trails-admin");
		when(settings.deploymentReporterRole()).thenReturn("trails-deployment");
		when(settings.deploymentRoleClaimPaths()).thenReturn(List.of("deployment.roles"));
		ResourceAuthorization authorization = new ResourceAuthorization(settings);
		JwtAuthenticationToken authentication = jwtAuthentication(
				Map.of("deployment", Map.of("roles", List.of("trails-deployment"))), List.of());

		boolean result = authorization.canReportDeployments(authentication);

		assertThat(result).isTrue();
	}

	@Test
	void canReportDeployments_shouldReturnFalse_whenJwtDoesNotContainDeploymentRole() {
		ApiSecurityProperties settings = mock(ApiSecurityProperties.class);
		when(settings.oauth2AuthenticationEnabled()).thenReturn(true);
		when(settings.trailsAdminAuthority()).thenReturn("ROLE_trails-admin");
		when(settings.deploymentReporterRole()).thenReturn("trails-deployment");
		when(settings.deploymentRoleClaimPaths()).thenReturn(List.of("deployment.roles"));
		ResourceAuthorization authorization = new ResourceAuthorization(settings);
		JwtAuthenticationToken authentication = jwtAuthentication(
				Map.of("deployment", Map.of("roles", List.of("other-role"))), List.of());

		boolean result = authorization.canReportDeployments(authentication);

		assertThat(result).isFalse();
	}

	@Test
	void canReportDeployments_shouldReturnTrue_whenUserHasAdminAuthority() {
		ApiSecurityProperties settings = mock(ApiSecurityProperties.class);
		when(settings.oauth2AuthenticationEnabled()).thenReturn(true);
		when(settings.trailsAdminAuthority()).thenReturn("ROLE_trails-admin");
		when(settings.deploymentReporterRole()).thenReturn("trails-deployment");
		ResourceAuthorization authorization = new ResourceAuthorization(settings);
		JwtAuthenticationToken authentication = jwtAuthentication(
				Map.of("deployment", Map.of("roles", List.of("other-role"))), List.of("ROLE_trails-admin"));

		boolean result = authorization.canReportDeployments(authentication);

		assertThat(result).isTrue();
	}

	private void authenticateWith(String authority) {
		SecurityContextHolder.getContext().setAuthentication(authentication(authority));
	}

	private UsernamePasswordAuthenticationToken authentication(String authority) {
		return new UsernamePasswordAuthenticationToken("user", "token", List.of(new SimpleGrantedAuthority(authority)));
	}

	private JwtAuthenticationToken jwtAuthentication(Map<String, Object> claims, List<String> authorities) {
		Jwt jwt = Jwt.withTokenValue("token")
			.header("alg", "none")
			.claims(jwtClaims -> jwtClaims.putAll(claims))
			.build();
		return new JwtAuthenticationToken(jwt, authorities.stream().map(SimpleGrantedAuthority::new).toList());
	}

}
