package io.github.yaforster.trails.adapter.api.rest.run;

import io.github.yaforster.trails.core.TrailsTest;
import io.github.yaforster.trails.core.data.ResultIndicator;
import io.github.yaforster.trails.core.persisted.PersistedTestRunResult;
import org.junit.jupiter.api.Test;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.Links;

import java.sql.Timestamp;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class TestExecutionLinkFactoryHATEOASTest extends TrailsTest {

	private final TestExecutionLinkFactoryHATEOAS factory = new TestExecutionLinkFactoryHATEOAS();

	@Test
	void startedLinks_ShouldContainSelfLinkToEventStream() {
		getInstancioOf(UUID.class).stream().limit(TEST_REPETITIONS).forEach(executionId -> {
			Links links = factory.startedLinks(executionId);

			assertEquals(1, links.toList().size());
			Link self = links.getRequiredLink("self");
			assertTrue(self.getHref().contains("/api/test/events/" + executionId));
		});
	}

	@Test
	void startedLinks_ShouldHandleNullExecutionId() {
		Links links = factory.startedLinks(null);

		assertEquals(1, links.toList().size());
		assertTrue(links.getRequiredLink("self").getHref().contains("/api/test/events"));
	}

	@Test
	void completedLinks_ShouldContainSelfAndResultLinks() {
		getInstancioOf(Integer.class).stream().limit(TEST_REPETITIONS).forEach(ignored -> {
			UUID executionId = getInstancioOf(UUID.class).create();
			Long runId = getInstancioOf(Long.class).create();
			Long applicationId = getInstancioOf(Long.class).create();
			Long stageId = getInstancioOf(Long.class).create();
			PersistedTestRunResult result = persistedResult(runId, applicationId, stageId);

			Links links = factory.completedLinks(executionId, result);

			assertEquals(2, links.toList().size());
			assertTrue(links.getRequiredLink("self").getHref().contains("/api/test/events/" + executionId));
			assertTrue(links.getRequiredLink("result")
				.getHref()
				.contains("/api/testruns/" + applicationId + "/" + stageId + "/" + runId));
		});
	}

	@Test
	void completedLinks_ShouldThrowWhenResultIsNull() {
		UUID executionId = getInstancioOf(UUID.class).create();

		assertThrows(NullPointerException.class, () -> factory.completedLinks(executionId, null));
	}

	@Test
	void failedLinks_ShouldContainSelfAndTemplatedResultLinks() {
		getInstancioOf(UUID.class).stream().limit(TEST_REPETITIONS).forEach(executionId -> {
			Links links = factory.failedLinks(executionId);

			assertEquals(2, links.toList().size());
			assertTrue(links.getRequiredLink("self").getHref().contains("/api/test/events/" + executionId));
			assertEquals("/api/testruns/{applicationId}/{stageId}/{testRunId}",
					links.getRequiredLink("result").getHref());
		});
	}

	private PersistedTestRunResult persistedResult(Long runId, Long applicationId, Long stageId) {
		return new PersistedTestRunResult(runId, applicationId, stageId, getInstancioOf(Long.class).create(),
				getInstancioOf(Timestamp.class).create(), getInstancioOf(ResultIndicator.class).create(),
				getInstancioOf(String.class).create());
	}

}
