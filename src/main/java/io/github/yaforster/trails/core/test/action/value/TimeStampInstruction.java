package io.github.yaforster.trails.core.test.action.value;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public record TimeStampInstruction(String formatPattern) implements ValueComputationInstruction {

	@Override
	public String computeValue() {
		LocalDateTime dateTime = LocalDateTime.now();
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern(formatPattern);
		return dateTime.format(formatter);
	}
}
