package io.github.yaforster.trails.adapter.api.rest.actionresult;

import io.github.yaforster.trails.adapter.api.rest.model.ActionResultChainDTO;
import io.github.yaforster.trails.app.services.ActionResultQueryService;
import io.github.yaforster.trails.app.services.HierarchyValidationService;
import lombok.AllArgsConstructor;
import org.openapitools.api.ActionResultApi;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

@AllArgsConstructor
@RestController
public class ActionResultController implements ActionResultApi {

	private final ActionResultQueryService databaseService;

	private final HierarchyValidationService hierarchyValidationService;

	private final ActionResultHATEOASFacade hateoasFacade;

	private final ActionResultControllerValidator validator;

	@Override
	public ResponseEntity<ActionResultChainDTO> listActionResultsChainInTestPath(Long applicationId, Long stageId,
			Long testRunId, Long testSetResultId, Long pathResultId) {
		validator.validate(applicationId, stageId, testRunId, testSetResultId, pathResultId);
		if (!hierarchyValidationService.pathBelongsToHierarchy(new HierarchyValidationService.TestPathHierarchy(
				applicationId, stageId, testRunId, testSetResultId, pathResultId))) {
			return ResponseEntity.notFound().build();
		}
		return databaseService.listActionResultChain(pathResultId)
			.map(orderedResults -> hateoasFacade.toChainDTOInHierarchy(applicationId, stageId, testRunId,
					testSetResultId, pathResultId, orderedResults))
			.map(ResponseEntity::ok)
			.orElse(ResponseEntity.notFound().build());
	}

}
