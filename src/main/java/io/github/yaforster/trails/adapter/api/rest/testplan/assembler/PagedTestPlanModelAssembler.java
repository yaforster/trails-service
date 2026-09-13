package io.github.yaforster.trails.adapter.api.rest.testplan.assembler;

import io.github.yaforster.trails.adapter.api.ResourceAuthorization;
import io.github.yaforster.trails.adapter.api.rest.testplan.TestPlanController;
import io.github.yaforster.trails.adapter.api.rest.testplan.mapper.TestPlanModel;
import lombok.AllArgsConstructor;
import org.jspecify.annotations.NonNull;
import org.springframework.data.domain.Page;
import org.springframework.data.web.PagedResourcesAssembler;
import org.springframework.hateoas.PagedModel;
import org.springframework.hateoas.server.RepresentationModelAssembler;
import org.springframework.stereotype.Component;

import static io.github.yaforster.trails.adapter.api.rest.hateoas.HATEOASLinks.put;
import static org.springframework.hateoas.server.core.DummyInvocationUtils.methodOn;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;

@Component
@AllArgsConstructor
public class PagedTestPlanModelAssembler
		implements RepresentationModelAssembler<PagedTestPlanContext, PagedModel<TestPlanModel>> {

	private final PagedResourcesAssembler<PersistedTestPlanContext> pagedAssembler;

	private final TestPlanListItemModelAssembler listItemAssembler;

	private final ResourceAuthorization resourceAuthorization;

	@Override
	public @NonNull PagedModel<TestPlanModel> toModel(@NonNull PagedTestPlanContext pagedContext) {
		Long applicationId = pagedContext.applicationId();
		Long stageId = pagedContext.stageId();
		Page<PersistedTestPlanContext> contexts = pagedContext.contexts();
		PagedModel<TestPlanModel> model = pagedAssembler.toModel(contexts, listItemAssembler);
		addPagingLinks(model, applicationId, stageId, contexts.getNumber(), contexts.getSize(),
				contexts.getTotalPages());
		return model;
	}

	private void addPagingLinks(PagedModel<?> model, Long applicationId, Long stageId, int page, int size,
			int totalPages) {
		int lastPage = Math.max(totalPages - 1, 0);

		model.add(linkTo(methodOn(TestPlanController.class).listTestPlans(applicationId, stageId, page, size, false))
			.withSelfRel());

		if (resourceAuthorization.canManageResources()) {
			model.add(put(linkTo(methodOn(TestPlanController.class).createNewTestPlan(applicationId, stageId, null))
				.withRel("create")));
		}

		model.add(linkTo(methodOn(TestPlanController.class).listTestPlans(applicationId, stageId, 0, size, false))
			.withRel("first"));

		model
			.add(linkTo(methodOn(TestPlanController.class).listTestPlans(applicationId, stageId, lastPage, size, false))
				.withRel("last"));

		if (page > 0) {
			model.add(linkTo(
					methodOn(TestPlanController.class).listTestPlans(applicationId, stageId, page - 1, size, false))
				.withRel("prev"));
		}

		if (page < lastPage) {
			model.add(linkTo(
					methodOn(TestPlanController.class).listTestPlans(applicationId, stageId, page + 1, size, false))
				.withRel("next"));
		}
	}

}
