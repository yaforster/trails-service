package io.github.yaforster.trails.adapter.api.security;

import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;

class JWTClaimConverterTest {

	private static List<String> authorities(AbstractAuthenticationToken token) {
		return token.getAuthorities().stream().map(GrantedAuthority::getAuthority).toList();
	}

	private static ApiSecurityProperties settings(String authorityPrefix, List<String> roleClaimPaths) {
		return new ApiSecurityProperties(null, null, null,
				new ApiSecurityProperties.OAuth2Properties(true, authorityPrefix, String.join(",", roleClaimPaths)));
	}

	private static Jwt jwt(Map<String, Object> claims) {
		return Jwt.withTokenValue("token").header("alg", "none").claims(jwtClaims -> jwtClaims.putAll(claims)).build();
	}

	private static Map<String, Object> orderedMap(String firstKey, Object firstValue, String secondKey,
			Object secondValue) {
		Map<String, Object> values = new LinkedHashMap<>();
		values.put(firstKey, firstValue);
		values.put(secondKey, secondValue);
		return values;
	}

	@Test
	void convert_shouldReturnJwtAuthenticationToken_whenJwtContainsRoles() {
		ApiSecurityProperties settings = settings("ROLE_", List.of("roles"));
		JWTClaimConverter converter = new JWTClaimConverter(settings);
		Jwt jwt = jwt(Map.of("roles", List.of("admin")));

		AbstractAuthenticationToken token = converter.convert(jwt);

		assertThat(token).isInstanceOf(JwtAuthenticationToken.class);
	}

	@Test
	void convert_shouldUseOriginalJwtAsPrincipal_whenJwtContainsRoles() {
		ApiSecurityProperties settings = settings("ROLE_", List.of("roles"));
		JWTClaimConverter converter = new JWTClaimConverter(settings);
		Jwt jwt = jwt(Map.of("roles", List.of("admin")));

		AbstractAuthenticationToken token = converter.convert(jwt);

		assertThat(token.getPrincipal()).isSameAs(jwt);
	}

	@Test
	void convert_shouldReadRealmRoles_whenNestedClaimPathExists() {
		ApiSecurityProperties settings = settings("ROLE_", List.of("realm_access.roles"));
		JWTClaimConverter converter = new JWTClaimConverter(settings);
		Jwt jwt = jwt(Map.of("realm_access", Map.of("roles", List.of("admin"))));

		AbstractAuthenticationToken token = converter.convert(jwt);

		assertThat(authorities(token)).containsExactly("ROLE_admin");
	}

	@Test
	void convert_shouldReadClientRoles_whenWildcardPathMatchesMultipleMaps() {
		ApiSecurityProperties settings = settings("ROLE_", List.of("resource_access.*.roles"));
		JWTClaimConverter converter = new JWTClaimConverter(settings);
		Jwt jwt = jwt(Map.of("resource_access", orderedMap("trails-service", Map.of("roles", List.of("writer")),
				"account", Map.of("roles", List.of("viewer")))));

		AbstractAuthenticationToken token = converter.convert(jwt);

		assertThat(authorities(token)).containsExactly("ROLE_writer", "ROLE_viewer");
	}

	@Test
	void convert_shouldReadRolesFromAllConfiguredClaimPaths_whenMultiplePathsExist() {
		ApiSecurityProperties settings = settings("ROLE_", List.of("realm_access.roles", "roles"));
		JWTClaimConverter converter = new JWTClaimConverter(settings);
		Jwt jwt = jwt(Map.of("realm_access", Map.of("roles", List.of("admin")), "roles", List.of("writer")));

		AbstractAuthenticationToken token = converter.convert(jwt);

		assertThat(authorities(token)).containsExactly("ROLE_admin", "ROLE_writer");
	}

	@Test
	void convert_shouldReadSpaceSeparatedRoles_whenRoleClaimIsString() {
		ApiSecurityProperties settings = settings("ROLE_", List.of("authorities"));
		JWTClaimConverter converter = new JWTClaimConverter(settings);
		Jwt jwt = jwt(Map.of("authorities", "admin writer"));

		AbstractAuthenticationToken token = converter.convert(jwt);

		assertThat(authorities(token)).containsExactly("ROLE_admin", "ROLE_writer");
	}

	@Test
	void convert_shouldTrimRoles_whenRoleClaimContainsSurroundingWhitespace() {
		ApiSecurityProperties settings = settings("ROLE_", List.of("roles"));
		JWTClaimConverter converter = new JWTClaimConverter(settings);
		Jwt jwt = jwt(Map.of("roles", List.of(" admin ")));

		AbstractAuthenticationToken token = converter.convert(jwt);

		assertThat(authorities(token)).containsExactly("ROLE_admin");
	}

	@Test
	void convert_shouldIgnoreBlankRoles_whenRoleClaimContainsBlankStrings() {
		ApiSecurityProperties settings = settings("ROLE_", List.of("roles"));
		JWTClaimConverter converter = new JWTClaimConverter(settings);
		Jwt jwt = jwt(Map.of("roles", List.of("admin", " ", "writer")));

		AbstractAuthenticationToken token = converter.convert(jwt);

		assertThat(authorities(token)).containsExactly("ROLE_admin", "ROLE_writer");
	}

