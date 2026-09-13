package io.github.yaforster.trails.core.test.result.failure;

import lombok.Getter;
import lombok.experimental.SuperBuilder;

@Getter
@SuperBuilder
public class TechnicalFailure extends Failure {

	public final String exceptionMessageFromAction;

}
