package io.github.yaforster.trails.adapter.api.rest.common.mapper;

import io.github.yaforster.trails.adapter.api.APIRequestValidationException;
import io.github.yaforster.trails.core.ValidationViolation;
import io.github.yaforster.trails.core.data.TrailsScreenshotFileBuilder;
import io.github.yaforster.trails.core.data.TrailsScreenshotProcessingException;
import org.junit.jupiter.api.Test;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ScreenshotFileRequestMapperTest {

	@Test
	void from_shouldReturnBadRequestValidation_whenFileCannotBeRead() throws IOException {
		TrailsScreenshotFileBuilder screenshotFileBuilder = mock(TrailsScreenshotFileBuilder.class);
		ScreenshotFileRequestMapper mapper = new ScreenshotFileRequestMapper(screenshotFileBuilder);
		MultipartFile file = mock(MultipartFile.class);
		when(file.getBytes()).thenThrow(new IOException("broken upload"));

		APIRequestValidationException exception = assertThrows(APIRequestValidationException.class,
				() -> mapper.from(file));

		assertThat(exception.getValidationViolations()).containsExactly(new ValidationViolation(
				"ELEMENT_SCREENSHOT_FILE_UNREADABLE", "Element screenshot file could not be read.", "/file"));
	}

	@Test
	void from_shouldReturnBadRequestValidation_whenScreenshotFileCannotBeProcessed() throws IOException {
		TrailsScreenshotFileBuilder screenshotFileBuilder = mock(TrailsScreenshotFileBuilder.class);
		ScreenshotFileRequestMapper mapper = new ScreenshotFileRequestMapper(screenshotFileBuilder);
		MultipartFile file = mock(MultipartFile.class);
		byte[] content = "hello".getBytes();
		when(file.getOriginalFilename()).thenReturn("notes.txt");
		when(file.getBytes()).thenReturn(content);
		when(screenshotFileBuilder.build("notes.txt", content)).thenThrow(new TrailsScreenshotProcessingException(
				new ValidationViolation("ELEMENT_SCREENSHOT_FILE_TYPE_UNSUPPORTED",
						"Element screenshot file must be a PNG, JPEG, GIF, BMP, or WebP image.", "/file")));

		APIRequestValidationException exception = assertThrows(APIRequestValidationException.class,
				() -> mapper.from(file));

		assertThat(exception.getValidationViolations())
			.containsExactly(new ValidationViolation("ELEMENT_SCREENSHOT_FILE_TYPE_UNSUPPORTED",
					"Element screenshot file must be a PNG, JPEG, GIF, BMP, or WebP image.", "/file"));
	}

}
