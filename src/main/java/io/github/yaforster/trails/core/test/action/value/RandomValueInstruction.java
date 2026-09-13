package io.github.yaforster.trails.core.test.action.value;

import java.util.Optional;
import java.util.concurrent.ThreadLocalRandom;

public record RandomValueInstruction(String prefix, String suffix, String charPool,
		int randomStringLength) implements ValueComputationInstruction {

	@Override
	public String computeValue() {
		return generateRandomStringAccordingTo(prefix, charPool, randomStringLength, suffix);
	}

	private String generateRandomStringAccordingTo(String prefix, String charPool, int randomStringLength,
			String suffix) {
		final String safePrefix = Optional.ofNullable(prefix).orElse("");
		final String safeSuffix = Optional.ofNullable(suffix).orElse("");
		final String safeCharPool = Optional.ofNullable(charPool).orElse("");
		if (safeCharPool.isEmpty() || randomStringLength <= 0) {
			return safePrefix + safeSuffix;
		}
		final StringBuilder randomPart = generateRandomPart(randomStringLength, safeCharPool);
		return safePrefix + randomPart + safeSuffix;
	}

	private StringBuilder generateRandomPart(int randomStringLength, String safeCharPool) {
		final StringBuilder randomPart = new StringBuilder(randomStringLength);
		final int poolLength = safeCharPool.length();
		final ThreadLocalRandom rng = ThreadLocalRandom.current();
		for (int i = 0; i < randomStringLength; i++) {
			int index = rng.nextInt(poolLength);
			randomPart.append(safeCharPool.charAt(index));
		}
		return randomPart;
	}
}
