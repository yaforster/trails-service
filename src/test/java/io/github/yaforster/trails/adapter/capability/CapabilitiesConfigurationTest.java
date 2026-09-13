package io.github.yaforster.trails.adapter.capability;

import io.github.yaforster.trails.adapter.api.rest.element.UserImageUploadConfiguration;
import io.github.yaforster.trails.adapter.api.security.SecurityPropertiesConfiguration;
import io.github.yaforster.trails.adapter.test.webdriver.WebdriverConfiguration;
import io.github.yaforster.trails.app.services.CapabilitiesService;
import io.github.yaforster.trails.app.services.CapabilityContribution;
import io.github.yaforster.trails.app.services.CapabilityContributor;
import io.github.yaforster.trails.core.Capability;
import io.github.yaforster.trails.core.CapabilityLabel;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class CapabilitiesConfigurationTest {

	@Test
	void capabilities_shouldUseBoundFeatureProperties() {
		new ApplicationContextRunner()
			.withUserConfiguration(UserImageUploadConfiguration.class, SecurityPropertiesConfiguration.class,
					WebdriverConfiguration.class, CapabilitiesConfiguration.class)
			.withPropertyValues("service.files.user.img.max-size=3MB", "service.files.user.img.max-width=1234",
					"service.files.user.img.max-height=567", "service.webdrivers.general.grid-url=http://selenium:4444",
					"service.webdrivers.general.file-download.timeout-millis=123",
					"service.api.security.oauth2.enabled=true",
					"service.api.security.roles.trails-test-manager=test-manager",
					"service.api.security.roles.trails-tester=tester", "service.api.security.roles.trails-admin=admin",
					"service.api.security.deployments.role=deployer",
					"spring.security.oauth2.resourceserver.jwt.issuer-uri=https://issuer")
			.run(context -> assertThat(context.getBean(CapabilitiesService.class).getCapabilities()).containsExactly(
					new Capability(CapabilityLabel.USER_IMAGE_MAX_HEIGHT, "567"),
					new Capability(CapabilityLabel.USER_IMAGE_MAX_WIDTH, "1234"),
					new Capability(CapabilityLabel.USER_IMAGE_MAX_SIZE_BYTES, String.valueOf(3L * 1024L * 1024L)),
					new Capability(CapabilityLabel.SUPPORTED_IMAGE_FORMATS, "PNG,JPEG,GIF,BMP,WEBP"),
					new Capability(CapabilityLabel.FILE_DOWNLOAD_TIMEOUT_MILLIS, "123"),
					new Capability(CapabilityLabel.USER_FEATURES_ACTIVE, "true")));
	}

	@Test
	void startup_shouldFail_whenARequiredCapabilityContributionIsMissing() {
		new ApplicationContextRunner().withUserConfiguration(CapabilitiesConfiguration.class)
			.run(context -> assertThat(context.getStartupFailure()).hasMessageContaining("Missing capability labels"));
	}

	@Test
	void startup_shouldFail_whenACapabilityLabelIsContributedTwice() {
		new ApplicationContextRunner()
			.withUserConfiguration(CapabilitiesConfiguration.class, DuplicateCapabilityContributorsConfiguration.class)
			.run(context -> assertThat(context.getStartupFailure())
				.hasMessageContaining("Duplicate capability labels"));
	}

	@Configuration(proxyBeanMethods = false)
	static class DuplicateCapabilityContributorsConfiguration {

		@Bean
		CapabilityContributor firstContributor() {
			return () -> List.of(new CapabilityContribution(CapabilityLabel.USER_FEATURES_ACTIVE, "true"));
		}

		@Bean
		CapabilityContributor secondContributor() {
			return () -> List.of(new CapabilityContribution(CapabilityLabel.USER_FEATURES_ACTIVE, "false"));
		}

	}

}
