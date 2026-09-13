package io.github.yaforster.trails.adapter.api.rest.testset.assembler;

import io.github.yaforster.trails.adapter.api.rest.print.PrintController;
import io.github.yaforster.trails.adapter.api.rest.testpath.TestPathController;
import io.github.yaforster.trails.adapter.api.rest.testrun.TestRunController;
import io.github.yaforster.trails.adapter.api.rest.testset.TestSetController;
import io.github.yaforster.trails.adapter.api.rest.testset.model.TestCaseResultModel;
import io.github.yaforster.trails.adapter.api.rest.testset.model.TestSetResultModel;
import io.github.yaforster.trails.core.persisted.PersistedTestSetResult;
import io.github.yaforster.trails.core.test.Browser;
import org.jspecify.annotations.NonNull;
import org.springframework.hateoas.server.RepresentationModelAssembler;
import org.springframework.stereotype.Component;

import java.util.Optional;

import static org.springframework.hateoas.server.core.DummyInvocationUtils.methodOn;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;

@Component
public class TestSetResultModelAssembler
		implements RepresentationModelAssembler<PersistedTestSetResultContext, TestSetResultModel> {

	private static @NonNull TestSetResultModel buildModel(PersistedTestSetResult entity) {
		Browser browserToRunIn = entity.browserToRunIn();
		Optional<TestCaseResultModel> testCaseResultModel = PersistedTestSetResult.NO_TEST_CASE_TIMESTAMP
			.equals(entity.testCaseTimestamp()) ? Optional.empty()
					: Optional.of(new TestCaseResultModel(entity.testCaseTimestamp(), entity.testedInBrowser()));

		return new TestSetResultModel(entity.id(), entity.totalRunTime(), browserToRunIn, entity.testPlanLabel(),
				testCaseResultModel);
	}

	@Override
	public @NonNull TestSetResultModel toModel(@NonNull PersistedTestSetResultContext context) {
		PersistedTestSetResult entity = context.entity();

		TestSetResultModel model = buildModel(entity);

		model.add(linkTo(methodOn(TestSetController.class).getBrowserResultInTestRun(context.testRunId(),
				context.applicationId(), context.stageId(), entity.id()))
			.withSelfRel());

		model.add(linkTo(methodOn(TestSetController.class).listBrowserResults(context.testRunId(),
				context.applicationId(), context.stageId(), 0, 20))
			.withRel("collection"));

		model.add(linkTo(methodOn(TestRunController.class).getTestRun(context.testRunId(), context.applicationId(),
				context.stageId()))
			.withRel("testRun"));

		model.add(linkTo(methodOn(TestPathController.class).listPathResultsInTestSet(context.applicationId(),
				context.stageId(), context.testRunId(), entity.id(), 0, 20))
			.withRel("paths"));

		model.add(linkTo(methodOn(PrintController.class).printTestSetResultInTestRun(context.applicationId(),
				context.stageId(), context.testRunId(), entity.id()))
			.withRel("print"));

		return model;
	}

}
