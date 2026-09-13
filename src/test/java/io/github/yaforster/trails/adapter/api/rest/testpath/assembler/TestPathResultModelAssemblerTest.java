package io.github.yaforster.trails.adapter.api.rest.testpath.assembler;

import io.github.yaforster.trails.adapter.api.rest.testpath.model.TestPathResultModel;
import io.github.yaforster.trails.core.persisted.PersistedTestPathResult;
import org.junit.jupiter.api.Test;
import org.springframework.hateoas.Link;

import static org.junit.jupiter.api.Assertions.*;

class TestPathResultModelAssemblerTest {

	private final TestPathResultModelAssembler assembler = new TestPathResultModelAssembler();

	private static void assertLinkContainsId(Link link, Long... ids) {
		String href = link.getHref();
		for (Long id : ids) {
			assertTrue(href.contains(String.valueOf(id)), () -> "Expected link '" + href + "' to contain id " + id);
		}
	}

	@Test
	void toModel_shouldMapFieldsAndAllLinks() {
		PersistedTestPathResult persistedTestPathResult = new PersistedTestPathResult(41L, 77L, "main path");
		PersistedTestPathResultContext context = PersistedTestPathResultContext.of(11L, 22L, 33L, 77L,
				persistedTestPathResult);

		TestPathResultModel model = assembler.toModel(context);

		assertEquals(41L, model.getId());

		assertEquals(6, model.getLinks().toList().size());
		assertLinkContainsId(model.getRequiredLink("self"), 41L);
		assertLinkContainsId(model.getRequiredLink("collection"), 11L, 22L, 33L, 77L);
		assertLinkContainsId(model.getRequiredLink("testSetResult"), 11L, 22L, 33L, 77L);
		assertLinkContainsId(model.getRequiredLink("actionResults"), 11L, 22L, 33L, 77L, 41L);
		assertLinkContainsId(model.getRequiredLink("downloadedFiles"), 11L, 22L, 33L, 77L, 41L);
		assertLinkContainsId(model.getRequiredLink("print"), 11L, 22L, 33L, 77L, 41L);
	}

	@Test
	void toModel_shouldThrow_whenPersistedTestPathResultIsNull() {
		PersistedTestPathResultContext context = PersistedTestPathResultContext.of(11L, 22L, 33L, 77L, null);

		assertThrows(NullPointerException.class, () -> assembler.toModel(context));
	}

}
