package io.github.yaforster.trails.adapter.api.rest.testplan.assembler;

import io.github.yaforster.trails.adapter.api.ResourceAuthorization;
import io.github.yaforster.trails.adapter.api.rest.testplan.TestPlanLinks;
import io.github.yaforster.trails.adapter.api.rest.testplan.mapper.TestPlanModel;
import io.github.yaforster.trails.core.persisted.PersistedTestPlan;
import lombok.AllArgsConstructor;
import org.jspecify.annotations.NonNull;
import org.springframework.hateoas.server.RepresentationModelAssembler;
import org.springframework.stereotype.Component;

@Component
@AllArgsConstructor
public class TestPlanListItemModelAssembler
		implements RepresentationModelAssembler<PersistedTestPlanContext, TestPlanModel> {

	private final ResourceAuthorization resourceAuthorization;

	private final TestPlanLinks testPlanLinks;

	@Override
	public @NonNull TestPlanModel toModel(@NonNull PersistedTestPlanContext context) {
		PersistedTestPlan persistedTestPlan = context.persistedTestPlan();
		TestPlanModel model = new TestPlanModel(persistedTestPlan.id(), persistedTestPlan.label(),
				persistedTestPlan.retired());

		model.add(testPlanLinks.collection(context.applicationId(), context.stageId()));

		if (context.hasTestSteps()) {
			model.add(testPlanLinks.steps(context.applicationId(), context.stageId(), persistedTestPlan.id()));
		}

		model.add(testPlanLinks.self(context.applicationId(), context.stageId(), persistedTestPlan.id()));

		if (resourceAuthorization.canManageResources() && persistedTestPlan.retired()) {
			model.add(testPlanLinks.restore(context.applicationId(), context.stageId(), persistedTestPlan.id()));
		}
		else if (resourceAuthorization.canManageResources()) {
			model.add(testPlanLinks.deleteTestPlan(context.applicationId(), context.stageId(), persistedTestPlan.id()));
		}

		return model;
	}

}
