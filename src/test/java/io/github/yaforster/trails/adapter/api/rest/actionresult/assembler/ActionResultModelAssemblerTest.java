package io.github.yaforster.trails.adapter.api.rest.actionresult.assembler;

import io.github.yaforster.trails.adapter.api.rest.actionresult.model.ActionResultModel;
import io.github.yaforster.trails.core.persisted.ActionResultType;
import io.github.yaforster.trails.core.persisted.PersistedActionResult;
import org.junit.jupiter.api.Test;
import org.springframework.hateoas.Link;

import static org.junit.jupiter.api.Assertions.*;

class ActionResultModelAssemblerTest {

	private final ActionResultModelAssembler assembler = new ActionResultModelAssembler();

	private static void assertLinkContainsId(Link link, Long... ids) {
		String href = link.getHref();
		for (Long id : ids) {
			assertTrue(href.contains(String.valueOf(id)), () -> "Expected link '" + href + "' to contain id " + id);
		}
	}

	@Test
	void toModel_shouldMapFieldsAndLinks() {
		PersistedActionResult persistedActionResult = new PersistedActionResult(5L, 7L, 9L, "Open Login", "Executed",
				ActionResultType.SUCCESS, null);
		PersistedActionResultContext context = PersistedActionResultContext.of(1L, 2L, 3L, 4L, 7L,
				persistedActionResult, true);

		ActionResultModel model = assembler.toModel(context);

		assertEquals(5L, model.getId());
		assertEquals(9L, model.getActionID());
		assertEquals("Open Login", model.getLabel());
		assertEquals("Executed", model.getResultMessage());
		assertEquals(ActionResultType.SUCCESS, model.getResultType());
		assertNull(model.getExceptionMessageFromAction());

		assertEquals(3, model.getLinks().toList().size());
		assertLinkContainsId(model.getRequiredLink("collection"), 1L, 2L, 3L, 4L, 7L);
		assertLinkContainsId(model.getRequiredLink("pathResult"), 1L, 2L, 3L, 4L, 7L);
		assertLinkContainsId(model.getRequiredLink("screenshot"), 1L, 2L, 3L, 4L, 7L, 5L);
	}

	@Test
	void toModel_shouldMapAllResultTypes() {
		assertResultTypeMapping(ActionResultType.SUCCESS);
		assertResultTypeMapping(ActionResultType.SKIPPED);
		assertResultTypeMapping(ActionResultType.VALIDATION_FAILURE);
		assertResultTypeMapping(ActionResultType.TECHNICAL_FAILURE);
	}

	@Test
	void toModel_shouldThrow_whenPersistedActionResultIsNull() {
		PersistedActionResultContext context = PersistedActionResultContext.of(1L, 2L, 3L, 4L, 42L, null, true);

		assertThrows(NullPointerException.class, () -> assembler.toModel(context));
	}

	private void assertResultTypeMapping(ActionResultType source) {
		PersistedActionResult persistedActionResult = new PersistedActionResult(1L, 2L, 3L, "l", "m", source, "e");
		PersistedActionResultContext context = PersistedActionResultContext.of(1L, 2L, 3L, 4L, 2L,
				persistedActionResult, true);

		ActionResultModel model = assembler.toModel(context);

		assertEquals(source, model.getResultType());
	}

}
