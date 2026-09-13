package io.github.yaforster.trails.adapter.api.rest.testrun.assembler;

import io.github.yaforster.trails.adapter.api.rest.testrun.model.TestRunStatisticsModel;
import io.github.yaforster.trails.core.persisted.PersistedTestRunStatistics;
import org.junit.jupiter.api.Test;
import org.springframework.hateoas.Link;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TestRunStatisticsAssemblerTest {

	private final TestRunStatisticsAssembler assembler = new TestRunStatisticsAssembler();

	private static void assertLinkContainsId(Link link, Long... ids) {
		String href = link.getHref();
		for (Long id : ids) {
			assertTrue(href.contains(String.valueOf(id)), () -> "Expected link '" + href + "' to contain id " + id);
		}
	}

	@Test
	void toModel_shouldMapFieldsAndLinks() {
		TestRunStatisticsContext context = new TestRunStatisticsContext(11L, 22L, 33L, 100L, 70L, 20L, 10L);

		TestRunStatisticsModel model = assembler.toModel(context);

		assertEquals(11L, model.getApplicationId());
		assertEquals(22L, model.getStageId());
		assertEquals(33L, model.getTestPlanId());
		assertEquals(100L, model.getTotalRuns());
		assertEquals(70L, model.getSuccessfulRuns());
		assertEquals(20L, model.getPartialSuccessRuns());
		assertEquals(10L, model.getFailedRuns());
		assertEquals(4, model.getLinks().toList().size());
		assertLinkContainsId(model.getRequiredLink("self"), 11L, 22L, 33L);
		assertLinkContainsId(model.getRequiredLink("history"), 11L, 22L, 33L);
		assertLinkContainsId(model.getRequiredLink("testPlan"), 11L, 22L, 33L);
		assertLinkContainsId(model.getRequiredLink("collection"), 11L, 22L);
	}

	@Test
	void toContext_shouldMapFromPersistedStatistics() {
		PersistedTestRunStatistics statistics = new PersistedTestRunStatistics(1L, 2L, 3L, 10L, 4L, 3L, 3L);

		TestRunStatisticsContext context = assembler.toContext(statistics);

		assertEquals(1L, context.applicationId());
		assertEquals(2L, context.stageId());
		assertEquals(3L, context.testPlanId());
		assertEquals(10L, context.totalRuns());
		assertEquals(4L, context.successfulRuns());
		assertEquals(3L, context.partialSuccessRuns());
		assertEquals(3L, context.failedRuns());
	}

}
