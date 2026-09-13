package io.github.yaforster.trails.adapter.test.webdriver.browser;

import org.junit.jupiter.api.Test;
import org.openqa.selenium.HasDownloads;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DownloadStabilityTrackerTest {

	@Test
	void observe_returnsStableArtifacts_afterThreeEqualPolls() {
		DownloadStabilityTracker tracker = new DownloadStabilityTracker();
		List<HasDownloads.DownloadedFile> files = List.of(file("report.pdf", 10, 100));
		tracker.observe(files);
		tracker.observe(files);

		assertEquals(List.of(new DownloadSnapshot("report.pdf", 10, 100)), tracker.observe(files).orElseThrow());
	}

	@Test
	void observe_resetsStability_whenReportedSizeChanges() {
		DownloadStabilityTracker tracker = new DownloadStabilityTracker();
		tracker.observe(List.of(file("report.pdf", 10, 100)));
		tracker.observe(List.of(file("report.pdf", 11, 100)));
		tracker.observe(List.of(file("report.pdf", 11, 100)));

		assertTrue(tracker.observe(List.of(file("report.pdf", 11, 100))).isPresent());
	}

	@Test
	void observe_ignoresBrowserTemporaryArtifacts() {
		DownloadStabilityTracker tracker = new DownloadStabilityTracker();

		assertTrue(tracker.observe(List.of(file("report.pdf.crdownload", 10, 100))).isEmpty());
	}

	@Test
	void observe_sortsArtifacts_beforeComparingPolls() {
		DownloadStabilityTracker tracker = new DownloadStabilityTracker();
		tracker.observe(List.of(file("b.txt", 1, 1), file("a.txt", 1, 1)));
		tracker.observe(List.of(file("a.txt", 1, 1), file("b.txt", 1, 1)));

		assertEquals("a.txt",
				tracker.observe(List.of(file("a.txt", 1, 1), file("b.txt", 1, 1))).orElseThrow().getFirst().name());
	}

	private static HasDownloads.DownloadedFile file(String name, long size, long modified) {
		return new HasDownloads.DownloadedFile(name, 0, modified, size);
	}

}
