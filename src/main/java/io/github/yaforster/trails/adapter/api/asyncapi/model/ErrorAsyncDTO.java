package io.github.yaforster.trails.adapter.api.asyncapi.model;

import java.util.Objects;

public class ErrorAsyncDTO {

	private String stacktrace;

	private String problematicElement;

	private String message;

	public static ErrorAsyncDTOBuilder builder() {
		return new ErrorAsyncDTOBuilder();
	}

	public String getStacktrace() {
		return stacktrace;
	}

	public void setStacktrace(String stacktrace) {
		this.stacktrace = stacktrace;
	}

	public String getProblematicElement() {
		return problematicElement;
	}

	public void setProblematicElement(String problematicElement) {
		this.problematicElement = problematicElement;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (o == null || getClass() != o.getClass()) {
			return false;
		}
		ErrorAsyncDTO that = (ErrorAsyncDTO) o;
		return Objects.equals(stacktrace, that.stacktrace)
				&& Objects.equals(problematicElement, that.problematicElement) && Objects.equals(message, that.message);
	}

	@Override
	public int hashCode() {
		return Objects.hash(stacktrace, problematicElement, message);
	}

	@Override
	public String toString() {
		return "ErrorAsyncDTO{" + "stacktrace=" + stacktrace + ", problematicElement=" + problematicElement
				+ ", message=" + message + '}';
	}

	public static class ErrorAsyncDTOBuilder {

		private final ErrorAsyncDTO value = new ErrorAsyncDTO();

		public ErrorAsyncDTOBuilder stacktrace(String stacktrace) {
			value.stacktrace = stacktrace;
			return this;
		}

		public ErrorAsyncDTOBuilder problematicElement(String problematicElement) {
			value.problematicElement = problematicElement;
			return this;
		}

		public ErrorAsyncDTOBuilder message(String message) {
			value.message = message;
			return this;
		}

		public ErrorAsyncDTO build() {
			return value;
		}

	}

}
