package io.github.yaforster.trails.integration.api;

import org.junit.jupiter.api.Test;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class CapabilitiesApiIT extends RestApiIntegrationTestBase {

	@Test
	void listCapabilities_shouldReturnCanonicalCapabilityResponse() throws Exception {
		performGet("/capabilities").andExpect(status().isOk())
			.andExpect(content()
				.string("""
						[{"name":"USER_IMAGE_MAX_HEIGHT","description":"The maximum supported pixel height of images uploaded as element screenshots","value":"1920"},{"name":"USER_IMAGE_MAX_WIDTH","description":"The maximum supported pixel width of images uploaded as element screenshots","value":"1920"},{"name":"USER_IMAGE_MAX_SIZE_BYTES","description":"The maximum file size of images uploaded as element screenshots in bytes","value":"5242880"},{"name":"SUPPORTED_IMAGE_FORMATS","description":"The image formats supported for element screenshot uploads","value":"PNG,JPEG,GIF,BMP,WEBP"},{"name":"FILE_DOWNLOAD_TIMEOUT_MILLIS","description":"The timeout for file downloads in milliseconds","value":"30000"},{"name":"USER_FEATURES_ACTIVE","description":"Whether user profile features are active","value":"false"}]"""));
	}

}
