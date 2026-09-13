package io.github.yaforster.trails.adapter.api.rest.aspect;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.slf4j.MDC;

import java.util.UUID;

@Slf4j
public abstract class TrailsAPIAspect {

	public static final String CORRELATION_ID_KEY = "correlationId";

	protected Object processAPIRequest(ProceedingJoinPoint joinPoint) throws Throwable {
		MDC.put(CORRELATION_ID_KEY, UUID.randomUUID().toString());
		long startNanos = System.nanoTime();
		try {
			log.debug("api request handler={}", joinPoint.getSignature());
			Object response = joinPoint.proceed(joinPoint.getArgs());
			log.debug("api response durationMs={}", (System.nanoTime() - startNanos) / 1_000_000);
			return response;
		}
		catch (Throwable throwable) {
			log.warn("api failure error={}", throwable.getClass().getSimpleName());
			throw throwable;
		}
		finally {
			MDC.remove(CORRELATION_ID_KEY);
		}
	}

}
