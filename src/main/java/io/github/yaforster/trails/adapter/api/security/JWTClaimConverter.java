package io.github.yaforster.trails.adapter.api.security;

import com.google.common.annotations.VisibleForTesting;
import org.jspecify.annotations.NonNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.convert.converter.Converter;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;

import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;

@Component
public class JWTClaimConverter implements Converter<Jwt, AbstractAuthenticationToken> {

	private final ApiSecurityProperties properties;

	private final JwtRoleClaimResolver roleClaimResolver;

	@Autowired
	public JWTClaimConverter(ApiSecurityProperties properties, JwtRoleClaimResolver roleClaimResolver) {
		this.properties = properties;
		this.roleClaimResolver = roleClaimResolver;
	}

	@VisibleForTesting
	public JWTClaimConverter(ApiSecurityProperties properties) {
		this(properties, new JwtRoleClaimResolver());
	}

	@Override
	public AbstractAuthenticationToken convert(@NonNull Jwt jwt) {
		return new JwtAuthenticationToken(jwt, extractAuthorities(jwt));
	}

	@VisibleForTesting
	protected Set<GrantedAuthority> extractAuthorities(Jwt jwt) {
		return roleClaimResolver.resolveRoleNames(jwt, properties.oauth2RoleClaimPaths())
			.map(this::toAuthority)
			.collect(LinkedHashSet::new, Set::add, Set::addAll);
	}

	@VisibleForTesting
	protected Stream<Object> resolveClaimPath(Map<String, Object> claims, String claimPath) {
		return roleClaimResolver.resolveClaimPath(claims, claimPath);
	}

	@VisibleForTesting
	protected Stream<Object> resolveClaimSegment(Object claims, String[] claimSegments, int index) {
		return roleClaimResolver.resolveClaimSegment(claims, claimSegments, index);
	}

	@VisibleForTesting
	protected Stream<Object> getObjectStream(Object value) {
		return roleClaimResolver.getObjectStream(value);
	}

	@VisibleForTesting
	protected Optional<String> toRoleName(Object value) {
		return roleClaimResolver.toRoleName(value);
	}

	@VisibleForTesting
	protected GrantedAuthority toAuthority(String role) {
		String prefix = properties.oauth2AuthorityPrefix();
		if (prefix.isBlank() || role.startsWith(prefix)) {
			return new SimpleGrantedAuthority(role);
		}
		return new SimpleGrantedAuthority(prefix + role);
	}

}
