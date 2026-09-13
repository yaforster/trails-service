package io.github.yaforster.trails.adapter.api.security;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

import static org.assertj.core.api.Assertions.assertThat;

class SecurityPropertiesValidatorTest {

	private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
		.withUserConfiguration(SecurityPropertiesConfiguration.class);

	@Test
	void context_shouldStart_whenOAuthIsDisabled() {
		contextRunner.run(context -> assertThat(context.getStartupFailure()).isNull());
	}

	@Test
	void context_shouldStart_whenOAuthUsesIssuerUri() {
		contextRunner
			.withPropertyValues(
					enabledProperties("spring.security.oauth2.resourceserver.jwt.issuer-uri=https://issuer"))
			.run(context -> assertThat(context.getStartupFailure()).isNull());
	}

	@Test
	void context_shouldStart_whenOAuthUsesJwkSetUri() {
		contextRunner
			.withPropertyValues(
					enabledProperties("spring.security.oauth2.resourceserver.jwt.jwk-set-uri=https://issuer/jwks"))
			.run(context -> assertThat(context.getStartupFailure()).isNull());
	}

	@Test
	void context_shouldFail_whenOAuthHasNoDecoderSource() {
		contextRunner.withPropertyValues(enabledProperties())
			.run(context -> assertThat(rootCause(context.getStartupFailure())).hasMessageContaining("Exactly one"));
	}

	@Test
	void context_shouldFail_whenOAuthHasBothDecoderSources() {
		contextRunner
			.withPropertyValues(enabledProperties("spring.security.oauth2.resourceserver.jwt.issuer-uri=https://issuer",
					"spring.security.oauth2.resourceserver.jwt.jwk-set-uri=https://issuer/jwks"))
			.run(context -> assertThat(rootCause(context.getStartupFailure())).hasMessageContaining("Exactly one"));
	}

	@Test
	void context_shouldFail_whenOAuthOmitsTestManagerRole() {
		contextRunner
			.withPropertyValues(enabledProperties("spring.security.oauth2.resourceserver.jwt.issuer-uri=https://issuer",
					"service.api.security.roles.trails-test-manager="))
			.run(context -> assertThat(rootCause(context.getStartupFailure()))
				.hasMessageContaining("trails-test-manager"));
	}

	@Test
	void context_shouldFail_whenOAuthOmitsTesterRole() {
		contextRunner
			.withPropertyValues(enabledProperties("spring.security.oauth2.resourceserver.jwt.issuer-uri=https://issuer",
					"service.api.security.roles.trails-tester="))
			.run(context -> assertThat(rootCause(context.getStartupFailure())).hasMessageContaining("trails-tester"));
	}

	@Test
	void context_shouldFail_whenOAuthOmitsAdminRole() {
		contextRunner
			.withPropertyValues(enabledProperties("spring.security.oauth2.resourceserver.jwt.issuer-uri=https://issuer",
					"service.api.security.roles.trails-admin="))
			.run(context -> assertThat(rootCause(context.getStartupFailure())).hasMessageContaining("trails-admin"));
	}

	@Test
	void context_shouldFail_whenOAuthOmitsDeploymentReporterRole() {
		contextRunner
			.withPropertyValues(enabledProperties("spring.security.oauth2.resourceserver.jwt.issuer-uri=https://issuer",
					"service.api.security.deployments.role="))
			.run(context -> assertThat(rootCause(context.getStartupFailure()))
				.hasMessageContaining("deployments.role"));
	}

	private String[] enabledProperties(String... overrides) {
		String[] properties = new String[overrides.length + 5];
		properties[0] = "service.api.security.oauth2.enabled=true";
		properties[1] = "service.api.security.roles.trails-test-manager=test-manager";
		properties[2] = "service.api.security.roles.trails-tester=tester";
		properties[3] = "service.api.security.roles.trails-admin=admin";
		properties[4] = "service.api.security.deployments.role=deployment-reporter";
		System.arraycopy(overrides, 0, properties, 5, overrides.length);
		return properties;
	}

	private Throwable rootCause(Throwable failure) {
		Throwable cause = failure;
		while (cause.getCause() != null) {
			cause = cause.getCause();
		}
		return cause;
	}

}
