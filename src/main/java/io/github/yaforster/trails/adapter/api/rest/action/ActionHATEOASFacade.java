package io.github.yaforster.trails.adapter.api.rest.action;

import io.github.yaforster.trails.adapter.api.rest.action.assembler.ActionModelAssembler;
import io.github.yaforster.trails.adapter.api.rest.action.assembler.PersistedActionContext;
import io.github.yaforster.trails.adapter.api.rest.action.mapper.PagedActionMapper;
import io.github.yaforster.trails.adapter.api.rest.common.PagedResultPageAdapter;
import io.github.yaforster.trails.adapter.api.rest.action.model.ActionModel;
import io.github.yaforster.trails.adapter.api.rest.common.mapper.PagedContentMapper;
import io.github.yaforster.trails.adapter.api.rest.model.PagedActionDTO;
import io.github.yaforster.trails.adapter.api.rest.testplan.TestPlanController;
import io.github.yaforster.trails.core.persisted.PersistedAction;
import io.github.yaforster.trails.core.PagedResult;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.web.PagedResourcesAssembler;
import org.springframework.hateoas.PagedModel;
import org.springframework.stereotype.Component;

import static org.springframework.hateoas.server.core.DummyInvocationUtils.methodOn;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;

@Component
@AllArgsConstructor
public class ActionHATEOASFacade extends PagedContentMapper {

	private final ActionModelAssembler assembler;

	private final PagedResourcesAssembler<PersistedActionContext> pagedAssembler;

	private final PagedActionMapper pagedActionMapper;

	public PagedActionDTO toPagedDTO(Long applicationId, Long stageId, Long testPlanId,
			PagedResult<PersistedAction> pagedActions) {
		Page<PersistedActionContext> contexts = PagedResultPageAdapter.toSpringPage(
				pagedActions.map(entity -> new PersistedActionContext(applicationId, stageId, testPlanId, entity)));

		PagedModel<ActionModel> model = pagedAssembler.toModel(contexts, assembler);
		addPagingLinks(model, applicationId, stageId, testPlanId, pagedActions.page(), pagedActions.size(),
				pagedActions.totalPages());

		return pagedActionMapper.toPagedDto(model);
	}

	private void addPagingLinks(PagedModel<?> model, Long applicationId, Long stageId, Long testPlanId, int page,
			int size, int totalPages) {
		int lastPage = Math.max(totalPages - 1, 0);

		model.add(linkTo(methodOn(ActionController.class).getActions(applicationId, stageId, testPlanId, page, size))
			.withSelfRel());

		model.add(linkTo(methodOn(TestPlanController.class).getTestPlan(applicationId, stageId, testPlanId, false))
			.withRel("testPlan"));

		model.add(linkTo(methodOn(ActionController.class).getActions(applicationId, stageId, testPlanId, 0, size))
			.withRel("first"));

		model
			.add(linkTo(methodOn(ActionController.class).getActions(applicationId, stageId, testPlanId, lastPage, size))
				.withRel("last"));

		if (page > 0) {
			model.add(linkTo(
					methodOn(ActionController.class).getActions(applicationId, stageId, testPlanId, page - 1, size))
				.withRel("prev"));
		}

		if (page < lastPage) {
			model.add(linkTo(
					methodOn(ActionController.class).getActions(applicationId, stageId, testPlanId, page + 1, size))
				.withRel("next"));
		}
	}

}
