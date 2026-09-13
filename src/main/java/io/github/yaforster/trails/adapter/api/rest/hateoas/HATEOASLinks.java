package io.github.yaforster.trails.adapter.api.rest.hateoas;

import org.springframework.hateoas.AffordanceModel;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.mediatype.Affordances;
import org.springframework.http.HttpMethod;

import java.util.stream.StreamSupport;

public final class HATEOASLinks {

	private static final HttpMethod DEFAULT_METHOD = HttpMethod.GET;

	private HATEOASLinks() {
	}

	public static Link get(Link link) {
		return method(link, DEFAULT_METHOD);
	}

	public static Link post(Link link) {
		return method(link, HttpMethod.POST);
	}

	public static Link put(Link link) {
		return method(link, HttpMethod.PUT);
	}

	public static Link delete(Link link) {
		return method(link, HttpMethod.DELETE);
	}

	public static String methodOf(Link link) {
		return link.getAffordances()
			.stream()
			.flatMap(affordance -> StreamSupport.stream(affordance.spliterator(), false))
			.map(AffordanceModel::getHttpMethod)
			.findFirst()
			.orElse(DEFAULT_METHOD)
			.name();
	}

	private static Link method(Link link, HttpMethod method) {
		return Affordances.of(link).afford(method).toLink();
	}

}
