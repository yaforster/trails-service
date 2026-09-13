package io.github.yaforster.trails.adapter.runtime.time;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;

class SystemTimeSourceTest {

	@Test
	void currentTimeMillis_shouldReturnAPositiveTime() {
		long result = new SystemTimeSource().currentTimeMillis();

		assertThat(result).isPositive();
	}

	@Test
	void currentTimeMillis_shouldReadSystemClockWithoutThrowing() {
		assertThatCode(() -> new SystemTimeSource().currentTimeMillis()).doesNotThrowAnyException();
	}

}
