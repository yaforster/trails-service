package io.github.yaforster.trails.adapter.api.rest.application.assembler;

import io.github.yaforster.trails.adapter.api.ResourceAuthorization;
import io.github.yaforster.trails.adapter.api.rest.application.model.ApplicationModel;
import io.github.yaforster.trails.core.persisted.PersistedApplication;
import org.junit.jupiter.api.Test;
import org.springframework.hateoas.Link;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ApplicationModelAssemblerTest {

	private final ResourceAuthorization resourceAuthorization = mock(ResourceAuthorization.class);

	private final ApplicationModelAssembler assembler = new ApplicationModelAssembler(resourceAuthorization);

	private static void assertLinkContainsId(Link link, Long... ids) {
		String href = link.getHref();
		for (Long id : ids) {
			assertTrue(href.contains(String.valueOf(id)), () -> "Expected link '" + href + "' to contain id " + id);
		}
	}

	@Test
	void toModel_shouldMapFieldsAndAddStagesLink_whenStagesExist() {
		when(resourceAuthorization.canManageResources()).thenReturn(true);
		PersistedApplication persistedApplication = new PersistedApplication(101L, "My App");
		PersistedApplicationContext context = new PersistedApplicationContext(persistedApplication, true);

		ApplicationModel model = assembler.toModel(context);

		assertEquals(101L, model.getId());
		assertEquals("My App", model.getLabel());

		assertLinkContainsId(model.getRequiredLink("self"), 101L);
		assertLinkContainsId(model.getRequiredLink("stages"), 101L);
		assertTrue(model.getLink("collection").isPresent());
		assertLinkContainsId(model.getRequiredLink("delete"), 101L);
	}

	@Test
	void toModel_shouldNotAddStagesLink_whenNoStagesExist() {
		PersistedApplication persistedApplication = new PersistedApplication(202L, "No Stages App");
		PersistedApplicationContext context = new PersistedApplicationContext(persistedApplication, false);

		ApplicationModel model = assembler.toModel(context);

		assertLinkContainsId(model.getRequiredLink("self"), 202L);
		assertTrue(model.getLink("collection").isPresent());
		assertFalse(model.getLink("stages").isPresent());
	}

	@Test
	void toModel_shouldAddRestoreLink_whenApplicationIsRetired() {
		when(resourceAuthorization.canManageResources()).thenReturn(true);
		PersistedApplication persistedApplication = new PersistedApplication(202L, "Retired App", true);
		PersistedApplicationContext context = new PersistedApplicationContext(persistedApplication, false);

		ApplicationModel model = assembler.toModel(context);

		assertTrue(model.getLink("restore").isPresent());
		assertLinkContainsId(model.getRequiredLink("restore"), 202L);
		assertFalse(model.getLink("delete").isPresent());
	}

	@Test
	void toModel_shouldNotAddMutationLinks_whenUserCannotManageResources() {
		when(resourceAuthorization.canManageResources()).thenReturn(false);
		PersistedApplication persistedApplication = new PersistedApplication(202L, "App");
		PersistedApplicationContext context = new PersistedApplicationContext(persistedApplication, false);

		ApplicationModel model = assembler.toModel(context);

		assertFalse(model.getLink("delete").isPresent());
		assertFalse(model.getLink("restore").isPresent());
	}

	@Test
	void toModel_shouldThrow_whenPersistedApplicationIsNull() {
		PersistedApplicationContext context = new PersistedApplicationContext(null, false);

		assertThrows(NullPointerException.class, () -> assembler.toModel(context));
	}

}
