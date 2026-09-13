package io.github.yaforster.trails.adapter.api.rest.element.mapper;

import io.github.yaforster.trails.adapter.api.rest.model.ArtifactDTO;
import io.github.yaforster.trails.core.TrailsTest;
import io.github.yaforster.trails.core.data.TrailsScreenshotFile;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ScreenshotArtifactDTOMapperTest extends TrailsTest {

	@Test
	void toDTO_shouldMapScreenshotId() {
		ScreenshotArtifactDTOMapper mapper = new ScreenshotArtifactDTOMapper();
		TrailsScreenshotFile file = screenshotFile("element.jpg", jpegBytes());

		ArtifactDTO dto = mapper.toDTO(1L, 2L, 9L, 42L, file);

		assertThat(dto.getId()).isEqualTo(42L);
	}

	@Test
	void toDTO_shouldMapScreenshotType() {
		ScreenshotArtifactDTOMapper mapper = new ScreenshotArtifactDTOMapper();
		TrailsScreenshotFile file = screenshotFile("element.jpg", jpegBytes());

		ArtifactDTO dto = mapper.toDTO(1L, 2L, 9L, 42L, file);

		assertThat(dto.getType()).isEqualTo(ArtifactDTO.TypeEnum.SCREENSHOT);
	}

	@Test
	void toDTO_shouldMapFilename() {
		ScreenshotArtifactDTOMapper mapper = new ScreenshotArtifactDTOMapper();
		TrailsScreenshotFile file = screenshotFile("element.jpg", jpegBytes());

		ArtifactDTO dto = mapper.toDTO(1L, 2L, 9L, 42L, file);

		assertThat(dto.getFilename()).isEqualTo("element.jpg");
	}

	@Test
	void toDTO_shouldMapSelfLinkToElementScreenshot() {
		ScreenshotArtifactDTOMapper mapper = new ScreenshotArtifactDTOMapper();
		TrailsScreenshotFile file = screenshotFile("element.jpg", jpegBytes());

		ArtifactDTO dto = mapper.toDTO(1L, 2L, 9L, 42L, file);

		assertThat(dto.getLinks().get("self").getHref()).contains("/applications/1/stages/2/elements/9/screenshot");
	}

}
