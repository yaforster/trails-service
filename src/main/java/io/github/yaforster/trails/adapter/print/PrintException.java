package io.github.yaforster.trails.adapter.print;

import io.github.yaforster.trails.core.TrailsException;
import lombok.Getter;
import lombok.experimental.StandardException;
import org.springframework.http.HttpStatus;

@StandardException
public class PrintException extends TrailsException {

	@Getter
	private final HttpStatus statusCode = HttpStatus.INTERNAL_SERVER_ERROR;

}
