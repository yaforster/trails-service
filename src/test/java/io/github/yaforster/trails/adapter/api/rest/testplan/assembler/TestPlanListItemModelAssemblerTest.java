package io.github.yaforster.trails.adapter.api.rest.testplan.assembler;

import io.github.yaforster.trails.adapter.api.ResourceAuthorization;
import io.github.yaforster.trails.adapter.api.rest.testplan.TestPlanLinks;
import io.github.yaforster.trails.adapter.api.rest.testplan.mapper.TestPlanModel;
import io.github.yaforster.trails.core.TrailsTest;
import io.github.yaforster.trails.core.persisted.PersistedTestPlan;
import org.junit.jupiter.api.Test;
import org.springframework.hateoas.Link;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class TestPlanListItemModelAssemblerTest extends TrailsTest {

	private final ResourceAuthorization resourceAuthorization = mock(ResourceAuthorization.class);

	private final TestPlanListItemModelAssembler assembler = new TestPlanListItemModelAssembler(resourceAuthorization,
			new TestPlanLinks());

	@Test
	void givenValidContextWithoutSteps_whenToModel_thenModelWithCorrectLinksAndNoSteps() {
		when(resourceAuthorization.canManageResources()).thenReturn(true);
		PersistedTestPlan persistedTestPlan = new PersistedTestPlan(10L, 20L, 30L, "Test plan", false);

		PersistedTestPlanContext context = new PersistedTestPlanContext(persistedTestPlan.applicationID(),
				persistedTestPlan.stageID(), persistedTestPlan, false);

		TestPlanModel model = assembler.toModel(context);

		assertThat(model.getId()).isEqualTo(persistedTestPlan.id());
		assertThat(model.getLabel()).isEqualTo(persistedTestPlan.label());

		Link collectionLink = model.getLinks().getLink("collection").orElse(null);
		Link selfLink = model.getLinks().getLink("self").orElse(null);
		Link stepsLink = model.getLinks().getLink("steps").orElse(null);

		assertThat(collectionLink).isNotNull();
		assertThat(selfLink).isNotNull();
		assertThat(stepsLink).isNull();
		assertThat(model.getLinks().getLink("delete")).isPresent();

		assertThat(collectionLink.getHref())
			.contains("/applications/" + context.applicationId() + "/stages/" + context.stageId() + "/testPlans");
		assertThat(selfLink.getHref()).contains("/applications/" + context.applicationId() + "/");
		assertThat(selfLink.getHref()).contains("/" + context.stageId() + "/");
		assertThat(selfLink.getHref()).contains("/" + persistedTestPlan.id());
		assertThat(selfLink.getHref()).contains("includeRetired=false");
	}

	@Test
	void givenValidContextWithSteps_whenToModel_thenModelWithCorrectLinksAndSteps() {
		when(resourceAuthorization.canManageResources()).thenReturn(true);
		PersistedTestPlan persistedTestPlan = new PersistedTestPlan(10L, 20L, 30L, "Test plan", false);

		PersistedTestPlanContext context = new PersistedTestPlanContext(persistedTestPlan.applicationID(),
				persistedTestPlan.stageID(), persistedTestPlan, true);

		TestPlanModel model = assembler.toModel(context);

		assertThat(model.getId()).isEqualTo(persistedTestPlan.id());
		assertThat(model.getLabel()).isEqualTo(persistedTestPlan.label());

		Link collectionLink = model.getLinks().getLink("collection").orElse(null);
		Link selfLink = model.getLinks().getLink("self").orElse(null);
		Link stepsLink = model.getLinks().getLink("steps").orElse(null);

		assertThat(collectionLink).isNotNull();
		assertThat(selfLink).isNotNull();
		assertThat(stepsLink).isNotNull();
		assertThat(model.getLinks().getLink("delete")).isPresent();

		assertThat(collectionLink.getHref())
			.contains("/applications/" + context.applicationId() + "/stages/" + context.stageId() + "/testPlans");
		assertThat(selfLink.getHref()).contains("/applications/" + context.applicationId() + "/");
		assertThat(selfLink.getHref()).contains("/" + context.stageId() + "/");
		assertThat(selfLink.getHref()).contains("/" + persistedTestPlan.id());
		assertThat(selfLink.getHref()).contains("includeRetired=false");
		assertThat(stepsLink.getHref()).contains("/actions");
	}

	@Test
	void givenContextWithNullPersistedTestPlan_whenToModel_thenThrowsNullPointerException() {
		PersistedTestPlanContext context = new PersistedTestPlanContext(100L, 200L, null, false);

		NullPointerException exception = assertThrows(NullPointerException.class, () -> assembler.toModel(context));
		assertThat(exception.getMessage()).contains("persistedTestPlan");
	}

}
