package io.github.yaforster.trails.adapter.api.rest.hateoas;

import io.github.yaforster.trails.adapter.api.rest.hateoas.HATEOASMapper;
import io.github.yaforster.trails.adapter.api.rest.model.LinkDTO;
import org.junit.jupiter.api.Test;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.Links;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class HATEOASMapperTest {

	private final HATEOASMapper mapper = new HATEOASMapper() {
	};

	@Test
	void mapLinks_ShouldReturnEmptyMapForNullLinks() {
		Map<String, LinkDTO> result = mapper.mapLinks(null);
		assertTrue(result.isEmpty());
	}

	@Test
	void mapLinks_ShouldReturnEmptyMapForEmptyLinks() {
		Map<String, LinkDTO> result = mapper.mapLinks(Links.NONE);
		assertTrue(result.isEmpty());
	}

	@Test
	void mapLinks_ShouldMapAllRelationsToLinkDTO() {
		Links links = Links.of(Link.of("https://example.org/resources/1", "self"),
				Link.of("https://example.org/resources/2", "next"));

		Map<String, LinkDTO> result = mapper.mapLinks(links);

		assertEquals(2, result.size());
		assertEquals("https://example.org/resources/1", result.get("self").getHref());
		assertEquals("https://example.org/resources/2", result.get("next").getHref());
	}

	@Test
	void mapLinks_ShouldThrowWhenRelationAppearsMultipleTimes() {
		Links links = Links.of(Link.of("https://example.org/resources/1", "self"),
				Link.of("https://example.org/resources/2", "self"));

		assertThrows(IllegalStateException.class, () -> mapper.mapLinks(links));
	}

}
