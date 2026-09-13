package io.github.yaforster.trails.adapter.api;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("service")
public record DemoResourceProperties(Boolean serveDemoResources) {

	public DemoResourceProperties {
		serveDemoResources = Boolean.TRUE.equals(serveDemoResources);
	}

}
