package io.github.yaforster.trails.core;

import lombok.experimental.StandardException;

@StandardException
public abstract class TrailsException extends RuntimeException {

	public ErrorClassification errorClassification() {
		return ErrorClassification.INTERNAL_ERROR;
	}

}
