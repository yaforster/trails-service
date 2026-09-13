package io.github.yaforster.trails.adapter.api.rest.element;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.util.unit.DataSize;

@ConfigurationProperties("service.files.user.img")
public record UserImageUploadProperties(DataSize maxSize, Integer maxWidth, Integer maxHeight) {

	private static final DataSize DEFAULT_MAX_SIZE = DataSize.ofBytes(5L * 1024L * 1024L);

	private static final int DEFAULT_MAX_DIMENSION = 1920;

	public UserImageUploadProperties {
		maxSize = maxSize == null || maxSize.toBytes() == 0 ? DEFAULT_MAX_SIZE : maxSize;
		maxWidth = maxWidth == null || maxWidth == 0 ? DEFAULT_MAX_DIMENSION : maxWidth;
		maxHeight = maxHeight == null || maxHeight == 0 ? DEFAULT_MAX_DIMENSION : maxHeight;
	}

}
