package io.github.yaforster.trails.adapter.api.rest.testplan.assembler;

import io.github.yaforster.trails.adapter.api.ResourceAuthorization;
import io.github.yaforster.trails.adapter.api.rest.testplan.TestPlanLinks;
import io.github.yaforster.trails.adapter.api.rest.testplan.mapper.TestPlanModel;
import io.github.yaforster.trails.adapter.api.rest.testplan.model.PersistedTestPlanModel;
import io.github.yaforster.trails.core.data.TestPlan;
import io.github.yaforster.trails.core.persisted.PersistedTestPlan;
import lombok.AllArgsConstructor;
import org.jspecify.annotations.NonNull;
import org.springframework.hateoas.server.RepresentationModelAssembler;
import org.springframework.stereotype.Component;

@Component
@AllArgsConstructor
public class TestPlanModelAssembler
		implements RepresentationModelAssembler<PersistedTestPlanContext, PersistedTestPlanModel> {

	private final ResourceAuthorization resourceAuthorization;

	private final TestPlanLinks testPlanLinks;

	@Override
	public @NonNull PersistedTestPlanModel toModel(@NonNull PersistedTestPlanContext context) {
		PersistedTestPlan persistedTestPlan = context.persistedTestPlan();
		PersistedTestPlanModel model = new PersistedTestPlanModel(persistedTestPlan.id(),
				persistedTestPlan.applicationID(), persistedTestPlan.stageID(), persistedTestPlan.label(),
				persistedTestPlan.retired(), persistedTestPlan.groups());

		model.add(testPlanLinks.collection(context.applicationId(), context.stageId()));

		if (context.hasTestSteps()) {
			model.add(testPlanLinks.steps(persistedTestPlan.applicationID(), persistedTestPlan.stageID(),
					persistedTestPlan.id()));
		}

		model.add(testPlanLinks.self(context.applicationId(), context.stageId(), persistedTestPlan.id()));
		model.add(testPlanLinks.statistics(context.applicationId(), context.stageId(), persistedTestPlan.id()));
		model.add(testPlanLinks.history(context.applicationId(), context.stageId(), persistedTestPlan.id()));
		if (resourceAuthorization.canExecuteTests()) {
			model.add(testPlanLinks.execute());
		}

		if (resourceAuthorization.canManageResources() && persistedTestPlan.retired()) {
			model.add(testPlanLinks.restore(context.applicationId(), context.stageId(), persistedTestPlan.id()));
		}
		else if (resourceAuthorization.canManageResources()) {
			model.add(testPlanLinks.deleteTestPlan(context.applicationId(), context.stageId(), persistedTestPlan.id()));
			model.add(testPlanLinks.promote(context.applicationId(), context.stageId(), persistedTestPlan.id()));
		}

		return model;
	}

	public @NonNull TestPlanModel toModel(@NonNull TestPlan testPlan) {
		TestPlanModel model = new TestPlanModel(testPlan.getId(), testPlan.getLabel(), false, testPlan.getGroups());

		model.add(testPlanLinks.collection(testPlan.getApplicationID(), testPlan.getStageID()));

		if (testPlan.getActions() != null && !testPlan.getActions().isEmpty()) {
			model.add(testPlanLinks.steps(testPlan.getApplicationID(), testPlan.getStageID(), testPlan.getId()));
		}

		model.add(testPlanLinks.self(testPlan.getApplicationID(), testPlan.getStageID(), testPlan.getId()));
		if (resourceAuthorization.canExecuteTests()) {
			model.add(testPlanLinks.execute());
		}

		return model;
	}

}
