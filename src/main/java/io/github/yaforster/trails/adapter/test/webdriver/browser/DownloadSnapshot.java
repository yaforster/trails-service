package io.github.yaforster.trails.adapter.test.webdriver.browser;

import org.openqa.selenium.HasDownloads;

record DownloadSnapshot(String name, long size, long lastModifiedTime) {

	static DownloadSnapshot from(HasDownloads.DownloadedFile file) {
		return new DownloadSnapshot(file.getName(), file.getSize(), file.getLastModifiedTime());
	}
}
