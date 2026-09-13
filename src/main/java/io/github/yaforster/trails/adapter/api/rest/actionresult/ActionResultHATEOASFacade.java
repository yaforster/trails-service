package io.github.yaforster.trails.adapter.api.rest.actionresult;

import io.github.yaforster.trails.adapter.api.rest.actionresult.assembler.ActionResultModelAssembler;
import io.github.yaforster.trails.adapter.api.rest.actionresult.assembler.PersistedActionResultContext;
import io.github.yaforster.trails.adapter.api.rest.actionresult.mapper.PagedActionResultMapper;
import io.github.yaforster.trails.adapter.api.rest.actionresult.model.ActionResultModel;
import io.github.yaforster.trails.adapter.api.rest.hateoas.HATEOASMapper;
import io.github.yaforster.trails.adapter.api.rest.model.ActionResultChainDTO;
import io.github.yaforster.trails.adapter.api.rest.model.ActionResultDTO;
import io.github.yaforster.trails.adapter.api.rest.testpath.TestPathController;
import io.github.yaforster.trails.app.services.ActionResultScreenshotQueryService;
import io.github.yaforster.trails.core.persisted.PersistedActionResult;
import lombok.AllArgsConstructor;
import org.springframework.hateoas.Links;
import org.springframework.stereotype.Component;

import java.util.List;

import static org.springframework.hateoas.server.core.DummyInvocationUtils.methodOn;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;

@Component
@AllArgsConstructor
public class ActionResultHATEOASFacade implements HATEOASMapper {

	private final ActionResultModelAssembler assembler;

	private final PagedActionResultMapper pagedActionResultMapper;

	private final ActionResultScreenshotQueryService actionResultScreenshotQueryService;

	private ActionResultDTO toDTO(Long applicationId, Long stageId, Long testRunId, Long testSetResultId,
			Long pathResultId, PersistedActionResult entity) {
		boolean hasScreenshot = actionResultScreenshotQueryService
			.findInPath(new ActionResultScreenshotQueryService.PathActionScreenshot(pathResultId, entity.id()))
			.isPresent();

		PersistedActionResultContext context = PersistedActionResultContext.of(applicationId, stageId, testRunId,
				testSetResultId, pathResultId, entity, hasScreenshot);
		ActionResultModel model = assembler.toModel(context);
		return pagedActionResultMapper.toActionResultDto(model);
	}

	public ActionResultChainDTO toChainDTOInHierarchy(Long applicationId, Long stageId, Long testRunId,
			Long testSetResultId, Long pathResultId, List<PersistedActionResult> orderedResults) {
		List<ActionResultDTO> items = orderedResults.stream()
			.map(entity -> toDTO(applicationId, stageId, testRunId, testSetResultId, pathResultId, entity))
			.toList();
		ActionResultChainDTO dto = new ActionResultChainDTO();
		dto.setItems(items);
		dto.setLinks(HATEOASMapper.super.mapLinks(Links.of(
				linkTo(methodOn(ActionResultController.class).listActionResultsChainInTestPath(applicationId, stageId,
						testRunId, testSetResultId, pathResultId))
					.withSelfRel(),
				linkTo(methodOn(TestPathController.class).getPathResultInTestSet(applicationId, stageId, testRunId,
						testSetResultId, pathResultId))
					.withRel("pathResult"))));
		return dto;
	}

}
