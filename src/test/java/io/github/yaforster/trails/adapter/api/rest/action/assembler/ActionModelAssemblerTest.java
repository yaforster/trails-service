package io.github.yaforster.trails.adapter.api.rest.action.assembler;

import io.github.yaforster.trails.adapter.api.rest.action.model.ActionModel;
import io.github.yaforster.trails.core.persisted.PersistedAction;
import io.github.yaforster.trails.core.test.Action;
import io.github.yaforster.trails.core.test.Position;
import org.junit.jupiter.api.Test;
import org.springframework.hateoas.Link;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ActionModelAssemblerTest {

	private final ActionModelAssembler assembler = new ActionModelAssembler();

	private static void assertLinkContainsId(Link link, Long... ids) {
		String href = link.getHref();
		for (Long id : ids) {
			assertTrue(href.contains(String.valueOf(id)), () -> "Expected link '" + href + "' to contain id " + id);
		}
	}

	@Test
	void toModel_shouldMapFieldsAndLinks() {
		Action action = mock(Action.class);
		PersistedAction persistedAction = new PersistedAction(101L, 300L, 909L, action);
		PersistedActionContext context = new PersistedActionContext(11L, 22L, 33L, persistedAction);

		Position position = new Position(BigDecimal.TEN, BigDecimal.ONE);
		when(action.getActionID()).thenReturn(101L);
		when(action.getLabel()).thenReturn("click submit");
		when(action.getNextActions()).thenReturn(List.of(201L, 202L));
		when(action.getPosition()).thenReturn(position);

		ActionModel model = assembler.toModel(context);

		assertEquals(101L, model.getActionID());
		assertEquals(909L, model.getReferenceID());
		assertEquals(11L, model.getApplicationId());
		assertEquals(22L, model.getStageId());
		assertEquals("click submit", model.getLabel());
		assertEquals(List.of(201L, 202L), model.getNextActions());
		assertSame(position, model.getPosition());
		assertSame(action, model.getAction());

		assertEquals(2, model.getLinks().toList().size());
		assertLinkContainsId(model.getRequiredLink("collection"), 11L, 22L, 33L);
		assertLinkContainsId(model.getRequiredLink("testPlan"), 11L, 22L, 33L);
	}

	@Test
	void toModel_shouldThrow_whenPersistedActionIsNull() {
		PersistedActionContext context = new PersistedActionContext(1L, 2L, 3L, null);

		assertThrows(NullPointerException.class, () -> assembler.toModel(context));
	}

}
