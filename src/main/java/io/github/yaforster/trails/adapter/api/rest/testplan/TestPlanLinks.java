package io.github.yaforster.trails.adapter.api.rest.testplan;

import io.github.yaforster.trails.adapter.api.rest.action.ActionController;
import io.github.yaforster.trails.adapter.api.rest.run.TestExecutionController;
import io.github.yaforster.trails.adapter.api.rest.testrun.TestRunController;
import org.springframework.hateoas.Link;
import org.springframework.stereotype.Component;

import static io.github.yaforster.trails.adapter.api.rest.hateoas.HATEOASLinks.delete;
import static io.github.yaforster.trails.adapter.api.rest.hateoas.HATEOASLinks.post;
import static org.springframework.hateoas.server.core.DummyInvocationUtils.methodOn;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;

@Component
public class TestPlanLinks {

	public Link collection(Long applicationId, Long stageId) {
		return linkTo(methodOn(TestPlanController.class).listTestPlans(applicationId, stageId, 0, 20, false))
			.withRel("collection");
	}

	public Link self(Long applicationId, Long stageId, Long testPlanId) {
		return linkTo(methodOn(TestPlanController.class).getTestPlan(applicationId, stageId, testPlanId, false))
			.withSelfRel();
	}

	public Link steps(Long applicationId, Long stageId, Long testPlanId) {
		return linkTo(methodOn(ActionController.class).getActions(applicationId, stageId, testPlanId, 0, 20))
			.withRel("steps");
	}

	public Link statistics(Long applicationId, Long stageId, Long testPlanId) {
		return linkTo(methodOn(TestRunController.class).getTestRunStatistics(applicationId, stageId, testPlanId))
			.withRel("statistics");
	}

	public Link history(Long applicationId, Long stageId, Long testPlanId) {
		return linkTo(methodOn(TestRunController.class).getTestRunHistory(applicationId, stageId, testPlanId, 0, 50))
			.withRel("history");
	}

	public Link execute() {
		return post(linkTo(methodOn(TestExecutionController.class).runTest(null)).withRel("execute"));
	}

	public Link restore(Long applicationId, Long stageId, Long testPlanId) {
		return post(linkTo(methodOn(TestPlanController.class).restoreTestPlan(applicationId, stageId, testPlanId))
			.withRel("restore"));
	}

	public Link deleteTestPlan(Long applicationId, Long stageId, Long testPlanId) {
		return delete(linkTo(methodOn(TestPlanController.class).deleteTestPlan(applicationId, stageId, testPlanId))
			.withRel("delete"));
	}

	public Link promote(Long applicationId, Long stageId, Long testPlanId) {
		String href = linkTo(methodOn(TestPlanController.class).getTestPlan(applicationId, stageId, testPlanId, false))
			.toUriComponentsBuilder()
			.replaceQuery(null)
			.path("/promote")
			.build()
			.toUriString() + "{?targetStageId}";
		return post(Link.of(href).withRel("promote"));
	}

}
