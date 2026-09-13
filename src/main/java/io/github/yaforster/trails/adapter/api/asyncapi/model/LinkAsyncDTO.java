package io.github.yaforster.trails.adapter.api.asyncapi.model;

import java.util.Objects;

public class LinkAsyncDTO {

	private String href;

	public static LinkAsyncDTOBuilder builder() {
		return new LinkAsyncDTOBuilder();
	}

	public String getHref() {
		return href;
	}

	public void setHref(String href) {
		this.href = href;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (o == null || getClass() != o.getClass()) {
			return false;
		}
		LinkAsyncDTO that = (LinkAsyncDTO) o;
		return Objects.equals(href, that.href);
	}

	@Override
	public int hashCode() {
		return Objects.hash(href);
	}

	@Override
	public String toString() {
		return "LinkAsyncDTO{" + "href=" + href + '}';
	}

	public static class LinkAsyncDTOBuilder {

		private final LinkAsyncDTO value = new LinkAsyncDTO();

		public LinkAsyncDTOBuilder href(String href) {
			value.href = href;
			return this;
		}

		public LinkAsyncDTO build() {
			return value;
		}

	}

}
