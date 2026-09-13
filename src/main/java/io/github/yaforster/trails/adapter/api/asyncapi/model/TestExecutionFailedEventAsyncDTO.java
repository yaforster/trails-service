package io.github.yaforster.trails.adapter.api.asyncapi.model;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class TestExecutionFailedEventAsyncDTO {

	private Status status;

	private ErrorAsyncDTO error;

	private Map<String, LinkAsyncDTO> links;

	private String message;

	private String executionId;

	public static TestExecutionFailedEventAsyncDTOBuilder builder() {
		return new TestExecutionFailedEventAsyncDTOBuilder();
	}

	public Status getStatus() {
		return status;
	}

	public void setStatus(Status status) {
		this.status = status;
	}

	public ErrorAsyncDTO getError() {
		return error;
	}

	public void setError(ErrorAsyncDTO error) {
		this.error = error;
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
		TestExecutionFailedEventAsyncDTO that = (TestExecutionFailedEventAsyncDTO) o;
		return status == that.status && Objects.equals(error, that.error) && Objects.equals(links, that.links)
				&& Objects.equals(message, that.message) && Objects.equals(executionId, that.executionId);
	}

	@Override
	public int hashCode() {
		return Objects.hash(status, error, links, message, executionId);
	}

	@Override
	public String toString() {
		return "TestExecutionFailedEventAsyncDTO{" + "status=" + status + ", error=" + error + ", links=" + links
				+ ", message=" + message + ", executionId=" + executionId + '}';
	}

	public enum Status {

		FAILED

	}

	public static class TestExecutionFailedEventAsyncDTOBuilder {

		private final TestExecutionFailedEventAsyncDTO value = new TestExecutionFailedEventAsyncDTO();

		private TestExecutionFailedEventAsyncDTOBuilder() {
			value.links = new HashMap<>();
		}

		public TestExecutionFailedEventAsyncDTOBuilder status(Status status) {
			value.status = status;
			return this;
		}

		public TestExecutionFailedEventAsyncDTOBuilder error(ErrorAsyncDTO error) {
			value.error = error;
			return this;
		}

		public TestExecutionFailedEventAsyncDTOBuilder links(Map<String, LinkAsyncDTO> links) {
			value.links = links;
			return this;
		}

		public TestExecutionFailedEventAsyncDTOBuilder linksValue(String key, LinkAsyncDTO link) {
			value.links.put(key, link);
			return this;
		}

		public TestExecutionFailedEventAsyncDTOBuilder message(String message) {
			value.message = message;
			return this;
		}

		public TestExecutionFailedEventAsyncDTOBuilder executionId(String executionId) {
			value.executionId = executionId;
			return this;
		}

		public TestExecutionFailedEventAsyncDTO build() {
			return value;
		}

	}

}
