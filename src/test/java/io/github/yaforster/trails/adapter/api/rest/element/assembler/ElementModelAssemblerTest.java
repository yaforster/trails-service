package io.github.yaforster.trails.adapter.api.rest.element.assembler;

import io.github.yaforster.trails.adapter.api.ResourceAuthorization;
import io.github.yaforster.trails.adapter.api.rest.element.model.ElementModel;
import io.github.yaforster.trails.core.data.ElementType;
import io.github.yaforster.trails.core.persisted.PersistedElement;
import io.github.yaforster.trails.core.test.action.element.LocatorType;
import org.junit.jupiter.api.Test;
import org.springframework.hateoas.Link;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ElementModelAssemblerTest {

	private static void assertLinkContainsId(Link link, Long... ids) {
		String href = link.getHref();
		for (Long id : ids) {
			assertTrue(href.contains(String.valueOf(id)), () -> "Expected link '" + href + "' to contain id " + id);
		}
	}

	@Test
	void toModel_shouldMapFieldsAndAddScreenshotLink_whenScreenshotExists() {
		ResourceAuthorization resourceAuthorization = mock(ResourceAuthorization.class);
		when(resourceAuthorization.canManageResources()).thenReturn(true);
		ElementModelAssembler assembler = new ElementModelAssembler(resourceAuthorization);
		PersistedElement persistedElement = new PersistedElement(19L, "Login Button", LocatorType.CSS, 11L, 22L,
				"#login", ElementType.BUTTON);
		PersistedElementContext context = new PersistedElementContext(11L, 22L, persistedElement, Optional.of(999L));

		ElementModel model = assembler.toModel(context);

		assertEquals(19L, model.getId());
		assertEquals("Login Button", model.getLabel());
		assertEquals(ElementType.BUTTON, model.getType());
		assertEquals("#login", model.getLocatorString());
		assertEquals(LocatorType.CSS, model.getLocatorType());

		assertLinkContainsId(model.getRequiredLink("collection"), 11L, 22L);
		assertLinkContainsId(model.getRequiredLink("self"), 11L, 22L, 19L);
		assertLinkContainsId(model.getRequiredLink("screenshot"), 19L);
		assertLinkContainsId(model.getRequiredLink("deleteScreenshot"), 11L, 22L, 19L);
		assertLinkContainsId(model.getRequiredLink("delete"), 11L, 22L, 19L);
		assertLinkContainsId(model.getRequiredLink("promote"), 11L, 22L, 19L);
	}

	@Test
	void toModel_shouldNotAddScreenshotLink_whenScreenshotIsMissing() {
		ResourceAuthorization resourceAuthorization = mock(ResourceAuthorization.class);
		when(resourceAuthorization.canManageResources()).thenReturn(true);
		ElementModelAssembler assembler = new ElementModelAssembler(resourceAuthorization);
		PersistedElement persistedElement = new PersistedElement(20L, "Username Input", LocatorType.XPATH, 101L, 202L,
				"//input[@id='username']", ElementType.INPUT);
		PersistedElementContext context = new PersistedElementContext(101L, 202L, persistedElement, Optional.empty());

		ElementModel model = assembler.toModel(context);

		assertLinkContainsId(model.getRequiredLink("collection"), 101L, 202L);
		assertLinkContainsId(model.getRequiredLink("self"), 101L, 202L, 20L);
		assertLinkContainsId(model.getRequiredLink("uploadScreenshot"), 101L, 202L, 20L);
		assertLinkContainsId(model.getRequiredLink("delete"), 101L, 202L, 20L);
		assertTrue(model.getRequiredLink("promote").getHref().endsWith("/promote{?targetStageId}"));
		assertFalse(model.getLink("screenshot").isPresent());
	}

	@Test
	void toModel_shouldNotAddPromoteLink_whenUserCannotPromoteResources() {
		ResourceAuthorization resourceAuthorization = mock(ResourceAuthorization.class);
		when(resourceAuthorization.canManageResources()).thenReturn(false);
		ElementModelAssembler assembler = new ElementModelAssembler(resourceAuthorization);
		PersistedElement persistedElement = new PersistedElement(20L, "Username Input", LocatorType.XPATH, 101L, 202L,
				"//input[@id='username']", ElementType.INPUT);
		PersistedElementContext context = new PersistedElementContext(101L, 202L, persistedElement, Optional.empty());

		ElementModel model = assembler.toModel(context);

		assertFalse(model.getLink("promote").isPresent());
		assertFalse(model.getLink("delete").isPresent());
		assertFalse(model.getLink("uploadScreenshot").isPresent());
	}

	@Test
	void toModel_shouldAddRestoreLinkForRetiredElement_whenUserCanManageResources() {
		ResourceAuthorization resourceAuthorization = mock(ResourceAuthorization.class);
		when(resourceAuthorization.canManageResources()).thenReturn(true);
		ElementModelAssembler assembler = new ElementModelAssembler(resourceAuthorization);
		PersistedElement persistedElement = new PersistedElement(20L, "Username Input", LocatorType.XPATH, 101L, 202L,
				"//input[@id='username']", ElementType.INPUT, true);
		PersistedElementContext context = new PersistedElementContext(101L, 202L, persistedElement, Optional.empty());

		ElementModel model = assembler.toModel(context);

		assertLinkContainsId(model.getRequiredLink("restore"), 101L, 202L, 20L);
		assertFalse(model.getLink("delete").isPresent());
		assertFalse(model.getLink("promote").isPresent());
	}

	@Test
	void toModel_shouldThrow_whenPersistedElementIsNull() {
		ResourceAuthorization resourceAuthorization = mock(ResourceAuthorization.class);
		ElementModelAssembler assembler = new ElementModelAssembler(resourceAuthorization);
		PersistedElementContext context = new PersistedElementContext(1L, 2L, null, Optional.empty());

		assertThrows(NullPointerException.class, () -> assembler.toModel(context));
	}

}
