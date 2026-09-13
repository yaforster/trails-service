package io.github.yaforster.trails.adapter.api.rest.element;

import io.github.yaforster.trails.core.data.TrailsScreenshotFileBuilder;
import io.github.yaforster.trails.core.data.TrailsScreenshotProcessingException;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

class UserImageUploadConfigurationTest {

	private static final long FIVE_MEBIBYTES = 5L * 1024L * 1024L;

	private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
		.withUserConfiguration(UserImageUploadConfiguration.class);

	@Test
	void properties_shouldUseLegacyDefaultMaxSize_whenLimitIsOmitted() {
		contextRunner.run(context -> assertThat(properties(context).maxSize().toBytes()).isEqualTo(FIVE_MEBIBYTES));
	}

	@Test
	void properties_shouldUseLegacyDefaultMaxWidth_whenLimitIsOmitted() {
		contextRunner.run(context -> assertThat(properties(context).maxWidth()).isEqualTo(1920));
	}

	@Test
	void properties_shouldUseLegacyDefaultMaxHeight_whenLimitIsOmitted() {
		contextRunner.run(context -> assertThat(properties(context).maxHeight()).isEqualTo(1920));
	}

	@Test
	void properties_shouldUseLegacyDefaultMaxSize_whenLimitIsBlank() {
		contextRunner.withPropertyValues("service.files.user.img.max-size=")
			.run(context -> assertThat(properties(context).maxSize().toBytes()).isEqualTo(FIVE_MEBIBYTES));
	}

	@Test
	void properties_shouldUseLegacyDefaultMaxWidth_whenLimitIsBlank() {
		contextRunner.withPropertyValues("service.files.user.img.max-width=")
			.run(context -> assertThat(properties(context).maxWidth()).isEqualTo(1920));
	}

	@Test
	void properties_shouldUseLegacyDefaultMaxHeight_whenLimitIsBlank() {
		contextRunner.withPropertyValues("service.files.user.img.max-height=")
			.run(context -> assertThat(properties(context).maxHeight()).isEqualTo(1920));
	}

	@Test
	void properties_shouldUseLegacyDefaultMaxSize_whenLimitIsZero() {
		contextRunner.withPropertyValues("service.files.user.img.max-size=0B")
			.run(context -> assertThat(properties(context).maxSize().toBytes()).isEqualTo(FIVE_MEBIBYTES));
	}

	@Test
	void properties_shouldUseLegacyDefaultMaxWidth_whenLimitIsZero() {
		contextRunner.withPropertyValues("service.files.user.img.max-width=0")
			.run(context -> assertThat(properties(context).maxWidth()).isEqualTo(1920));
	}

	@Test
	void properties_shouldUseLegacyDefaultMaxHeight_whenLimitIsZero() {
		contextRunner.withPropertyValues("service.files.user.img.max-height=0")
			.run(context -> assertThat(properties(context).maxHeight()).isEqualTo(1920));
	}

	@Test
	void properties_shouldRetainNegativeMaxSize_whenConfigured() {
		contextRunner.withPropertyValues("service.files.user.img.max-size=-1B")
			.run(context -> assertThat(properties(context).maxSize().toBytes()).isEqualTo(-1L));
	}

	@Test
	void properties_shouldRetainNegativeMaxWidth_whenConfigured() {
		contextRunner.withPropertyValues("service.files.user.img.max-width=-1")
			.run(context -> assertThat(properties(context).maxWidth()).isEqualTo(-1));
	}

	@Test
	void properties_shouldRetainNegativeMaxHeight_whenConfigured() {
		contextRunner.withPropertyValues("service.files.user.img.max-height=-1")
			.run(context -> assertThat(properties(context).maxHeight()).isEqualTo(-1));
	}

	@Test
	void configuration_shouldComposeBuilderWithBoundMaxSize() {
		contextRunner.withPropertyValues("service.files.user.img.max-size=1B").run(context -> {
			TrailsScreenshotFileBuilder builder = context.getBean(TrailsScreenshotFileBuilder.class);

			TrailsScreenshotProcessingException exception = assertThrows(TrailsScreenshotProcessingException.class,
					() -> builder.build("image.png", new byte[] { 1, 2 }));

			assertThat(exception.getValidationViolation().code()).isEqualTo("ELEMENT_SCREENSHOT_FILE_TOO_LARGE");
		});
	}

	@Test
	void properties_shouldReportPropertyName_whenMaxSizeIsMalformed() {
		contextRunner.withPropertyValues("service.files.user.img.max-size=not-a-size")
			.run(context -> assertThat(bindingFailure(context.getStartupFailure()).getMessage())
				.contains("service.files.user.img.max-size"));
	}

	@Test
	void properties_shouldReportPropertyName_whenMaxWidthOverflows() {
		contextRunner.withPropertyValues("service.files.user.img.max-width=2147483648")
			.run(context -> assertThat(bindingFailure(context.getStartupFailure()).getMessage())
				.contains("service.files.user.img.max-width"));
	}

	private UserImageUploadProperties properties(
			org.springframework.boot.test.context.assertj.AssertableApplicationContext context) {
		return context.getBean(UserImageUploadProperties.class);
	}

	private Throwable bindingFailure(Throwable failure) {
		Throwable exception = failure;
		while (exception.getCause() != null && !exception.getMessage().startsWith("Failed to bind properties")) {
			exception = exception.getCause();
		}
		return exception;
	}

}
