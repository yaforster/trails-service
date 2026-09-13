package io.github.yaforster.trails.adapter.api.asyncapi.model;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class TestExecutionStartedEventAsyncDTO {

	private Status status;

	private Map<String, LinkAsyncDTO> links;

	private String message;

	private String executionId;

	public static TestExecutionStartedEventAsyncDTOBuilder builder() {
		return new TestExecutionStartedEventAsyncDTOBuilder();
	}

	public Status getStatus() {
		return status;
	}

	public void setStatus(Status status) {
		this.status = status;
	}

	public Map<String, LinkAsyncDTO> getLinks() {
		return links;
	}

	public void setLinks(Map<String, LinkAsyncDTO> links) {
		this.links = links;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public String getExecutionId() {
		return executionId;
	}

	public void setExecutionId(String executionId) {
		this.executionId = executionId;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (o == null || getClass() != o.getClass()) {
			return false;
		}
		TestExecutionStartedEventAsyncDTO that = (TestExecutionStartedEventAsyncDTO) o;
		return status == that.status && Objects.equals(links, that.links) && Objects.equals(message, that.message)
				&& Objects.equals(executionId, that.executionId);
	}

	@Override
	public int hashCode() {
		return Objects.hash(status, links, message, executionId);
	}

	@Override
	public String toString() {
		return "TestExecutionStartedEventAsyncDTO{" + "status=" + status + ", links=" + links + ", message=" + message
				+ ", executionId=" + executionId + '}';
	}

	public enum Status {

		RUNNING

	}

	public static class TestExecutionStartedEventAsyncDTOBuilder {

		private final TestExecutionStartedEventAsyncDTO value = new TestExecutionStartedEventAsyncDTO();

		private TestExecutionStartedEventAsyncDTOBuilder() {
			value.links = new HashMap<>();
		}

		public TestExecutionStartedEventAsyncDTOBuilder status(Status status) {
			value.status = status;
			return this;
		}

		public TestExecutionStartedEventAsyncDTOBuilder links(Map<String, LinkAsyncDTO> links) {
			value.links = links;
			return this;
		}

		public TestExecutionStartedEventAsyncDTOBuilder linksValue(String key, LinkAsyncDTO link) {
			value.links.put(key, link);
			return this;
		}

		public TestExecutionStartedEventAsyncDTOBuilder message(String message) {
			value.message = message;
			return this;
		}

		public TestExecutionStartedEventAsyncDTOBuilder executionId(String executionId) {
			value.executionId = executionId;
			return this;
		}

		public TestExecutionStartedEventAsyncDTO build() {
			return value;
		}

	}

}
