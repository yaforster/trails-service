package io.github.yaforster.trails.adapter.api.asyncapi.run;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.Signature;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class AsyncAPIAspectTest {

	@Test
	void wrapAsyncAPI_shouldReturnProceedResponse_whenJoinPointSucceeds() throws Throwable {
		ResponseEntity<String> expected = ResponseEntity.ok("published");
		ProceedingJoinPoint joinPoint = joinPoint(new Object[0], expected);
		AsyncAPIAspect aspect = new AsyncAPIAspect(mock(AsyncAPIExceptionMapper.class));

		Object response = aspect.wrapAsyncAPI(joinPoint);

		assertSame(expected, response);
	}

	@Test
	void wrapAsyncAPI_shouldProceedWithOriginalArguments_whenJoinPointSucceeds() throws Throwable {
		Object[] arguments = new Object[] { "event" };
		ProceedingJoinPoint joinPoint = joinPoint(arguments, ResponseEntity.accepted().build());
		AsyncAPIAspect aspect = new AsyncAPIAspect(mock(AsyncAPIExceptionMapper.class));

		aspect.wrapAsyncAPI(joinPoint);

		verify(joinPoint).proceed(arguments);
	}

	@Test
	void wrapAsyncAPI_shouldReturnNull_whenExceptionWasMapped() throws Throwable {
		UUID executionId = UUID.randomUUID();
		RuntimeException exception = new RuntimeException("boom");
		ProceedingJoinPoint joinPoint = joinPoint(new Object[] { executionId }, exception);
		AsyncAPIExceptionMapper exceptionMapper = mock(AsyncAPIExceptionMapper.class);
		when(exceptionMapper.toFailedEvent(new Object[] { executionId }, exception)).thenReturn(true);
		AsyncAPIAspect aspect = new AsyncAPIAspect(exceptionMapper);

		Object response = aspect.wrapAsyncAPI(joinPoint);

		assertNull(response);
	}

	@Test
	void wrapAsyncAPI_shouldRethrowException_whenExceptionCouldNotBeMapped() throws Throwable {
		RuntimeException exception = new RuntimeException("boom");
		ProceedingJoinPoint joinPoint = joinPoint(new Object[0], exception);
		AsyncAPIExceptionMapper exceptionMapper = mock(AsyncAPIExceptionMapper.class);
		when(exceptionMapper.toFailedEvent(new Object[0], exception)).thenReturn(false);
		AsyncAPIAspect aspect = new AsyncAPIAspect(exceptionMapper);

		assertThrows(RuntimeException.class, () -> aspect.wrapAsyncAPI(joinPoint));
	}

	@Test
	void wrapAsyncAPI_shouldPassOriginalArgumentsToExceptionMapper_whenJoinPointThrowsException() throws Throwable {
		Object[] arguments = new Object[] { "event" };
		RuntimeException exception = new RuntimeException("boom");
		ProceedingJoinPoint joinPoint = joinPoint(arguments, exception);
		AsyncAPIExceptionMapper exceptionMapper = mock(AsyncAPIExceptionMapper.class);
		when(exceptionMapper.toFailedEvent(arguments, exception)).thenReturn(true);
		AsyncAPIAspect aspect = new AsyncAPIAspect(exceptionMapper);

		aspect.wrapAsyncAPI(joinPoint);

		verify(exceptionMapper).toFailedEvent(arguments, exception);
	}

	@Test
	void asyncConsumers_shouldBeCallableAsAspectPointcut() {
		AsyncAPIAspect aspect = new AsyncAPIAspect(mock(AsyncAPIExceptionMapper.class));

		aspect.asyncConsumers();
	}

	@Test
	void asyncPublishers_shouldBeCallableAsAspectPointcut() {
		AsyncAPIAspect aspect = new AsyncAPIAspect(mock(AsyncAPIExceptionMapper.class));

		aspect.asyncPublishers();
	}

	private ProceedingJoinPoint joinPoint(Object[] arguments, ResponseEntity<?> response) throws Throwable {
		ProceedingJoinPoint joinPoint = mock(ProceedingJoinPoint.class);
		Signature signature = mock(Signature.class);
		when(joinPoint.getArgs()).thenReturn(arguments);
		when(joinPoint.getSignature()).thenReturn(signature);
		when(signature.getName()).thenReturn("method");
		when(joinPoint.proceed(arguments)).thenReturn(response);
		return joinPoint;
	}

	private ProceedingJoinPoint joinPoint(Object[] arguments, RuntimeException exception) throws Throwable {
		ProceedingJoinPoint joinPoint = mock(ProceedingJoinPoint.class);
		Signature signature = mock(Signature.class);
		when(joinPoint.getArgs()).thenReturn(arguments);
		when(joinPoint.getSignature()).thenReturn(signature);
		when(signature.getName()).thenReturn("method");
		when(joinPoint.proceed(arguments)).thenThrow(exception);
		return joinPoint;
	}

}
