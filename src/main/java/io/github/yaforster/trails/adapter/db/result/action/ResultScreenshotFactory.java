package io.github.yaforster.trails.adapter.db.result.action;

import org.springframework.stereotype.Component;

import java.util.Base64;
import java.util.Optional;

@Component
public class ResultScreenshotFactory {

	public Optional<ResultScreenshotEntity> fromBase64(Long resultId, String base64Screenshot) {
		ScreenshotPayload screenshotPayload = parseScreenshot(base64Screenshot);
		if (screenshotPayload.content() == null) {
			return Optional.empty();
		}

		String fileName = "action-result-" + resultId + fileExtensionFor(screenshotPayload.contentType());
		ResultScreenshotEntity screenshotEntity = new ResultScreenshotEntity().setResultId(resultId)
			.setFileName(fileName)
			.setContentType(screenshotPayload.contentType())
			.setContent(screenshotPayload.content());
		return Optional.of(screenshotEntity);
	}

	private ScreenshotPayload parseScreenshot(String base64Screenshot) {
		if (base64Screenshot == null || base64Screenshot.isBlank()) {
			return ScreenshotPayload.empty();
		}

		String screenshotValue = base64Screenshot.trim();
		String contentType = "image/png";

		int commaIndex = screenshotValue.indexOf(',');
		if (screenshotValue.startsWith("data:") && commaIndex > 0) {
			String metadata = screenshotValue.substring(5, commaIndex);
			String[] metadataParts = metadata.split(";");
			if (metadataParts.length > 0 && !metadataParts[0].isBlank()) {
				contentType = metadataParts[0];
			}
			screenshotValue = screenshotValue.substring(commaIndex + 1);
		}

		try {
			byte[] decoded = Base64.getDecoder().decode(screenshotValue);
			if (decoded.length == 0) {
				return ScreenshotPayload.empty();
			}
			return new ScreenshotPayload(decoded, contentType);
		}
		catch (IllegalArgumentException ignored) {
			return ScreenshotPayload.empty();
		}
	}

	private String fileExtensionFor(String contentType) {
		return switch (contentType.toLowerCase()) {
			case "image/jpeg", "image/jpg" -> ".jpg";
			case "image/webp" -> ".webp";
			case "image/gif" -> ".gif";
			default -> ".png";
		};
	}

	private record ScreenshotPayload(byte[] content, String contentType) {

		private static ScreenshotPayload empty() {
			return new ScreenshotPayload(null, null);
		}
	}

}
