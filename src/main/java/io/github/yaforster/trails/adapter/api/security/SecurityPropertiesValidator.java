package io.github.yaforster.trails.adapter.api.security;

import jakarta.annotation.PostConstruct;
import org.springframework.util.StringUtils;

class SecurityPropertiesValidator {

	private final ApiSecurityProperties properties;

	private final JwtDecoderProperties decoderProperties;

	SecurityPropertiesValidator(ApiSecurityProperties properties, JwtDecoderProperties decoderProperties) {
		this.properties = properties;
		this.decoderProperties = decoderProperties;
	}

	@PostConstruct
	void validateConfiguration() {
		if (!properties.oauth2AuthenticationEnabled()) {
			return;
		}
		requireExactlyOneDecoderSource();
		requireConfiguredRole("service.api.security.roles.trails-test-manager", properties.trailsTestManagerRole());
		requireConfiguredRole("service.api.security.roles.trails-tester", properties.trailsTesterRole());
		requireConfiguredRole("service.api.security.roles.trails-admin", properties.trailsAdminRole());
		requireConfiguredRole("service.api.security.deployments.role", properties.deploymentReporterRole());
	}

	private void requireExactlyOneDecoderSource() {
		boolean issuerUriConfigured = StringUtils.hasText(decoderProperties.issuerUri());
		boolean jwkSetUriConfigured = StringUtils.hasText(decoderProperties.jwkSetUri());
		if (issuerUriConfigured != jwkSetUriConfigured) {
			return;
		}
		throw new IllegalStateException("Exactly one of spring.security.oauth2.resourceserver.jwt.issuer-uri or "
				+ "spring.security.oauth2.resourceserver.jwt.jwk-set-uri"
				+ " must be set when service.api.security.oauth2.enabled=true");
	}

	private void requireConfiguredRole(String propertyName, String role) {
		if (!StringUtils.hasText(role)) {
			throw new IllegalStateException(
					propertyName + " must be set when service.api.security.oauth2.enabled=true");
		}
	}

}
