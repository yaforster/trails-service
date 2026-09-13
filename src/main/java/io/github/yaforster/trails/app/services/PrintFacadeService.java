package io.github.yaforster.trails.app.services;

import io.github.yaforster.trails.core.GeneratedFile;

import java.util.Optional;

public interface PrintFacadeService {

	Optional<GeneratedFile> printPath(TestPathPrint testPathPrint);

	Optional<GeneratedFile> printTestSet(TestSetPrint testSetPrint);

	Optional<GeneratedFile> printTestRun(TestRunPrint testRunPrint);

	record TestPathPrint(Long applicationId, Long stageId, Long testRunId, Long testSetResultId, Long pathResultId) {
	}

	record TestSetPrint(Long applicationId, Long stageId, Long testRunId, Long testSetResultId) {
	}

	record TestRunPrint(Long applicationId, Long stageId, Long testRunId) {
	}

}
