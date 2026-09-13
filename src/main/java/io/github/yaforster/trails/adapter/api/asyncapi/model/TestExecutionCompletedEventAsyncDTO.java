package io.github.yaforster.trails.adapter.api.asyncapi.model;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class TestExecutionCompletedEventAsyncDTO {

	private Status status;

	private Map<String, LinkAsyncDTO> links;

	private String message;

	private String executionId;

	private Long testRunId;

	public static TestExecutionCompletedEventAsyncDTOBuilder builder() {
		return new TestExecutionCompletedEventAsyncDTOBuilder();
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

	public Long getTestRunId() {
		return testRunId;
	}

	public void setTestRunId(Long testRunId) {
		this.testRunId = testRunId;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (o == null || getClass() != o.getClass()) {
			return false;
		}
		TestExecutionCompletedEventAsyncDTO that = (TestExecutionCompletedEventAsyncDTO) o;
		return status == that.status && Objects.equals(links, that.links) && Objects.equals(message, that.message)
				&& Objects.equals(executionId, that.executionId) && Objects.equals(testRunId, that.testRunId);
	}

	@Override
	public int hashCode() {
		return Objects.hash(status, links, message, executionId, testRunId);
	}

	@Override
	public String toString() {
		return "TestExecutionCompletedEventAsyncDTO{" + "status=" + status + ", links=" + links + ", message=" + message
				+ ", executionId=" + executionId + ", testRunId=" + testRunId + '}';
	}

	public enum Status {

		COMPLETED

	}

	public static class TestExecutionCompletedEventAsyncDTOBuilder {

		private final TestExecutionCompletedEventAsyncDTO value = new TestExecutionCompletedEventAsyncDTO();

		private TestExecutionCompletedEventAsyncDTOBuilder() {
			value.links = new HashMap<>();
		}

		public TestExecutionCompletedEventAsyncDTOBuilder status(Status status) {
			value.status = status;
			return this;
		}

		public TestExecutionCompletedEventAsyncDTOBuilder links(Map<String, LinkAsyncDTO> links) {
			value.links = links;
			return this;
		}

		public TestExecutionCompletedEventAsyncDTOBuilder linksValue(String key, LinkAsyncDTO link) {
			value.links.put(key, link);
			return this;
		}

		public TestExecutionCompletedEventAsyncDTOBuilder message(String message) {
			value.message = message;
			return this;
		}

		public TestExecutionCompletedEventAsyncDTOBuilder executionId(String executionId) {
			value.executionId = executionId;
			return this;
		}

		public TestExecutionCompletedEventAsyncDTOBuilder testRunId(Long testRunId) {
			value.testRunId = testRunId;
			return this;
		}

		public TestExecutionCompletedEventAsyncDTO build() {
			return value;
		}

	}

}
