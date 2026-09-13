package io.github.yaforster.trails.adapter.api.rest.action;

import io.github.yaforster.trails.adapter.api.rest.model.PagedActionDTO;
import io.github.yaforster.trails.app.services.ActionQueryService;
import lombok.AllArgsConstructor;
import org.openapitools.api.ActionApi;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

@AllArgsConstructor
@RestController
public class ActionController implements ActionApi {

	private final ActionQueryService actionQueryService;

	private final ActionHATEOASFacade hateoasFacade;

	private final ActionControllerValidator validator;

	@Override
	public ResponseEntity<PagedActionDTO> getActions(Long applicationId, Long stageId, Long testPlanId, Integer page,
			Integer size) {
		validator.validate(applicationId, stageId, testPlanId, page, size);
		return actionQueryService
			.listActions(new ActionQueryService.ActionPage(applicationId, stageId, testPlanId, page, size))
			.map(pagedActions -> hateoasFacade.toPagedDTO(applicationId, stageId, testPlanId, pagedActions))
			.map(ResponseEntity::ok)
			.orElse(ResponseEntity.notFound().build());
	}

}
