package io.github.yaforster.trails.adapter.api.rest.common.mapper;

import io.github.yaforster.trails.adapter.api.rest.hateoas.HATEOASLinks;
import io.github.yaforster.trails.adapter.api.rest.model.LinkDTO;
import org.springframework.hateoas.Links;

import java.util.Map;
import java.util.stream.Collectors;

public abstract class PagedContentMapper {

	public Map<String, LinkDTO> mapLinks(Links links) {
		return links.stream().collect(Collectors.toMap(link -> link.getRel().value(), link -> {
			LinkDTO l = new LinkDTO();
			l.setHref(link.getHref());
			l.setMethod(HATEOASLinks.methodOf(link));
			l.setTemplated(link.isTemplated());
			return l;
		}, (existing, _) -> existing // Handle duplicate relations if necessary
		));
	}

}