	@Test
	void convert_shouldIgnoreNonStringRoles_whenRoleClaimContainsMixedValues() {
		ApiSecurityProperties settings = settings("ROLE_", List.of("roles"));
		JWTClaimConverter converter = new JWTClaimConverter(settings);
		Jwt jwt = jwt(Map.of("roles", List.of("admin", 123, true)));

		AbstractAuthenticationToken token = converter.convert(jwt);

		assertThat(authorities(token)).containsExactly("ROLE_admin");
	}

	@Test
	void convert_shouldDeduplicateRoles_whenSameRoleAppearsMoreThanOnce() {
		ApiSecurityProperties settings = settings("ROLE_", List.of("realm_access.roles", "roles"));
		JWTClaimConverter converter = new JWTClaimConverter(settings);
		Jwt jwt = jwt(Map.of("realm_access", Map.of("roles", List.of("admin")), "roles", List.of("admin")));

		AbstractAuthenticationToken token = converter.convert(jwt);

		assertThat(authorities(token)).containsExactly("ROLE_admin");
	}

	@Test
	void convert_shouldNotDuplicateAuthorityPrefix_whenRoleAlreadyHasPrefix() {
		ApiSecurityProperties settings = settings("ROLE_", List.of("roles"));
		JWTClaimConverter converter = new JWTClaimConverter(settings);
		Jwt jwt = jwt(Map.of("roles", List.of("ROLE_admin")));

		AbstractAuthenticationToken token = converter.convert(jwt);

		assertThat(authorities(token)).containsExactly("ROLE_admin");
	}

	@Test
	void convert_shouldNotAddPrefix_whenAuthorityPrefixIsBlank() {
		ApiSecurityProperties settings = settings("", List.of("roles"));
		JWTClaimConverter converter = new JWTClaimConverter(settings);
		Jwt jwt = jwt(Map.of("roles", List.of("admin")));

		AbstractAuthenticationToken token = converter.convert(jwt);

		assertThat(authorities(token)).containsExactly("admin");
	}

	@Test
	void convert_shouldReturnNoAuthorities_whenConfiguredPathIsMissing() {
		ApiSecurityProperties settings = settings("ROLE_", List.of("realm_access.roles"));
		JWTClaimConverter converter = new JWTClaimConverter(settings);
		Jwt jwt = jwt(Map.of("roles", List.of("admin")));

		AbstractAuthenticationToken token = converter.convert(jwt);

		assertThat(token.getAuthorities()).isEmpty();
	}

	@Test
	void convert_shouldReturnNoAuthorities_whenNestedPathReachesNonMapBeforeFinalSegment() {
		ApiSecurityProperties settings = settings("ROLE_", List.of("realm_access.roles"));
		JWTClaimConverter converter = new JWTClaimConverter(settings);
		Jwt jwt = jwt(Map.of("realm_access", "admin"));

		AbstractAuthenticationToken token = converter.convert(jwt);

		assertThat(token.getAuthorities()).isEmpty();
	}

	@Test
	void extractAuthorities_shouldPreserveFirstOccurrenceOrder_whenRolesComeFromDifferentPaths() {
		ApiSecurityProperties settings = settings("ROLE_", List.of("roles", "authorities"));
		JWTClaimConverter converter = new JWTClaimConverter(settings);
		Jwt jwt = jwt(Map.of("roles", List.of("admin", "writer"), "authorities", List.of("viewer")));

		Set<GrantedAuthority> authorities = converter.extractAuthorities(jwt);

		assertThat(authorities).extracting(GrantedAuthority::getAuthority)
			.containsExactly("ROLE_admin", "ROLE_writer", "ROLE_viewer");
	}

	@Test
	void resolveClaimPath_shouldReturnClaimValue_whenNestedPathExists() {
		ApiSecurityProperties settings = settings("ROLE_", List.of("roles"));
		JWTClaimConverter converter = new JWTClaimConverter(settings);
		Map<String, Object> claims = Map.of("realm_access", Map.of("roles", List.of("admin")));

		List<Object> resolvedValues = converter.resolveClaimPath(claims, "realm_access.roles").toList();

		assertThat(resolvedValues).containsExactly("admin");
	}

	@Test
	void resolveClaimPath_shouldReturnValuesFromWildcardMatches_whenWildcardPathExists() {
		ApiSecurityProperties settings = settings("ROLE_", List.of("roles"));
		JWTClaimConverter converter = new JWTClaimConverter(settings);
		Map<String, Object> claims = Map.of("resource_access", orderedMap("trails-service",
				Map.of("roles", List.of("writer")), "account", Map.of("roles", List.of("viewer"))));

		List<Object> resolvedValues = converter.resolveClaimPath(claims, "resource_access.*.roles").toList();

		assertThat(resolvedValues).containsExactly("writer", "viewer");
	}

