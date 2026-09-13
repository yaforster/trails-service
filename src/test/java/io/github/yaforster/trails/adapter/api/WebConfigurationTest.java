package io.github.yaforster.trails.adapter.api;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.web.servlet.config.annotation.CorsRegistration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;

import static org.mockito.Mockito.*;

class WebConfigurationTest {

	@Test
	void addCorsMappings_RegistersCredentialFreeWildcardMapping() {
		DemoResourceProperties settings = mock(DemoResourceProperties.class);
		CorsRegistry registry = mock(CorsRegistry.class);
		CorsRegistration registration = mock(CorsRegistration.class);
		when(registry.addMapping("/**")).thenReturn(registration);
		when(registration.allowedOriginPatterns("*")).thenReturn(registration);
		when(registration.allowedMethods("*")).thenReturn(registration);
		when(registration.allowedHeaders("*")).thenReturn(registration);
		when(registration.allowCredentials(false)).thenReturn(registration);
		WebConfiguration webConfiguration = new WebConfiguration(settings);

		webConfiguration.addCorsMappings(registry);

		verify(registry, times(1)).addMapping("/**");
		verify(registration, times(1)).allowedOriginPatterns("*");
		verify(registration, times(1)).allowedMethods("*");
		verify(registration, times(1)).allowedHeaders("*");
		verify(registration, times(1)).allowCredentials(false);
	}

}
