package io.github.yaforster.trails.adapter.api.security;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.util.StringUtils;

import java.util.Arrays;
import java.util.List;

@ConfigurationProperties("service.api.security")
public record ApiSecurityProperties(CsrfProperties csrf, RolesProperties roles, DeploymentProperties deployments,
		OAuth2Properties oauth2) {

	private static final List<String> DEFAULT_ROLE_CLAIM_PATHS = List.of("realm_access.roles",
			"resource_access.*.roles", "roles", "authorities");

	public boolean csrfEnabled() {
		return csrf != null && csrf.enabled();
	}

	public boolean oauth2AuthenticationEnabled() {
		return oauth2 != null && oauth2.enabled();
	}

	public String oauth2AuthorityPrefix() {
		return oauth2 == null || oauth2.authorityPrefix() == null ? "ROLE_" : oauth2.authorityPrefix();
	}

	public List<String> oauth2RoleClaimPaths() {
		return claimPaths(oauth2 == null ? null : oauth2.roleClaimPaths());
	}

	public List<String> deploymentRoleClaimPaths() {
		return claimPaths(deployments == null ? null : deployments.roleClaimPaths());
	}

	public String trailsTestManagerRole() {
		return roles == null ? null : roles.trailsTestManager();
	}

	public String trailsTesterRole() {
		return roles == null ? null : roles.trailsTester();
	}

	public String trailsAdminRole() {
		return roles == null ? null : roles.trailsAdmin();
	}

	public String deploymentReporterRole() {
		return deployments == null ? null : deployments.role();
	}

	public String trailsTestManagerAuthority() {
		return toAuthority(trailsTestManagerRole());
	}

	public String trailsTesterAuthority() {
		return toAuthority(trailsTesterRole());
	}

	public String trailsAdminAuthority() {
		return toAuthority(trailsAdminRole());
	}

	private List<String> claimPaths(String configuredPaths) {
		if (!StringUtils.hasText(configuredPaths)) {
			return DEFAULT_ROLE_CLAIM_PATHS;
		}
		return Arrays.stream(configuredPaths.split(",")).map(String::trim).filter(StringUtils::hasText).toList();
	}

	private String toAuthority(String role) {
		String authorityPrefix = oauth2AuthorityPrefix();
		if (!StringUtils.hasText(role) || !StringUtils.hasText(authorityPrefix) || role.startsWith(authorityPrefix)) {
			return role;
		}
		return authorityPrefix + role;
	}

	public record CsrfProperties(boolean enabled) {

	}

	public record RolesProperties(String trailsTestManager, String trailsTester, String trailsAdmin) {

	}

	public record DeploymentProperties(String role, String roleClaimPaths) {

	}

	public record OAuth2Properties(boolean enabled, String authorityPrefix, String roleClaimPaths) {

	}

}
