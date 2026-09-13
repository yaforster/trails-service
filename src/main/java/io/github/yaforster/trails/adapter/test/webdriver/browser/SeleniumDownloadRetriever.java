package io.github.yaforster.trails.adapter.test.webdriver.browser;

import io.github.yaforster.trails.core.test.FileData;
import org.openqa.selenium.HasDownloads;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;
import java.util.stream.Stream;

final class SeleniumDownloadRetriever {

	private final HasDownloads downloads;

	SeleniumDownloadRetriever(HasDownloads downloads) {
		this.downloads = downloads;
	}

	Optional<FileData> retrieve(String fileName) {
		Path directory = null;
		try {
			directory = Files.createTempDirectory("trails-managed-download-");
			downloads.downloadFile(fileName, directory);
			try (Stream<Path> paths = Files.walk(directory)) {
				Optional<Path> downloaded = paths.filter(Files::isRegularFile)
					.filter(path -> DownloadFileNamePolicy.matches(path, fileName))
					.findFirst();
				return downloaded.map(path -> read(path));
			}
		}
		catch (Exception ignored) {
			return Optional.empty();
		}
		finally {
			delete(directory);
		}
	}

	private FileData read(Path path) {
		try {
			return new FileData(Files.readAllBytes(path), DownloadFileNamePolicy.artifactName(path));
		}
		catch (IOException exception) {
			throw new IllegalStateException(exception);
		}
	}

	private void delete(Path directory) {
		if (directory == null) {
			return;
		}
		try (Stream<Path> paths = Files.walk(directory)) {
			paths.sorted((left, right) -> right.getNameCount() - left.getNameCount()).forEach(path -> {
				try {
					Files.deleteIfExists(path);
				}
				catch (IOException ignored) {
				}
			});
		}
		catch (IOException ignored) {
		}
	}

}
