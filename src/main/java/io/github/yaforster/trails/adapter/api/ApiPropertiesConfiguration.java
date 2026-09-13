package io.github.yaforster.trails.adapter.api;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties({ ApiErrorHandlingProperties.class, DemoResourceProperties.class })
public class ApiPropertiesConfiguration {

}
