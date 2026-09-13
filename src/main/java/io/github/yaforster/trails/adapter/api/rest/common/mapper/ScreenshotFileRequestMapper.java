package io.github.yaforster.trails.adapter.api.rest.common.mapper;

import io.github.yaforster.trails.adapter.api.APIRequestValidationException;
import io.github.yaforster.trails.core.data.TrailsScreenshotFile;
import io.github.yaforster.trails.core.data.TrailsScreenshotFileBuilder;
import io.github.yaforster.trails.core.data.TrailsScreenshotProcessingException;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@Component
@AllArgsConstructor
public class ScreenshotFileRequestMapper {

	private final TrailsScreenshotFileBuilder screenshotFileBuilder;

	public TrailsScreenshotFile from(MultipartFile file) {
		try {
			return screenshotFileBuilder.build(file.getOriginalFilename(), file.getBytes());
		}
		catch (IOException exception) {
			throw new APIRequestValidationException(
					TrailsScreenshotProcessingException.unreadableFile().getValidationViolation());
		}
		catch (TrailsScreenshotProcessingException exception) {
			throw new APIRequestValidationException(exception.getValidationViolation());
		}
	}

}
