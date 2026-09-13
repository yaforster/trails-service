package io.github.yaforster.trails.adapter.api;

import io.github.yaforster.trails.adapter.api.security.JwtRoleClaimResolver;
import io.github.yaforster.trails.adapter.api.security.ApiSecurityProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;

@Component
public class ResourceAuthorization {

	private final ApiSecurityProperties properties;

	private final JwtRoleClaimResolver roleClaimResolver;

	@Autowired
	public ResourceAuthorization(ApiSecurityProperties properties, JwtRoleClaimResolver roleClaimResolver) {
		this.properties = properties;
		this.roleClaimResolver = roleClaimResolver;
	}

	public ResourceAuthorization(ApiSecurityProperties properties) {
		this(properties, new JwtRoleClaimResolver());
	}

	public boolean canPromoteResources() {
		return canManageResources();
	}

	public boolean canManageResources() {
		return canManageResources(SecurityContextHolder.getContext().getAuthentication());
	}

	public boolean canAccessTestData() {
		return canAccessTestData(SecurityContextHolder.getContext().getAuthentication());
	}

	public boolean canExecuteTests() {
		return canExecuteTests(SecurityContextHolder.getContext().getAuthentication());
	}

	public boolean canManageUsers(Authentication authentication) {
		if (!properties.oauth2AuthenticationEnabled()) {
			return true;
		}
		return authentication != null && authentication.isAuthenticated()
				&& hasAuthority(authentication, properties.trailsAdminAuthority());
	}

	public boolean canReportDeployments(Authentication authentication) {
		if (!properties.oauth2AuthenticationEnabled()) {
			return true;
		}

		if (authentication == null || !authentication.isAuthenticated()) {
			return false;
		}

		if (hasAuthority(authentication, properties.trailsAdminAuthority())) {
			return true;
		}

		if (!(authentication.getPrincipal() instanceof Jwt jwt)) {
			return false;
		}

		String deploymentReporterRole = properties.deploymentReporterRole();
		if (deploymentReporterRole == null || deploymentReporterRole.isBlank()) {
			return false;
		}

		return roleClaimResolver.resolveRoleNames(jwt, properties.deploymentRoleClaimPaths())
			.anyMatch(deploymentReporterRole::equals);
	}

	public boolean canExecuteTests(Authentication authentication) {
		if (!properties.oauth2AuthenticationEnabled()) {
			return true;
		}

		if (authentication == null || !authentication.isAuthenticated()) {
			return false;
		}

		return hasAuthority(authentication, properties.trailsTesterAuthority())
				|| hasAuthority(authentication, properties.trailsTestManagerAuthority())
				|| hasAuthority(authentication, properties.trailsAdminAuthority());
	}

	public boolean canAccessTestData(Authentication authentication) {
		if (!properties.oauth2AuthenticationEnabled()) {
			return true;
		}

		if (authentication == null || !authentication.isAuthenticated()) {
			return false;
		}

		return hasAuthority(authentication, properties.trailsTesterAuthority())
				|| hasAuthority(authentication, properties.trailsTestManagerAuthority())
				|| hasAuthority(authentication, properties.trailsAdminAuthority());
	}

	public boolean canManageResources(Authentication authentication) {
		if (!properties.oauth2AuthenticationEnabled()) {
			return true;
		}

		if (authentication == null || !authentication.isAuthenticated()) {
			return false;
		}

		return hasAuthority(authentication, properties.trailsTestManagerAuthority())
				|| hasAuthority(authentication, properties.trailsAdminAuthority());
	}

	private boolean hasAuthority(Authentication authentication, String authority) {
		if (authority == null) {
			return false;
		}
		return authentication.getAuthorities().stream().map(GrantedAuthority::getAuthority).anyMatch(authority::equals);
	}

}
