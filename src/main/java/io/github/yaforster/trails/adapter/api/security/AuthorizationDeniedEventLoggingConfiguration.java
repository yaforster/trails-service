package io.github.yaforster.trails.adapter.api.security;

import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authorization.AuthorizationEventPublisher;
import org.springframework.security.authorization.SpringAuthorizationEventPublisher;

@Configuration
@ConditionalOnProperty(name = "service.api.security.oauth2.enabled", havingValue = "true")
public class AuthorizationDeniedEventLoggingConfiguration {

	/**
	 * Publishes Spring Security authorization decisions as Spring application events so
	 * AuthorizationDeniedEventLogger can log rejected access attempts. The conditional
	 * keeps this as the default while still allowing tests or deployments to provide a
	 * custom publisher.
	 */
	@Bean
	@ConditionalOnMissingBean(AuthorizationEventPublisher.class)
	AuthorizationEventPublisher authorizationEventPublisher(ApplicationEventPublisher eventPublisher) {
		return new SpringAuthorizationEventPublisher(eventPublisher);
	}

}
