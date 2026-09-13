package io.github.yaforster.trails.adapter.api.rest.hateoas;

import org.junit.jupiter.api.Test;
import org.springframework.hateoas.Link;

import static org.assertj.core.api.Assertions.assertThat;

class HATEOASLinksTest {

	@Test
	void shouldDefaultToGet_whenLinkHasNoAffordance() {
		assertThat(HATEOASLinks.methodOf(Link.of("/applications"))).isEqualTo("GET");
	}

	@Test
	void shouldRetainPostMethod_whenEquivalentGetLinkIsCreatedAfterward() {
		Link postLink = HATEOASLinks.post(Link.of("/applications"));
		HATEOASLinks.get(Link.of("/applications"));

		assertThat(HATEOASLinks.methodOf(postLink)).isEqualTo("POST");
	}

	@Test
	void shouldExposePutMethod_whenPutLinkIsMapped() {
		assertThat(HATEOASLinks.methodOf(HATEOASLinks.put(Link.of("/applications/1")))).isEqualTo("PUT");
	}

	@Test
	void shouldExposeDeleteMethod_whenDeleteLinkIsMapped() {
		assertThat(HATEOASLinks.methodOf(HATEOASLinks.delete(Link.of("/applications/1")))).isEqualTo("DELETE");
	}

}
