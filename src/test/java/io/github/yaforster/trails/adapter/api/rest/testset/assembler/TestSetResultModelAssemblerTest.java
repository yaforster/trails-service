package io.github.yaforster.trails.adapter.api.rest.testset.assembler;

import io.github.yaforster.trails.adapter.api.rest.testset.model.TestSetResultModel;
import io.github.yaforster.trails.core.persisted.PersistedTestSetResult;
import io.github.yaforster.trails.core.test.Browser;
import org.junit.jupiter.api.Test;
import org.springframework.hateoas.Link;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TestSetResultModelAssemblerTest {

	private final TestSetResultModelAssembler assembler = new TestSetResultModelAssembler();

	private static void assertLinkContainsId(Link link, Long... ids) {
		String href = link.getHref();
		for (Long id : ids) {
			assertTrue(href.contains(String.valueOf(id)), () -> "Expected link '" + href + "' to contain id " + id);
		}
	}

	@Test
	void toModel_shouldMapFieldsAndLinks_whenTestCaseExists() {
		PersistedTestSetResult entity = new PersistedTestSetResult(11L, 22L, 33L, 44L, 120, Browser.CHROME, "plan-A",
				OffsetDateTime.of(2024, 5, 10, 12, 30, 0, 0, ZoneOffset.UTC), Browser.FIREFOX);
		PersistedTestSetResultContext context = new PersistedTestSetResultContext(33L, 44L, 22L, entity);

		TestSetResultModel model = assembler.toModel(context);

		assertEquals(11L, model.getId());
		assertEquals(120, model.getTotalRunTime());
		assertEquals(Browser.CHROME, model.getBrowserToRunIn());
		assertEquals("plan-A", model.getTestPlanLabel());
		assertTrue(model.getTestCaseResult().isPresent());
		assertEquals(entity.testCaseTimestamp(), model.getTestCaseResult().get().timestamp());
		assertEquals(Browser.FIREFOX, model.getTestCaseResult().get().testedInBrowser());

		assertEquals(5, model.getLinks().toList().size());
		assertLinkContainsId(model.getRequiredLink("self"), 11L, 22L, 33L, 44L);
		assertLinkContainsId(model.getRequiredLink("collection"), 22L, 33L, 44L);
		assertLinkContainsId(model.getRequiredLink("testRun"), 22L, 33L, 44L);
		assertLinkContainsId(model.getRequiredLink("paths"), 22L, 33L, 44L, 11L);
		assertLinkContainsId(model.getRequiredLink("print"), 22L, 33L, 44L, 11L);
	}

	@Test
	void toModel_shouldNotCreateTestCaseResult_whenTimestampMarksMissingTestCase() {
		PersistedTestSetResult entity = new PersistedTestSetResult(101L, 202L, 303L, 404L, 35, Browser.EDGE, "plan-B",
				PersistedTestSetResult.NO_TEST_CASE_TIMESTAMP, Browser.CHROME);
		PersistedTestSetResultContext context = new PersistedTestSetResultContext(303L, 404L, 202L, entity);

		TestSetResultModel model = assembler.toModel(context);

		assertTrue(model.getTestCaseResult().isEmpty());
		assertEquals(5, model.getLinks().toList().size());
		assertLinkContainsId(model.getRequiredLink("self"), 101L, 202L, 303L, 404L);
		assertLinkContainsId(model.getRequiredLink("collection"), 202L, 303L, 404L);
		assertLinkContainsId(model.getRequiredLink("testRun"), 202L, 303L, 404L);
		assertLinkContainsId(model.getRequiredLink("paths"), 202L, 303L, 404L, 101L);
		assertLinkContainsId(model.getRequiredLink("print"), 202L, 303L, 404L, 101L);
	}

}
