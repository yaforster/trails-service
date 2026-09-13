package io.github.yaforster.trails.adapter.test.webdriver.browser;

import org.openqa.selenium.HasDownloads;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

final class DownloadStabilityTracker {

	private static final int REQUIRED_STABLE_POLLS = 3;

	private List<DownloadSnapshot> previous = Collections.emptyList();

	private int stablePolls;

	Optional<List<DownloadSnapshot>> observe(List<HasDownloads.DownloadedFile> files) {
		List<DownloadSnapshot> artifacts = files.stream()
			.map(DownloadSnapshot::from)
			.filter(file -> !DownloadFileNamePolicy.isBrowserTemporary(file.name()))
			.sorted(Comparator.comparing(DownloadSnapshot::name))
			.toList();
		if (artifacts.isEmpty()) {
			previous = Collections.emptyList();
			stablePolls = 0;
			return Optional.empty();
		}
		stablePolls = artifacts.equals(previous) ? stablePolls + 1 : 1;
		previous = artifacts;
		return stablePolls >= REQUIRED_STABLE_POLLS ? Optional.of(artifacts) : Optional.empty();
	}

}
