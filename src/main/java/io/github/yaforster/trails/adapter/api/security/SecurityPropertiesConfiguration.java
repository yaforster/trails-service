package io.github.yaforster.trails.adapter.api.security;

import io.github.yaforster.trails.app.services.CapabilityContribution;
import io.github.yaforster.trails.app.services.CapabilityContributor;
import io.github.yaforster.trails.core.CapabilityLabel;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration(proxyBeanMethods = false)
@EnableConfigurationProperties({ ApiSecurityProperties.class, JwtDecoderProperties.class })
public class SecurityPropertiesConfiguration {

	@Bean
	SecurityPropertiesValidator securityPropertiesValidator(ApiSecurityProperties properties,
			JwtDecoderProperties decoderProperties) {
		return new SecurityPropertiesValidator(properties, decoderProperties);
	}

	@Bean
	CapabilityContributor userFeaturesCapabilityContributor(ApiSecurityProperties properties) {
		return () -> List.of(new CapabilityContribution(CapabilityLabel.USER_FEATURES_ACTIVE,
				String.valueOf(properties.oauth2AuthenticationEnabled())));
	}

}
