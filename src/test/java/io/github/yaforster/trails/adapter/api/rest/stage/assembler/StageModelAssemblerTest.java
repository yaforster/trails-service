package io.github.yaforster.trails.adapter.api.rest.stage.assembler;

import io.github.yaforster.trails.adapter.api.ResourceAuthorization;
import io.github.yaforster.trails.adapter.api.rest.stage.model.StageModel;
import io.github.yaforster.trails.core.persisted.PersistedStage;
import org.junit.jupiter.api.Test;
import org.springframework.hateoas.Link;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class StageModelAssemblerTest {

	private final ResourceAuthorization resourceAuthorization = mock(ResourceAuthorization.class);

	private final StageModelAssembler assembler = new StageModelAssembler(resourceAuthorization);

	private static void assertLinkContainsId(Link link, Long... ids) {
		String href = link.getHref();
		for (Long id : ids) {
			assertTrue(href.contains(String.valueOf(id)), () -> "Expected link '" + href + "' to contain id " + id);
		}
	}

	@Test
	void toModel_shouldMapFieldsAndAddElementsLink_whenElementsExist() {
		when(resourceAuthorization.canManageResources()).thenReturn(true);
		PersistedStage persistedStage = new PersistedStage(19L, 11L, "QA", "https://qa.example.com");
		PersistedStageContext context = new PersistedStageContext(11L, persistedStage, true);

		StageModel model = assembler.toModel(context);

		assertEquals(19L, model.getId());
		assertEquals("QA", model.getLabel());
		assertEquals("https://qa.example.com", model.getUrl());

		assertLinkContainsId(model.getRequiredLink("collection"), 11L);
		assertLinkContainsId(model.getRequiredLink("self"), 11L, 19L);
		assertLinkContainsId(model.getRequiredLink("elements"), 11L, 19L);
		assertLinkContainsId(model.getRequiredLink("testPlans"), 11L, 19L);
		assertLinkContainsId(model.getRequiredLink("testRuns"), 11L, 19L);
		assertLinkContainsId(model.getRequiredLink("delete"), 11L, 19L);
	}

	@Test
	void toModel_shouldNotAddElementsLink_whenNoElementsExist() {
		PersistedStage persistedStage = new PersistedStage(29L, 21L, "Prod", "https://prod.example.com");
		PersistedStageContext context = new PersistedStageContext(21L, persistedStage, false);

		StageModel model = assembler.toModel(context);

		assertLinkContainsId(model.getRequiredLink("collection"), 21L);
		assertLinkContainsId(model.getRequiredLink("self"), 21L, 29L);
		assertFalse(model.getLink("elements").isPresent());
		assertLinkContainsId(model.getRequiredLink("testPlans"), 21L, 29L);
		assertLinkContainsId(model.getRequiredLink("testRuns"), 21L, 29L);
	}

	@Test
	void toModel_shouldAddRestoreLink_whenStageIsRetiredAndUserCanManageResources() {
		when(resourceAuthorization.canManageResources()).thenReturn(true);
		PersistedStage persistedStage = new PersistedStage(29L, 21L, "Prod", "https://prod.example.com", true);
		PersistedStageContext context = new PersistedStageContext(21L, persistedStage, false);

		StageModel model = assembler.toModel(context);

		assertLinkContainsId(model.getRequiredLink("restore"), 21L, 29L);
		assertFalse(model.getLink("delete").isPresent());
	}

	@Test
	void toModel_shouldNotAddMutationLinks_whenUserCannotManageResources() {
		when(resourceAuthorization.canManageResources()).thenReturn(false);
		PersistedStage persistedStage = new PersistedStage(29L, 21L, "Prod", "https://prod.example.com");
		PersistedStageContext context = new PersistedStageContext(21L, persistedStage, false);

		StageModel model = assembler.toModel(context);

		assertFalse(model.getLink("delete").isPresent());
		assertFalse(model.getLink("restore").isPresent());
	}

	@Test
	void toModel_shouldThrow_whenPersistedStageIsNull() {
		PersistedStageContext context = new PersistedStageContext(1L, null, false);

		assertThrows(NullPointerException.class, () -> assembler.toModel(context));
	}

}
