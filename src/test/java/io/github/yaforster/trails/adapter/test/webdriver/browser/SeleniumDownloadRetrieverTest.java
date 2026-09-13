package io.github.yaforster.trails.adapter.test.webdriver.browser;

import io.github.yaforster.trails.core.test.FileData;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.openqa.selenium.HasDownloads;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;

class SeleniumDownloadRetrieverTest {

	@Test
	void retrieve_returnsManagedDownloadContent_andUsesFinalizedArtifactName() throws Exception {
		HasDownloads downloads = Mockito.mock(HasDownloads.class);
		writeDownload(downloads, "report.pdf.crdownload", "report.pdf", new byte[] { 1, 2, 3 });

		Optional<FileData> result = new SeleniumDownloadRetriever(downloads).retrieve("report.pdf.crdownload");

		assertEquals("report.pdf", result.orElseThrow().fileName());
	}

	@Test
	void retrieve_returnsEmpty_whenManagedDownloadDoesNotContainMatchingFile() throws Exception {
		HasDownloads downloads = Mockito.mock(HasDownloads.class);
		writeDownload(downloads, "report.pdf", "other.pdf", new byte[] { 1 });

		assertTrue(new SeleniumDownloadRetriever(downloads).retrieve("report.pdf").isEmpty());
	}

	private static void writeDownload(HasDownloads downloads, String requestedName, String writtenName, byte[] content)
			throws Exception {
		Mockito.doAnswer(invocation -> {
			Path directory = invocation.getArgument(1);
			Files.write(directory.resolve(writtenName), content);
			return null;
		}).when(downloads).downloadFile(eq(requestedName), any(Path.class));
	}

}
