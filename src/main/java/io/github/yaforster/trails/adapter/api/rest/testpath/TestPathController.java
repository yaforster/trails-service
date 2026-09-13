package io.github.yaforster.trails.adapter.api.rest.testpath;

import io.github.yaforster.trails.adapter.api.rest.model.PagedTestPathResultDTO;
import io.github.yaforster.trails.adapter.api.rest.model.TestPathResultDTO;
import io.github.yaforster.trails.app.services.HierarchyValidationService;
import io.github.yaforster.trails.app.services.TestPathResultQueryService;
import lombok.AllArgsConstructor;
import org.openapitools.api.TestpathApi;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

@AllArgsConstructor
@RestController
public class TestPathController implements TestpathApi {

	private final TestPathResultQueryService testPathResultQueryService;

	private final HierarchyValidationService hierarchyValidationService;

	private final TestPathHATEOASFacade hateoasFacade;

	private final TestPathControllerValidator validator;

	@Override
	public ResponseEntity<PagedTestPathResultDTO> listPathResultsInTestSet(Long applicationId, Long stageId,
			Long testRunId, Long testSetResultId, Integer page, Integer size) {
		validator.validateListPathResultsInTestSet(applicationId, stageId, testRunId, testSetResultId, page, size);
		if (!hierarchyValidationService.testSetBelongsToRun(
				new HierarchyValidationService.TestSetHierarchy(applicationId, stageId, testRunId, testSetResultId))) {
			return ResponseEntity.notFound().build();
		}
		return testPathResultQueryService
			.listPathResults(new TestPathResultQueryService.TestSetPathPage(testSetResultId, page, size))
			.map(pagedResults -> hateoasFacade.toPagedDTOInHierarchy(applicationId, stageId, testRunId, testSetResultId,
					pagedResults))
			.map(ResponseEntity::ok)
			.orElse(ResponseEntity.notFound().build());
	}

	@Override
	public ResponseEntity<TestPathResultDTO> getPathResultInTestSet(Long applicationId, Long stageId, Long testRunId,
			Long testSetResultId, Long pathResultId) {
		validator.validateGetPathResultInTestSet(applicationId, stageId, testRunId, testSetResultId, pathResultId);
		if (!hierarchyValidationService.testSetBelongsToRun(
				new HierarchyValidationService.TestSetHierarchy(applicationId, stageId, testRunId, testSetResultId))) {
			return ResponseEntity.notFound().build();
		}
		return testPathResultQueryService.getPathResult(pathResultId)
			.filter(pathResult -> pathResult.testSetResultId().equals(testSetResultId))
			.map(pathResult -> hateoasFacade.toDTOInHierarchy(applicationId, stageId, testRunId, pathResult))
			.map(ResponseEntity::ok)
			.orElse(ResponseEntity.notFound().build());
	}

}
