package io.github.yaforster.trails.core.test;

import java.util.Collections;
import java.util.List;

/**
 * Run-scoped context passed to actions while a single test path is executed.
 * <p>
 * The context exposes framework-free browser, download, and document execution ports and
 * caches managed downloads once per test path before persistence.
 */
public class TestExecutionContext {

	private final BrowserExecution execution;

	private List<FileData> downloadedFiles;

	public TestExecutionContext(BrowserExecution execution) {
		this.execution = execution;
	}

	public BrowserSession browser() {
		return execution.browser();
	}

	public io.github.yaforster.trails.core.test.action.document.DocumentExecution documents() {
		return execution.documents();
	}

	public List<FileData> downloadedFiles() {
		if (downloadedFiles == null) {
			return refreshDownloadedFiles();
		}
		return downloadedFiles;
	}

	public List<FileData> refreshDownloadedFiles() {
		downloadedFiles = downloadFiles();
		return downloadedFiles;
	}

	private List<FileData> downloadFiles() {
		List<FileData> files = browser().refreshDownloadedFiles();
		return files == null ? Collections.emptyList() : files;
	}

}
