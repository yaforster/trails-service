package io.github.yaforster.trails.adapter.api.rest.common.mapper;

import io.github.yaforster.trails.adapter.api.rest.common.mapper.PagedContentMapper;
import io.github.yaforster.trails.adapter.api.rest.model.LinkDTO;
import org.junit.jupiter.api.Test;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.Links;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

class PagedContentMapperTest {

	private final PagedContentMapper mapper = new PagedContentMapper() {
	};

	@Test
	void mapLinks_ShouldMapAllRelationsToLinkDTO() {
		Links links = Links.of(Link.of("https://example.org/resources?page=0", "self"),
				Link.of("https://example.org/resources?page=1", "next"));

		Map<String, LinkDTO> result = mapper.mapLinks(links);

		assertEquals(2, result.size());
		assertEquals("https://example.org/resources?page=0", result.get("self").getHref());
		assertEquals("https://example.org/resources?page=1", result.get("next").getHref());
	}

	@Test
	void mapLinks_ShouldKeepFirstHrefWhenRelAppearsMultipleTimes() {
		Links links = Links.of(Link.of("https://example.org/resources?page=0", "self"),
				Link.of("https://example.org/resources?page=999", "self"));

		Map<String, LinkDTO> result = mapper.mapLinks(links);

		assertEquals(1, result.size());
		assertEquals("https://example.org/resources?page=0", result.get("self").getHref());
	}

}
