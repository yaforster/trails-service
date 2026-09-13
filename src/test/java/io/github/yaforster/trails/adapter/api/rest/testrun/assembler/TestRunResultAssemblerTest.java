package io.github.yaforster.trails.adapter.api.rest.testrun.assembler;

import io.github.yaforster.trails.adapter.api.rest.testrun.model.TestRunResultModel;
import io.github.yaforster.trails.core.data.ResultIndicator;
import io.github.yaforster.trails.core.persisted.PersistedTestRunResult;
import org.junit.jupiter.api.Test;
import org.springframework.hateoas.Link;

import java.sql.Timestamp;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;

import static org.junit.jupiter.api.Assertions.*;

class TestRunResultAssemblerTest {

	private final TestRunResultAssembler assembler = new TestRunResultAssembler();

	private static void assertLinkContainsId(Link link, Long... ids) {
		String href = link.getHref();
		for (Long id : ids) {
			assertTrue(href.contains(String.valueOf(id)), () -> "Expected link '" + href + "' to contain id " + id);
		}
	}

	@Test
	void toModel_shouldMapFieldsAndLinks() {
		TestRunResultContext context = new TestRunResultContext(10L, 20L, 30L,
				OffsetDateTime.of(2024, 1, 2, 3, 4, 5, 0, ZoneOffset.UTC), ResultIndicator.PARTIAL_SUCCESS,
				"nightly run");

		TestRunResultModel model = assembler.toModel(context);

		assertEquals(30L, model.getId());
		assertEquals(context.timestamp(), model.getTimestamp());
		assertEquals(ResultIndicator.PARTIAL_SUCCESS, model.getIndicator());
		assertEquals(10L, model.getApplicationId());
		assertEquals(20L, model.getStageId());
		assertEquals("nightly run", model.getLabel());

		assertEquals(4, model.getLinks().toList().size());
		assertLinkContainsId(model.getRequiredLink("self"), 30L, 10L, 20L);
		assertLinkContainsId(model.getRequiredLink("collection"), 10L, 20L);
		assertLinkContainsId(model.getRequiredLink("testSets"), 30L, 10L, 20L);
		assertLinkContainsId(model.getRequiredLink("print"), 10L, 20L, 30L);
	}

	@Test
	void toContext_shouldMapFromPersistedTestRunResult() {
		Timestamp timestamp = Timestamp.from(Instant.parse("2024-12-31T23:59:59Z"));
		PersistedTestRunResult persisted = new PersistedTestRunResult(7L, 8L, 9L, 10L, timestamp,
				ResultIndicator.FAILURE, "nightly run");

		TestRunResultContext context = assembler.toContext(persisted);

		assertEquals(8L, context.applicationId());
		assertEquals(9L, context.stageId());
		assertEquals(7L, context.resultId());
		assertEquals(timestamp.toInstant().atOffset(ZoneOffset.UTC), context.timestamp());
		assertEquals(ResultIndicator.FAILURE, context.indicator());
		assertEquals("nightly run", context.label());
	}

	@Test
	void toContext_shouldKeepTimestampNull_whenSourceTimestampIsNull() {
		PersistedTestRunResult persisted = new PersistedTestRunResult(4L, 5L, 6L, 7L, null,
				ResultIndicator.PARTIAL_SUCCESS, "label");

		TestRunResultContext fromPersisted = assembler.toContext(persisted);

		assertNull(fromPersisted.timestamp());
	}

}
