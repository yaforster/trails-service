package io.github.yaforster.trails.adapter.test.webdriver.browser;

import io.github.yaforster.trails.core.test.FileData;
import org.openqa.selenium.HasDownloads;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

final class SeleniumDownloadCollector {

	private final WebDriver driver;

	private final HasDownloads downloads;

	private final int timeoutMillis;

	private final DownloadStabilityTracker tracker = new DownloadStabilityTracker();

	SeleniumDownloadCollector(WebDriver driver, HasDownloads downloads, int timeoutMillis) {
		this.driver = driver;
		this.downloads = downloads;
		this.timeoutMillis = timeoutMillis;
	}

	List<FileData> getDownloadedFiles() {
		try {
			List<DownloadSnapshot> files = new WebDriverWait(driver, Duration.ofMillis(timeoutMillis))
				.pollingEvery(Duration.ofMillis(100))
				.until(ignored -> stableFilesOrNull());
			return files.stream()
				.map(DownloadSnapshot::name)
				.map(this::retrieve)
				.filter(Optional::isPresent)
				.map(Optional::get)
				.toList();
		}
		catch (TimeoutException ignored) {
			return Collections.emptyList();
		}
	}

	private List<DownloadSnapshot> stableFilesOrNull() {
		return tracker.observe(downloads.getDownloadedFiles()).orElse(null);
	}

	private Optional<FileData> retrieve(String fileName) {
		return new SeleniumDownloadRetriever(downloads).retrieve(fileName);
	}

}
