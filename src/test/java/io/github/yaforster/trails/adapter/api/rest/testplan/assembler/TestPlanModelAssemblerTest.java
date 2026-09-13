package io.github.yaforster.trails.adapter.api.rest.testplan.assembler;

import io.github.yaforster.trails.adapter.api.ResourceAuthorization;
import io.github.yaforster.trails.adapter.api.rest.testplan.TestPlanLinks;
import io.github.yaforster.trails.adapter.api.rest.testplan.mapper.TestPlanModel;
import io.github.yaforster.trails.adapter.api.rest.testplan.model.PersistedTestPlanModel;
import io.github.yaforster.trails.core.data.TestPlan;
import io.github.yaforster.trails.core.persisted.PersistedTestPlan;
import io.github.yaforster.trails.core.test.Action;
import org.junit.jupiter.api.Test;
import org.springframework.hateoas.Link;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class TestPlanModelAssemblerTest {

	private final TestPlanLinks testPlanLinks = new TestPlanLinks();

	private static void assertLinkContainsIds(Link link, Long... ids) {
		for (Long id : ids) {
			assertThat(link.getHref()).contains(String.valueOf(id));
		}
	}

	@Test
	void toModel_withPersistedContextAndSteps_shouldMapAndAddStepsLink() {
		ResourceAuthorization resourceAuthorization = mock(ResourceAuthorization.class);
		when(resourceAuthorization.canManageResources()).thenReturn(true);
		when(resourceAuthorization.canExecuteTests()).thenReturn(true);
		TestPlanModelAssembler assembler = new TestPlanModelAssembler(resourceAuthorization, testPlanLinks);
		PersistedTestPlan persisted = new PersistedTestPlan(31L, 11L, 21L, "Smoke");
		PersistedTestPlanContext context = new PersistedTestPlanContext(11L, 21L, persisted, true);

		PersistedTestPlanModel model = assembler.toModel(context);

		assertThat(model.getId()).isEqualTo(31L);
		assertThat(model.getApplicationID()).isEqualTo(11L);
		assertThat(model.getStageID()).isEqualTo(21L);
		assertThat(model.getLabel()).isEqualTo("Smoke");

		Link collection = model.getRequiredLink("collection");
		Link steps = model.getRequiredLink("steps");
		Link self = model.getRequiredLink("self");
		Link statistics = model.getRequiredLink("statistics");
		Link history = model.getRequiredLink("history");
		Link execute = model.getRequiredLink("execute");
		Link delete = model.getRequiredLink("delete");
		Link promote = model.getRequiredLink("promote");
		assertThat(collection.getHref()).contains("/applications/11/stages/21/testPlans");
		assertThat(steps.getHref()).contains("/actions");
		assertThat(statistics.getHref()).contains("/statistics");
		assertThat(history.getHref()).contains("/metrics/history");
		assertThat(execute.getHref()).contains("/api/test");
		assertLinkContainsIds(delete, 11L, 21L, 31L);
		assertThat(promote.getHref()).endsWith("/promote{?targetStageId}");
		assertLinkContainsIds(steps, 11L, 21L, 31L);
		assertLinkContainsIds(statistics, 11L, 21L, 31L);
		assertLinkContainsIds(self, 11L, 21L, 31L);
	}

	@Test
	void toModel_withPersistedContextWithoutSteps_shouldNotAddStepsLink() {
		ResourceAuthorization resourceAuthorization = mock(ResourceAuthorization.class);
		when(resourceAuthorization.canManageResources()).thenReturn(true);
		when(resourceAuthorization.canExecuteTests()).thenReturn(true);
		TestPlanModelAssembler assembler = new TestPlanModelAssembler(resourceAuthorization, testPlanLinks);
		PersistedTestPlan persisted = new PersistedTestPlan(41L, 12L, 22L, "Regression");
		PersistedTestPlanContext context = new PersistedTestPlanContext(12L, 22L, persisted, false);

		PersistedTestPlanModel model = assembler.toModel(context);

		assertThat(model.getLink("steps")).isEmpty();
		assertThat(model.getRequiredLink("statistics").getHref()).contains("/statistics");
		assertThat(model.getRequiredLink("collection").getHref()).contains("/applications/12/stages/22/testPlans");
		assertThat(model.getRequiredLink("execute").getHref()).contains("/api/test");
		assertLinkContainsIds(model.getRequiredLink("self"), 12L, 22L, 41L);
	}

	@Test
	void toModel_withPersistedContext_shouldNotAddPromoteLink_whenUserCannotPromoteResources() {
		ResourceAuthorization resourceAuthorization = mock(ResourceAuthorization.class);
		when(resourceAuthorization.canManageResources()).thenReturn(false);
		TestPlanModelAssembler assembler = new TestPlanModelAssembler(resourceAuthorization, testPlanLinks);
		PersistedTestPlan persisted = new PersistedTestPlan(41L, 12L, 22L, "Regression");
		PersistedTestPlanContext context = new PersistedTestPlanContext(12L, 22L, persisted, false);

		PersistedTestPlanModel model = assembler.toModel(context);

		assertThat(model.getLink("promote")).isEmpty();
		assertThat(model.getLink("delete")).isEmpty();
		assertThat(model.getLink("restore")).isEmpty();
	}

	@Test
	void toModel_withPersistedContext_shouldNotAddExecuteLink_whenUserCannotExecuteTests() {
		ResourceAuthorization resourceAuthorization = mock(ResourceAuthorization.class);
		when(resourceAuthorization.canManageResources()).thenReturn(true);
		when(resourceAuthorization.canExecuteTests()).thenReturn(false);
		TestPlanModelAssembler assembler = new TestPlanModelAssembler(resourceAuthorization, testPlanLinks);
		PersistedTestPlan persisted = new PersistedTestPlan(41L, 12L, 22L, "Regression");
		PersistedTestPlanContext context = new PersistedTestPlanContext(12L, 22L, persisted, false);

		PersistedTestPlanModel model = assembler.toModel(context);

		assertThat(model.getLink("execute")).isEmpty();
	}

	@Test
	void toModel_withNullPersistedTestPlan_shouldThrow() {
		ResourceAuthorization resourceAuthorization = mock(ResourceAuthorization.class);
		when(resourceAuthorization.canExecuteTests()).thenReturn(true);
		TestPlanModelAssembler assembler = new TestPlanModelAssembler(resourceAuthorization, testPlanLinks);
		PersistedTestPlanContext context = new PersistedTestPlanContext(1L, 2L, null, false);

		assertThrows(NullPointerException.class, () -> assembler.toModel(context));
	}

	@Test
	void toModel_withTestPlanAndActions_shouldMapAndAddStepsLink() {
		ResourceAuthorization resourceAuthorization = mock(ResourceAuthorization.class);
		when(resourceAuthorization.canExecuteTests()).thenReturn(true);
		TestPlanModelAssembler assembler = new TestPlanModelAssembler(resourceAuthorization, testPlanLinks);
		TestPlan testPlan = new TestPlan(51L, 13L, 23L, "E2E", List.of(mock(Action.class)));

		TestPlanModel model = assembler.toModel(testPlan);

		assertThat(model.getId()).isEqualTo(51L);
		assertThat(model.getLabel()).isEqualTo("E2E");
		assertThat(model.getRequiredLink("collection").getHref()).contains("/applications/13/stages/23/testPlans");
		assertThat(model.getRequiredLink("steps").getHref()).contains("/actions");
		assertThat(model.getRequiredLink("execute").getHref()).contains("/api/test");
		assertLinkContainsIds(model.getRequiredLink("steps"), 13L, 23L, 51L);
		assertLinkContainsIds(model.getRequiredLink("self"), 13L, 23L, 51L);
	}

	@Test
	void toModel_withTestPlanWithoutActions_shouldNotAddStepsLink() {
		ResourceAuthorization resourceAuthorization = mock(ResourceAuthorization.class);
		when(resourceAuthorization.canExecuteTests()).thenReturn(true);
		TestPlanModelAssembler assembler = new TestPlanModelAssembler(resourceAuthorization, testPlanLinks);
		TestPlan noActions = new TestPlan(61L, 14L, 24L, "NoActions", List.of());
		TestPlan nullActions = new TestPlan(62L, 14L, 24L, "NullActions", null);

		TestPlanModel modelWithoutActions = assembler.toModel(noActions);
		TestPlanModel modelWithNullActions = assembler.toModel(nullActions);

		assertThat(modelWithoutActions.getLink("steps")).isEmpty();
		assertThat(modelWithNullActions.getLink("steps")).isEmpty();
		assertThat(modelWithoutActions.getRequiredLink("execute").getHref()).contains("/api/test");
		assertThat(modelWithNullActions.getRequiredLink("execute").getHref()).contains("/api/test");
		assertLinkContainsIds(modelWithoutActions.getRequiredLink("self"), 14L, 24L, 61L);
		assertLinkContainsIds(modelWithNullActions.getRequiredLink("self"), 14L, 24L, 62L);
	}

}
