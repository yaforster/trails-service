package io.github.yaforster.trails.adapter.api.rest.hateoas;

import io.github.yaforster.trails.adapter.api.rest.model.LinkDTO;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.Links;

import java.util.Collections;
import java.util.Map;
import java.util.stream.Collectors;

public interface HATEOASMapper {

	default Map<String, LinkDTO> mapLinks(Links links) {
		if (links == null || links.isEmpty()) {
			return Collections.emptyMap();
		}
		return links.stream().collect(Collectors.toMap(link -> link.getRel().value(), this::toDTO));
	}

	default LinkDTO toDTO(Link link) {
		return new LinkDTO().href(link.getHref()).method(HATEOASLinks.methodOf(link)).templated(link.isTemplated());
	}

}
