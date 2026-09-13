package io.github.yaforster.trails.adapter.api;

import lombok.Getter;
import lombok.experimental.StandardException;
import org.springframework.http.HttpStatus;

@StandardException
public class NotAllowedException extends RuntimeException {

	@Getter
	private final HttpStatus statusCode = HttpStatus.BAD_REQUEST;

}
