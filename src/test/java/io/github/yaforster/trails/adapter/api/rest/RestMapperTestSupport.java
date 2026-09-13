package io.github.yaforster.trails.adapter.api.rest;

import org.springframework.hateoas.Link;
import org.springframework.hateoas.PagedModel;

import java.util.List;

final class RestMapperTestSupport {

	private RestMapperTestSupport() {
	}

	static <T> PagedModel<T> paged(List<T> content) {
		PagedModel.PageMetadata metadata = new PagedModel.PageMetadata(2, 1, 5, 3);
		return PagedModel.of(content, metadata, Link.of("/page/self", "self"));
	}

}
