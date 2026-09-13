package io.github.yaforster.trails.adapter.api.rest.element;

import io.github.yaforster.trails.app.services.CapabilityContribution;
import io.github.yaforster.trails.app.services.CapabilityContributor;
import io.github.yaforster.trails.core.CapabilityLabel;
import io.github.yaforster.trails.core.data.ImageType;
import io.github.yaforster.trails.core.data.ImageTypeDetector;
import io.github.yaforster.trails.core.data.TrailsScreenshotFileBuilder;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;
import java.util.stream.Collectors;

@Configuration
@EnableConfigurationProperties(UserImageUploadProperties.class)
public class UserImageUploadConfiguration {

	@Bean
	TrailsScreenshotFileBuilder trailsScreenshotFileBuilder(UserImageUploadProperties properties) {
		return new TrailsScreenshotFileBuilder(properties.maxSize().toBytes(), properties.maxWidth(),
				properties.maxHeight());
	}

	@Bean
	CapabilityContributor userImageCapabilityContributor(UserImageUploadProperties properties) {
		String supportedImageFormats = ImageTypeDetector.supportedImageTypes()
			.stream()
			.map(ImageType::name)
			.collect(Collectors.joining(","));
		return () -> List.of(
				new CapabilityContribution(CapabilityLabel.USER_IMAGE_MAX_HEIGHT,
						String.valueOf(properties.maxHeight())),
				new CapabilityContribution(CapabilityLabel.USER_IMAGE_MAX_WIDTH, String.valueOf(properties.maxWidth())),
				new CapabilityContribution(CapabilityLabel.USER_IMAGE_MAX_SIZE_BYTES,
						String.valueOf(properties.maxSize().toBytes())),
				new CapabilityContribution(CapabilityLabel.SUPPORTED_IMAGE_FORMATS, supportedImageFormats));
	}

}
