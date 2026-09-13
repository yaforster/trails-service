package io.github.yaforster.trails.adapter.db.result.run;

import io.github.yaforster.trails.app.services.ActionResultPersistence;
import io.github.yaforster.trails.app.services.DownloadedFilePersistence;
import io.github.yaforster.trails.app.services.TestPathResultPersistence;
import io.github.yaforster.trails.app.services.TestSetResultPersistence;
import io.github.yaforster.trails.core.test.*;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@AllArgsConstructor
public class TestRunResultWriter {

	private final TestSetResultPersistence testSetResultPersistence;

	private final TestPathResultPersistence testPathResultPersistence;

	private final ActionResultPersistence actionResultPersistence;

	private final DownloadedFilePersistence downloadedFilePersistence;

	public void writeResults(Long testRunId, List<TestSetResult> testSetResults) {
		if (testSetResults == null || testSetResults.isEmpty()) {
			return;
		}
		for (TestSetResult testSetResult : testSetResults) {
			Long testSetResultId = testSetResultPersistence
				.store(new TestSetResultPersistence.TestSetResultToStore(testRunId, testSetResult));
			writePathResults(testSetResultId, testSetResult.testCaseResult());
		}
	}

	private void writePathResults(Long testSetResultId, TestCaseResult testCaseResult) {
		if (testCaseResult == null || testCaseResult.results() == null || testCaseResult.results().isEmpty()) {
			return;
		}

		int pathIndex = 1;
		for (TestPathResult testPathResult : testCaseResult.results()) {
			Long testPathResultId = testPathResultPersistence
				.store(new TestPathResultPersistence.NamedTestPath(testSetResultId, "Path " + pathIndex++));

			writeActionResults(testPathResultId, testPathResult.results());
			writeDownloadedFiles(testPathResultId, testPathResult.downloadedFiles());
		}
	}

	private void writeActionResults(Long testPathResultId, List<Result> results) {
		if (results == null || results.isEmpty()) {
			return;
		}
		for (int executionOrder = 0; executionOrder < results.size(); executionOrder++) {
			Result result = results.get(executionOrder);
			actionResultPersistence
				.store(new ActionResultPersistence.ActionResult(testPathResultId, executionOrder, result));
		}
	}

	private void writeDownloadedFiles(Long testPathResultId, List<FileData> downloadedFiles) {
		if (downloadedFiles == null || downloadedFiles.isEmpty()) {
			return;
		}
		for (FileData fileData : downloadedFiles) {
			downloadedFilePersistence.store(new DownloadedFilePersistence.DownloadedFile(testPathResultId, fileData));
		}
	}

}
