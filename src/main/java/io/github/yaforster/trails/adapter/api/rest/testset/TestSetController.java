package io.github.yaforster.trails.adapter.api.rest.testset;

import io.github.yaforster.trails.adapter.api.rest.model.PagedTestSetResultDTO;
import io.github.yaforster.trails.adapter.api.rest.model.TestSetResultDTO;
import io.github.yaforster.trails.app.services.TestSetResultQueryService;
import lombok.AllArgsConstructor;
import org.openapitools.api.TestsetApi;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

@AllArgsConstructor
@RestController
public class TestSetController implements TestsetApi {

	private final TestSetResultQueryService testSetResultQueryService;

	private final TestSetHATEOASFacade hateoasFacade;

	private final TestSetControllerValidator validator;

	@Override
	public ResponseEntity<PagedTestSetResultDTO> listBrowserResults(Long testRunId, Long applicationId, Long stageId,
			Integer page, Integer size) {
		validator.validateListBrowserResults(testRunId, applicationId, stageId, page, size);
		return testSetResultQueryService
			.listBrowserResults(
					new TestSetResultQueryService.TestRunBrowserResults(applicationId, stageId, testRunId, page, size))
			.map(pagedResults -> hateoasFacade.toPagedDTO(applicationId, stageId, testRunId, pagedResults))
			.map(ResponseEntity::ok)
			.orElse(ResponseEntity.notFound().build());
	}

	@Override
	public ResponseEntity<TestSetResultDTO> getBrowserResultInTestRun(Long testRunId, Long applicationId, Long stageId,
			Long testSetResultId) {
		validator.validateGetBrowserResultInTestRun(testRunId, applicationId, stageId, testSetResultId);
		return testSetResultQueryService.getTestSetResult(testSetResultId)
			.filter(result -> result.applicationId().equals(applicationId) && result.stageId().equals(stageId)
					&& result.testRunId().equals(testRunId))
			.map(hateoasFacade::toDTO)
			.map(ResponseEntity::ok)
			.orElse(ResponseEntity.notFound().build());
	}

}
