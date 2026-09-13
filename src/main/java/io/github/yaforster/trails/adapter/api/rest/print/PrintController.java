package io.github.yaforster.trails.adapter.api.rest.print;

import io.github.yaforster.trails.app.services.PrintFacadeService;
import lombok.AllArgsConstructor;
import org.openapitools.api.PrintApi;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

@AllArgsConstructor
@RestController
public class PrintController implements PrintApi {

	private final PrintFacadeService printFacadeService;

	private final PrintResponseMapper responseMapper;

	private final PrintControllerValidator validator;

	@Override
	public ResponseEntity<Resource> printPathResultInTestSet(Long applicationId, Long stageId, Long testRunId,
			Long testSetResultId, Long pathResultId) {
		validator.validatePrintPathResultInTestSet(applicationId, stageId, testRunId, testSetResultId, pathResultId);
		return printFacadeService
			.printPath(new PrintFacadeService.TestPathPrint(applicationId, stageId, testRunId, testSetResultId,
					pathResultId))
			.map(responseMapper::toPdfResponse)
			.orElse(ResponseEntity.notFound().build());
	}

	@Override
	public ResponseEntity<Resource> printTestSetResultInTestRun(Long applicationId, Long stageId, Long testRunId,
			Long testSetResultId) {
		validator.validatePrintTestSetResultInTestRun(applicationId, stageId, testRunId, testSetResultId);
		return printFacadeService
			.printTestSet(new PrintFacadeService.TestSetPrint(applicationId, stageId, testRunId, testSetResultId))
			.map(responseMapper::toPdfResponse)
			.orElse(ResponseEntity.notFound().build());
	}

	@Override
	public ResponseEntity<Resource> printTestRun(Long applicationId, Long stageId, Long testRunId) {
		validator.validatePrintTestRun(applicationId, stageId, testRunId);
		return printFacadeService.printTestRun(new PrintFacadeService.TestRunPrint(applicationId, stageId, testRunId))
			.map(responseMapper::toPdfResponse)
			.orElse(ResponseEntity.notFound().build());
	}

}
