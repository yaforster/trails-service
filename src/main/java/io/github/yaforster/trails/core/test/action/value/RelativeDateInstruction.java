package io.github.yaforster.trails.core.test.action.value;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public record RelativeDateInstruction(int offsetDays, String formatPattern) implements ValueComputationInstruction {

	@Override
	public String computeValue() {
		return LocalDate.now().plusDays(offsetDays).format(DateTimeFormatter.ofPattern(formatPattern));
	}
}
