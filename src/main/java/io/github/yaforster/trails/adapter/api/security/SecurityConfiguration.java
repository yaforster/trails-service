package io.github.yaforster.trails.adapter.api.security;

import lombok.AllArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.annotation.web.configurers.oauth2.server.resource.OAuth2ResourceServerConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity(proxyTargetClass = true)
@AllArgsConstructor
public class SecurityConfiguration {

	private static final String API_ENDPOINTS = "/api/**";

	private static final String ACTUATOR_ENDPOINTS = "/actuator/**";

	private static final String CAPABILITIES_ENDPOINT = "/api/capabilities";

	private static final String USER_ENDPOINTS = "/user/**";

	private final ApiSecurityProperties properties;

	private final JWTClaimConverter jwtClaimConverter;

	@Bean
	@Order(1)
	public SecurityFilterChain publicSecurityFilterChain(HttpSecurity httpSecurity) throws Exception {
		configureCommonSecurity(httpSecurity);
		httpSecurity.securityMatcher(ACTUATOR_ENDPOINTS, CAPABILITIES_ENDPOINT)
			.authorizeHttpRequests(authorize -> authorize.anyRequest().permitAll());
		return httpSecurity.build();
	}

	@Bean
	@Order(2)
	public SecurityFilterChain apiSecurityFilterChain(HttpSecurity httpSecurity) throws Exception {
		configureCommonSecurity(httpSecurity);
		httpSecurity.securityMatcher(API_ENDPOINTS, USER_ENDPOINTS);
		configureApiAuthorization(httpSecurity);
		return httpSecurity.build();
	}

	@Bean
	@Order(3)
	public SecurityFilterChain defaultSecurityFilterChain(HttpSecurity httpSecurity) throws Exception {
		configureCommonSecurity(httpSecurity);
		httpSecurity.authorizeHttpRequests(authorize -> authorize.anyRequest().permitAll());
		return httpSecurity.build();
	}

	private void configureCommonSecurity(HttpSecurity httpSecurity) throws Exception {
		configureCors(httpSecurity);
		configureCsrf(httpSecurity);
		configureStatelessSessions(httpSecurity);
	}

	private void configureCors(HttpSecurity httpSecurity) throws Exception {
		httpSecurity.cors(Customizer.withDefaults());
	}

	private void configureCsrf(HttpSecurity httpSecurity) throws Exception {
		if (properties.csrfEnabled()) {
			httpSecurity.csrf(Customizer.withDefaults());
			return;
		}
		httpSecurity.csrf(AbstractHttpConfigurer::disable);
	}

	private void configureStatelessSessions(HttpSecurity httpSecurity) throws Exception {
		httpSecurity.sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS));
	}

	private void configureApiAuthorization(HttpSecurity httpSecurity) throws Exception {
		if (!properties.oauth2AuthenticationEnabled()) {
			httpSecurity.authorizeHttpRequests(authorize -> authorize.requestMatchers(HttpMethod.OPTIONS, "/**")
				.permitAll()
				.anyRequest()
				.permitAll());
			return;
		}
		httpSecurity
			.authorizeHttpRequests(authorize -> authorize.requestMatchers(HttpMethod.OPTIONS, "/**")
				.permitAll()
				.anyRequest()
				.authenticated())
			.oauth2ResourceServer(this::setJWTConverter);
	}

	private void setJWTConverter(OAuth2ResourceServerConfigurer<HttpSecurity> oauth2) {
		oauth2.jwt(jwt -> jwt.jwtAuthenticationConverter(jwtClaimConverter));
	}

}
