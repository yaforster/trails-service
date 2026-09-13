package io.github.yaforster.trails.core.test.action.value;

public sealed interface ValueComputationInstruction permits EnvInstruction, FixedValueInstruction,
		RandomValueInstruction, RelativeDateInstruction, TestDataValueInstruction, TimeStampInstruction {

	String computeValue();

}
