package io.github.yaforster.trails.adapter.api.rest.common;

import io.github.yaforster.trails.core.PagedResult;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

public final class PagedResultPageAdapter {

	private PagedResultPageAdapter() {
	}

	public static <T> Page<T> toSpringPage(PagedResult<T> pagedResult) {
		return new PageImpl<>(pagedResult.items(), PageRequest.of(pagedResult.page(), pagedResult.size()),
				pagedResult.totalItems());
	}

}