	@Test
	void resolveClaimPath_shouldReturnNoValues_whenClaimPathIsMissing() {
		ApiSecurityProperties settings = settings("ROLE_", List.of("roles"));
		JWTClaimConverter converter = new JWTClaimConverter(settings);
		Map<String, Object> claims = Map.of("realm_access", Map.of("groups", List.of("admin")));

		List<Object> resolvedValues = converter.resolveClaimPath(claims, "realm_access.roles").toList();

		assertThat(resolvedValues).isEmpty();
	}

	@Test
	void resolveClaimSegment_shouldReturnNoValues_whenCurrentValueIsNotMapBeforePathEnds() {
		ApiSecurityProperties settings = settings("ROLE_", List.of("roles"));
		JWTClaimConverter converter = new JWTClaimConverter(settings);

		List<Object> resolvedValues = converter.resolveClaimSegment("admin", new String[] { "roles" }, 0).toList();

		assertThat(resolvedValues).isEmpty();
	}

	@Test
	void resolveClaimSegment_shouldFlattenCurrentValue_whenIndexReachedEndOfPath() {
		ApiSecurityProperties settings = settings("ROLE_", List.of("roles"));
		JWTClaimConverter converter = new JWTClaimConverter(settings);

		List<Object> resolvedValues = converter
			.resolveClaimSegment(List.of("admin", "writer"), new String[] { "roles" }, 1)
			.toList();

		assertThat(resolvedValues).containsExactly("admin", "writer");
	}

	@Test
	void getObjectStream_shouldReturnCollectionItems_whenValueIsCollection() {
		ApiSecurityProperties settings = settings("ROLE_", List.of("roles"));
		JWTClaimConverter converter = new JWTClaimConverter(settings);

		List<Object> values = converter.getObjectStream(List.of("admin", "writer")).toList();

		assertThat(values).containsExactly("admin", "writer");
	}

	@Test
	void getObjectStream_shouldSplitString_whenValueContainsSpace() {
		ApiSecurityProperties settings = settings("ROLE_", List.of("roles"));
		JWTClaimConverter converter = new JWTClaimConverter(settings);

		List<Object> values = converter.getObjectStream("admin writer").toList();

		assertThat(values).containsExactly("admin", "writer");
	}

	@Test
	void getObjectStream_shouldReturnOriginalString_whenValueDoesNotContainSpace() {
		ApiSecurityProperties settings = settings("ROLE_", List.of("roles"));
		JWTClaimConverter converter = new JWTClaimConverter(settings);

		List<Object> values = converter.getObjectStream("admin").toList();

		assertThat(values).containsExactly("admin");
	}

	@Test
	void getObjectStream_shouldReturnEmptyStream_whenValueIsNull() {
		ApiSecurityProperties settings = settings("ROLE_", List.of("roles"));
		JWTClaimConverter converter = new JWTClaimConverter(settings);

		List<Object> values = converter.getObjectStream(null).toList();

		assertThat(values).isEmpty();
	}

	@Test
	void toRoleName_shouldReturnTrimmedRole_whenValueIsText() {
		ApiSecurityProperties settings = settings("ROLE_", List.of("roles"));
		JWTClaimConverter converter = new JWTClaimConverter(settings);

		Optional<String> role = converter.toRoleName(" admin ");

		assertThat(role).contains("admin");
	}

	@Test
	void toRoleName_shouldReturnEmptyOptional_whenValueIsBlankText() {
		ApiSecurityProperties settings = settings("ROLE_", List.of("roles"));
		JWTClaimConverter converter = new JWTClaimConverter(settings);

		Optional<String> role = converter.toRoleName(" ");

		assertThat(role).isEmpty();
	}

	@Test
	void toRoleName_shouldReturnEmptyOptional_whenValueIsNotText() {
		ApiSecurityProperties settings = settings("ROLE_", List.of("roles"));
		JWTClaimConverter converter = new JWTClaimConverter(settings);

		Optional<String> role = converter.toRoleName(123);

		assertThat(role).isEmpty();
	}

	@Test
	void toAuthority_shouldAddPrefix_whenRoleDoesNotStartWithPrefix() {
		ApiSecurityProperties settings = settings("ROLE_", List.of("roles"));
		JWTClaimConverter converter = new JWTClaimConverter(settings);

		GrantedAuthority authority = converter.toAuthority("admin");

		assertThat(authority.getAuthority()).isEqualTo("ROLE_admin");
	}

	@Test
	void toAuthority_shouldKeepRoleUnchanged_whenRoleStartsWithPrefix() {
		ApiSecurityProperties settings = settings("ROLE_", List.of("roles"));
		JWTClaimConverter converter = new JWTClaimConverter(settings);

		GrantedAuthority authority = converter.toAuthority("ROLE_admin");

		assertThat(authority.getAuthority()).isEqualTo("ROLE_admin");
	}

	@Test
	void toAuthority_shouldKeepRoleUnchanged_whenPrefixIsBlank() {
		ApiSecurityProperties settings = settings("", List.of("roles"));
		JWTClaimConverter converter = new JWTClaimConverter(settings);

		GrantedAuthority authority = converter.toAuthority("admin");

		assertThat(authority.getAuthority()).isEqualTo("admin");
	}

}
