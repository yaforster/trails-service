package io.github.yaforster.trails.core;

import io.github.yaforster.trails.adapter.api.DemoResourceProperties;
import io.github.yaforster.trails.adapter.api.security.JWTClaimConverter;
import io.github.yaforster.trails.adapter.api.security.ApiSecurityProperties;
import io.github.yaforster.trails.adapter.api.security.JwtRoleClaimResolver;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.core.env.Environment;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.BadJwtException;
import org.springframework.security.oauth2.jwt.JwtDecoder;

import java.time.Instant;
import java.util.List;
import java.util.Map;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@TestConfiguration
@EnableMethodSecurity(proxyTargetClass = true)
public class SecurityTestConfiguration {

	public static final String OAUTH2_AUTHENTICATION_ENABLED = "service.api.security.oauth2.enabled=true";

	public static final String OAUTH2_AUTHENTICATION_DISABLED = "service.api.security.oauth2.enabled=false";

	public static final String CSRF_ENABLED = "service.api.security.csrf.enabled=true";

	public static final String CSRF_DISABLED = "service.api.security.csrf.enabled=false";

	public static final String TRAILS_TEST_MANAGER_ROLE = "trails-test-manager";

	public static final String TRAILS_TESTER_ROLE = "trails-tester";

	public static final String TRAILS_ADMIN_ROLE = "trails-admin";

	public static final String TRAILS_TEST_MANAGER_TOKEN = "trails-test-manager-token";

	public static final String TRAILS_TESTER_TOKEN = "trails-tester-token";

	public static final String TRAILS_ADMIN_TOKEN = "trails-admin-token";

	public static final String TRAILS_TEST_MANAGER_ROLE_PROPERTY = "service.api.security.roles.trails-test-manager="
			+ TRAILS_TEST_MANAGER_ROLE;

	public static final String TRAILS_TESTER_ROLE_PROPERTY = "service.api.security.roles.trails-tester="
			+ TRAILS_TESTER_ROLE;

	public static final String TRAILS_ADMIN_ROLE_PROPERTY = "service.api.security.roles.trails-admin="
			+ TRAILS_ADMIN_ROLE;

	@Bean
	JWTClaimConverter jwtClaimConverter(ApiSecurityProperties properties) {
		return new JWTClaimConverter(properties);
	}

	@Bean
	JwtRoleClaimResolver jwtRoleClaimResolver() {
		return new JwtRoleClaimResolver();
	}

	@Bean
	JwtDecoder jwtDecoder() {
		JwtDecoder jwtDecoder = mock(JwtDecoder.class);
		when(jwtDecoder.decode(TRAILS_TEST_MANAGER_TOKEN))
			.thenReturn(jwt(TRAILS_TEST_MANAGER_TOKEN, TRAILS_TEST_MANAGER_ROLE));
		when(jwtDecoder.decode(TRAILS_TESTER_TOKEN)).thenReturn(jwt(TRAILS_TESTER_TOKEN, TRAILS_TESTER_ROLE));
		when(jwtDecoder.decode(TRAILS_ADMIN_TOKEN)).thenReturn(jwt(TRAILS_ADMIN_TOKEN, TRAILS_ADMIN_ROLE));
		when(jwtDecoder.decode("invalid-token")).thenThrow(new BadJwtException("Invalid token"));
		return jwtDecoder;
	}

	@Bean
	ApiSecurityProperties apiSecurityProperties(Environment environment) {
		return new ApiSecurityProperties(
				new ApiSecurityProperties.CsrfProperties(
						environment.getRequiredProperty("service.api.security.csrf.enabled").equalsIgnoreCase("true")),
				new ApiSecurityProperties.RolesProperties(
						environment.getProperty("service.api.security.roles.trails-test-manager"),
						environment.getProperty("service.api.security.roles.trails-tester"),
						environment.getProperty("service.api.security.roles.trails-admin")),
				new ApiSecurityProperties.DeploymentProperties(null, "roles"),
				new ApiSecurityProperties.OAuth2Properties(
						environment.getRequiredProperty("service.api.security.oauth2.enabled").equalsIgnoreCase("true"),
						environment.getProperty("service.api.security.oauth2.authority-prefix", "ROLE_"), "roles"));
	}

	@Bean
	DemoResourceProperties demoResourceProperties() {
		return new DemoResourceProperties(false);
	}

	private Jwt jwt(String token, String role) {
		Instant issuedAt = Instant.now();
		return new Jwt(token, issuedAt, issuedAt.plusSeconds(300), Map.of("alg", "none"),
				Map.of("roles", List.of(role)));
	}

}
