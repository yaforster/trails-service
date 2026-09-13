package io.github.yaforster.trails.adapter.capability;

import io.github.yaforster.trails.app.services.CapabilitiesService;
import io.github.yaforster.trails.app.services.CapabilityContributor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration(proxyBeanMethods = false)
public class CapabilitiesConfiguration {

	@Bean
	CapabilitiesService capabilitiesService(List<CapabilityContributor> contributors) {
		return new CapabilitiesServiceImpl(contributors);
	}

}
