package io.github.yaforster.trails.adapter.api.security;

import com.google.common.annotations.VisibleForTesting;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Stream;

@Component
public class JwtRoleClaimResolver {

	private static final String CLAIM_PATH_SEPARATOR = "\\.";

	private static final String WILDCARD_SEGMENT = "*";

	private static final String ROLE_SEPARATOR = " ";

	public Stream<String> resolveRoleNames(Jwt jwt, List<String> roleClaimPaths) {
		return roleClaimPaths.stream()
			.flatMap(claimPath -> resolveClaimPath(jwt.getClaims(), claimPath))
			.map(this::toRoleName)
			.flatMap(Optional::stream);
	}

	@VisibleForTesting
	public Stream<Object> resolveClaimPath(Map<String, Object> claims, String claimPath) {
		String[] claimSegments = claimPath.split(CLAIM_PATH_SEPARATOR);
		return resolveClaimSegment(claims, claimSegments, 0);
	}

	@VisibleForTesting
	public Stream<Object> resolveClaimSegment(Object claims, String[] claimSegments, int index) {
		if (index >= claimSegments.length) {
			return getObjectStream(claims);
		}
		if (!(claims instanceof Map<?, ?> map)) {
			return Stream.empty();
		}
		String segment = claimSegments[index];
		if (WILDCARD_SEGMENT.equals(segment)) {
			return map.values().stream().flatMap(value -> resolveClaimSegment(value, claimSegments, index + 1));
		}
		if (!map.containsKey(segment)) {
			return Stream.empty();
		}
		return resolveClaimSegment(map.get(segment), claimSegments, index + 1);
	}

	@VisibleForTesting
	public Stream<Object> getObjectStream(Object value) {
		if (value instanceof Collection<?> collection) {
			return collection.stream().map(Object.class::cast);
		}
		if (value instanceof String text && text.contains(ROLE_SEPARATOR)) {
			return Arrays.stream(text.split(ROLE_SEPARATOR));
		}
		return Stream.ofNullable(value);
	}

	@VisibleForTesting
	public Optional<String> toRoleName(Object value) {
		if (!(value instanceof String role) || role.isBlank()) {
			return Optional.empty();
		}
		return Optional.of(role.trim());
	}

}
