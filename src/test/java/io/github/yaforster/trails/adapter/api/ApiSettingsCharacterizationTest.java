package io.github.yaforster.trails.adapter.api;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.assertj.AssertableApplicationContext;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ApiSettingsCharacterizationTest {

	private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
		.withUserConfiguration(ApiPropertiesConfiguration.class);

	@Test
	void apiSettings_shouldBindTrueValues() {
		contextRunner
			.withPropertyValues("service.api.error-handling.add-stacktrace-to-response=true",
					"service.serve-demo-resources=true")
			.run(context -> assertThat(propertyValues(context)).containsExactly(true, true));
	}

	@Test
	void apiSettings_shouldBindFalseValues() {
		contextRunner
			.withPropertyValues("service.api.error-handling.add-stacktrace-to-response=false",
					"service.serve-demo-resources=false")
			.run(context -> assertThat(propertyValues(context)).containsExactly(false, false));
	}

	@Test
	void apiSettings_shouldKeepBlankValuesDisabled() {
		contextRunner
			.withPropertyValues("service.api.error-handling.add-stacktrace-to-response=",
					"service.serve-demo-resources=")
			.run(context -> assertThat(propertyValues(context)).containsExactly(false, false));
	}

	@Test
	void apiSettings_shouldKeepMissingValuesDisabled() {
		contextRunner.run(context -> assertThat(propertyValues(context)).containsExactly(false, false));
	}

	private List<Boolean> propertyValues(AssertableApplicationContext context) {
		ApiErrorHandlingProperties errorHandlingProperties = context.getBean(ApiErrorHandlingProperties.class);
		DemoResourceProperties demoResourceProperties = context.getBean(DemoResourceProperties.class);
		return List.of(errorHandlingProperties.addStacktraceToResponse(), demoResourceProperties.serveDemoResources());
	}

}
