package io.github.yaforster.trails.adapter.test.webdriver.browser;

import java.nio.file.Path;

final class DownloadFileNamePolicy {

	private static final String[] TEMPORARY_SUFFIXES = { ".crdownload", ".part", ".tmp", ".download" };

	private DownloadFileNamePolicy() {
	}

	static boolean isBrowserTemporary(String name) {
		return name.startsWith(".com.microsoft.Edge.") || name.startsWith(".com.google.Chrome.")
				|| name.startsWith(".org.chromium.Chromium.");
	}

	static boolean matches(Path path, String reportedName) {
		String name = path.getFileName().toString();
		return name.equals(reportedName) || name.equals(finalizedName(reportedName));
	}

	static String artifactName(Path path) {
		return finalizedName(path.getFileName().toString());
	}

	private static String finalizedName(String name) {
		for (String suffix : TEMPORARY_SUFFIXES) {
			if (name.endsWith(suffix)) {
				return name.substring(0, name.length() - suffix.length());
			}
		}
		return name;
	}

}
