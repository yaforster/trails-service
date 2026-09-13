package io.github.yaforster.trails.adapter.api.rest.application;

import io.github.yaforster.trails.app.services.ApplicationDatabaseService;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

import static org.mockito.Mockito.mock;

@TestConfiguration
@EnableWebMvc
class ApplicationControllerSecurityTestConfiguration {

	@Bean
	ApplicationDatabaseService applicationDatabaseService() {
		return mock(ApplicationDatabaseService.class);
	}

	@Bean
	ApplicationHATEOASFacade applicationHATEOASFacade() {
		return mock(ApplicationHATEOASFacade.class);
	}

	@Bean
	ApplicationControllerValidator applicationControllerValidator() {
		return mock(ApplicationControllerValidator.class);
	}

}
