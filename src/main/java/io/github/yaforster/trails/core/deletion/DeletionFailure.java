package io.github.yaforster.trails.core.deletion;

public record DeletionFailure(Long id, String message, ErrorDetails error) implements DatabaseDeletionResult {

	public DeletionFailure(Long id, Throwable throwable) {
		this(id, throwable.getLocalizedMessage(), ErrorDetails.fromException(throwable));
	}
}
