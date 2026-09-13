package io.github.yaforster.trails.adapter.api.rest.artifact.assembler;

import io.github.yaforster.trails.adapter.api.rest.artifact.model.ArtifactModel;
import io.github.yaforster.trails.core.ArtifactType;
import io.github.yaforster.trails.core.persisted.PersistedArtifact;
import org.junit.jupiter.api.Test;
import org.springframework.hateoas.Link;

import static org.junit.jupiter.api.Assertions.*;

class ArtifactModelAssemblerTest {

	private final ArtifactModelAssembler assembler = new ArtifactModelAssembler();

	private static void assertLinkContainsId(Link link, Long... ids) {
		String href = link.getHref();
		for (Long id : ids) {
			assertTrue(href.contains(String.valueOf(id)), () -> "Expected link '" + href + "' to contain id " + id);
		}
	}

	@Test
	void toModel_shouldMapFieldsAndLinks() {
		PersistedArtifact persistedArtifact = new PersistedArtifact(15L, 55L, ArtifactType.SCREENSHOT, "screen.png",
				"image/png", 1234L, "s3://bucket/screen.png");
		PersistedArtifactContext context = new PersistedArtifactContext(1L, 2L, 3L, 4L, 77L, persistedArtifact);

		ArtifactModel model = assembler.toModel(context);

		assertEquals(15L, model.getId());
		assertEquals("screen.png", model.getFilename());
		assertEquals(ArtifactModel.Type.OTHER, model.getType());

		assertEquals(2, model.getLinks().toList().size());
		assertLinkContainsId(model.getRequiredLink("self"), 1L, 2L, 3L, 4L, 77L, 15L);
		assertLinkContainsId(model.getRequiredLink("collection"), 1L, 2L, 3L, 4L, 77L);
	}

	@Test
	void toModel_shouldThrow_whenPersistedArtifactIsNull() {
		PersistedArtifactContext context = new PersistedArtifactContext(1L, 2L, 3L, 4L, 77L, null);

		assertThrows(NullPointerException.class, () -> assembler.toModel(context));
	}

}
