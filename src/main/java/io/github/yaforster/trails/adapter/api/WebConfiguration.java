package io.github.yaforster.trails.adapter.api;

import lombok.AllArgsConstructor;
import org.jspecify.annotations.NonNull;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
@AllArgsConstructor
public class WebConfiguration implements WebMvcConfigurer {

	private final DemoResourceProperties properties;

	@Override
	public void addCorsMappings(@NonNull CorsRegistry registry) {
		registry.addMapping("/**")
			.allowedOriginPatterns("*")
			.allowedMethods("*")
			.allowedHeaders("*")
			.allowCredentials(false);
	}

	@Override
	public void addResourceHandlers(@NonNull ResourceHandlerRegistry registry) {
		if (properties.serveDemoResources()) {
			registry.addResourceHandler("/**").addResourceLocations("classpath:/META-INF/resources/");
		}
	}

}
