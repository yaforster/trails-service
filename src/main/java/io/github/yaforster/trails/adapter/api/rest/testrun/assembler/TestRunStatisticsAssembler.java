package io.github.yaforster.trails.adapter.api.rest.testrun.assembler;

import io.github.yaforster.trails.adapter.api.rest.testplan.TestPlanController;
import io.github.yaforster.trails.adapter.api.rest.testrun.TestRunController;
import io.github.yaforster.trails.adapter.api.rest.testrun.model.TestRunStatisticsModel;
import io.github.yaforster.trails.core.persisted.PersistedTestRunStatistics;
import org.jspecify.annotations.NonNull;
import org.springframework.hateoas.server.RepresentationModelAssembler;
import org.springframework.stereotype.Component;

import static org.springframework.hateoas.server.core.DummyInvocationUtils.methodOn;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;

@Component
public class TestRunStatisticsAssembler
		implements RepresentationModelAssembler<TestRunStatisticsContext, TestRunStatisticsModel> {

	@Override
	public @NonNull TestRunStatisticsModel toModel(@NonNull TestRunStatisticsContext context) {
		TestRunStatisticsModel model = new TestRunStatisticsModel(context.applicationId(), context.stageId(),
				context.testPlanId(), context.totalRuns(), context.successfulRuns(), context.partialSuccessRuns(),
				context.failedRuns());

		model.add(linkTo(methodOn(TestRunController.class).getTestRunStatistics(context.applicationId(),
				context.stageId(), context.testPlanId()))
			.withSelfRel());
		model.add(linkTo(methodOn(TestRunController.class).getTestRunHistory(context.applicationId(), context.stageId(),
				context.testPlanId(), 0, 50))
			.withRel("history"));
		model.add(linkTo(methodOn(TestPlanController.class).getTestPlan(context.applicationId(), context.stageId(),
				context.testPlanId(), false))
			.withRel("testPlan"));
		model.add(linkTo(methodOn(TestPlanController.class).listTestPlans(context.applicationId(), context.stageId(), 0,
				20, false))
			.withRel("collection"));
		return model;
	}

	public TestRunStatisticsContext toContext(PersistedTestRunStatistics statistics) {
		return new TestRunStatisticsContext(statistics.applicationId(), statistics.stageId(), statistics.testPlanId(),
				statistics.totalRuns(), statistics.successfulRuns(), statistics.partialSuccessRuns(),
				statistics.failedRuns());
	}

}
