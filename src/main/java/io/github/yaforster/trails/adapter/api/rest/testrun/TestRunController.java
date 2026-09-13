package io.github.yaforster.trails.adapter.api.rest.testrun;

import io.github.yaforster.trails.adapter.api.rest.model.PagedTestRunDTO;
import io.github.yaforster.trails.adapter.api.rest.model.PagedTestRunHistoryDTO;
import io.github.yaforster.trails.adapter.api.rest.model.PersistedTestRunResultDTO;
import io.github.yaforster.trails.adapter.api.rest.model.TestRunStatisticsDTO;
import io.github.yaforster.trails.app.services.TestRunResultDatabaseService;
import lombok.AllArgsConstructor;
import org.openapitools.api.TestrunApi;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;

@AllArgsConstructor
@Controller
public class TestRunController implements TestrunApi {

	private final TestRunHATEOASFacade hateoasFacade;

	private final TestRunResultDatabaseService testRunResultDatabaseService;

	private final TestRunControllerValidator validator;

	@Override
	public ResponseEntity<PersistedTestRunResultDTO> getTestRun(Long testRunId, Long applicationId, Long stageId) {
		validator.validateGetTestRun(testRunId, applicationId, stageId);
		return testRunResultDatabaseService
			.getTestRunResult(new TestRunResultDatabaseService.TestRunResultDetails(applicationId, stageId, testRunId))
			.map(hateoasFacade::toDTO)
			.map(ResponseEntity::ok)
			.orElse(ResponseEntity.notFound().build());
	}

	@Override
	public ResponseEntity<PagedTestRunDTO> listTestRuns(Long applicationId, Long stageId, Integer page, Integer size) {
		validator.validateListTestRuns(applicationId, stageId, page, size);
		return testRunResultDatabaseService
			.listTestRuns(new TestRunResultDatabaseService.TestRunPage(applicationId, stageId, page, size))
			.map(paged -> hateoasFacade.toPagedDTO(applicationId, stageId, paged))
			.map(ResponseEntity::ok)
			.orElse(ResponseEntity.notFound().build());
	}

	@Override
	public ResponseEntity<TestRunStatisticsDTO> getTestRunStatistics(Long applicationId, Long stageId,
			Long testPlanId) {
		validator.validateGetTestRunStatistics(applicationId, stageId, testPlanId);
		return testRunResultDatabaseService
			.getTestRunStatistics(
					new TestRunResultDatabaseService.TestPlanStatistics(applicationId, stageId, testPlanId))
			.map(hateoasFacade::toStatisticsDTO)
			.map(ResponseEntity::ok)
			.orElse(ResponseEntity.notFound().build());
	}

	@Override
	public ResponseEntity<PagedTestRunHistoryDTO> getTestRunHistory(Long applicationId, Long stageId, Long testPlanId,
			Integer page, Integer size) {
		validator.validateGetTestRunHistory(applicationId, stageId, testPlanId, page, size);
		return testRunResultDatabaseService
			.getTestRunHistory(
					new TestRunResultDatabaseService.TestPlanHistory(applicationId, stageId, testPlanId, page, size))
			.map(hateoasFacade::toHistoryDTO)
			.map(ResponseEntity::ok)
			.orElse(ResponseEntity.notFound().build());
	}

}
