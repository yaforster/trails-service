package io.github.yaforster.trails.core;

import lombok.Getter;

@Getter
public enum CapabilityLabel {

	USER_IMAGE_MAX_HEIGHT("The maximum supported pixel height of images uploaded as element screenshots"),
	USER_IMAGE_MAX_WIDTH("The maximum supported pixel width of images uploaded as element screenshots"),
	USER_IMAGE_MAX_SIZE_BYTES("The maximum file size of images uploaded as element screenshots in bytes"),
	SUPPORTED_IMAGE_FORMATS("The image formats supported for element screenshot uploads"),
	FILE_DOWNLOAD_TIMEOUT_MILLIS("The timeout for file downloads in milliseconds"),
	USER_FEATURES_ACTIVE("Whether user profile features are active");

	private final String description;

	CapabilityLabel(String description) {
		this.description = description;
	}

}
