package io.github.yaforster.trails.adapter.api.rest;

import io.github.yaforster.trails.adapter.api.rest.aspect.TrailsAPIAspect;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.Signature;
import org.junit.jupiter.api.Test;
import org.slf4j.MDC;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class TrailsAPIAspectTest {

	@Test
	void processAPIRequest_shouldProceedWithOriginalArgumentsAndClearCorrelationId_whenHandlerSucceeds()
			throws Throwable {
		Object[] arguments = { "payload" };
		ProceedingJoinPoint joinPoint = joinPoint(arguments, "response");
		TestTrailsAPIAspect aspect = new TestTrailsAPIAspect();

		Object response = aspect.process(joinPoint);

		assertThat(response).isEqualTo("response");
		verify(joinPoint).proceed(arguments);
		assertThat(MDC.get("correlationId")).isNull();
	}

	@Test
	void processAPIRequest_shouldClearCorrelationId_whenHandlerFails() throws Throwable {
		ProceedingJoinPoint joinPoint = joinPoint(new Object[0], new IllegalStateException("boom"));
		TestTrailsAPIAspect aspect = new TestTrailsAPIAspect();

		assertThatThrownBy(() -> aspect.process(joinPoint)).isInstanceOf(IllegalStateException.class);

		assertThat(MDC.get("correlationId")).isNull();
	}

	private ProceedingJoinPoint joinPoint(Object[] arguments, Object outcome) throws Throwable {
		ProceedingJoinPoint joinPoint = mock(ProceedingJoinPoint.class);
		Signature signature = mock(Signature.class);
		when(joinPoint.getArgs()).thenReturn(arguments);
		when(joinPoint.getSignature()).thenReturn(signature);
		when(signature.toString()).thenReturn("handler()");
		if (outcome instanceof Throwable throwable) {
			when(joinPoint.proceed(arguments)).thenThrow(throwable);
		}
		else {
			when(joinPoint.proceed(arguments)).thenAnswer(invocation -> {
				assertThat(UUID.fromString(MDC.get("correlationId"))).isNotNull();
				return outcome;
			});
		}
		return joinPoint;
	}

	private static class TestTrailsAPIAspect extends TrailsAPIAspect {

		private Object process(ProceedingJoinPoint joinPoint) throws Throwable {
			return processAPIRequest(joinPoint);
		}

	}

}
