package io.github.yaforster.trails.adapter.api.rest;

import io.github.yaforster.trails.adapter.api.APIExceptionMapper;
import io.github.yaforster.trails.adapter.api.ValidationErrorDTOMapper;
import io.github.yaforster.trails.adapter.api.rest.aspect.HATEOASAPIAspect;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.Signature;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class HATEOASAPIAspectTest {

	@Test
	void wrapHateoasEndpoints_shouldReturnEndpointResponse_whenEndpointSucceeds() throws Throwable {
		ResponseEntity<String> expected = ResponseEntity.ok("created");
		ProceedingJoinPoint joinPoint = joinPoint(new Object[] { "body" }, expected);
		HATEOASAPIAspect aspect = new HATEOASAPIAspect(mock(APIExceptionMapper.class));

		Object response = aspect.wrapHateoasEndpoints(joinPoint);

		assertSame(expected, response);
	}

	@Test
	void wrapHateoasEndpoints_shouldProceedWithOriginalArguments_whenEndpointSucceeds() throws Throwable {
		Object[] arguments = new Object[] { "body" };
		ProceedingJoinPoint joinPoint = joinPoint(arguments, ResponseEntity.ok().build());
		HATEOASAPIAspect aspect = new HATEOASAPIAspect(mock(APIExceptionMapper.class));

		aspect.wrapHateoasEndpoints(joinPoint);

		verify(joinPoint).proceed(arguments);
	}

	@Test
	void wrapHateoasEndpoints_shouldReturnMappedResponse_whenEndpointThrowsException() throws Throwable {
		ProceedingJoinPoint joinPoint = mock(ProceedingJoinPoint.class);
		RuntimeException exception = new RuntimeException("boom");
		APIExceptionMapper exceptionMapper = new APIExceptionMapper(Mappers.getMapper(ValidationErrorDTOMapper.class));
		when(joinPoint.getArgs()).thenReturn(new Object[0]);
		when(joinPoint.proceed(new Object[0])).thenThrow(exception);
		HATEOASAPIAspect aspect = new HATEOASAPIAspect(exceptionMapper);

		ResponseEntity<?> response = (ResponseEntity<?>) aspect.wrapHateoasEndpoints(joinPoint);

		assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
	}

	@Test
	void wrapHateoasEndpoints_shouldDelegateExceptionToMapper_whenEndpointThrowsException() throws Throwable {
		ProceedingJoinPoint joinPoint = mock(ProceedingJoinPoint.class);
		RuntimeException exception = new RuntimeException("boom");
		APIExceptionMapper exceptionMapper = mock(APIExceptionMapper.class);
		when(joinPoint.getArgs()).thenReturn(new Object[0]);
		when(joinPoint.proceed(new Object[0])).thenThrow(exception);
		when(exceptionMapper.toErrorResponse(exception)).thenReturn(ResponseEntity.internalServerError().build());
		HATEOASAPIAspect aspect = new HATEOASAPIAspect(exceptionMapper);

		aspect.wrapHateoasEndpoints(joinPoint);

		verify(exceptionMapper).toErrorResponse(exception);
	}

	@Test
	void hateoasEndpoints_shouldBeCallableAsAspectPointcut() {
		HATEOASAPIAspect aspect = new HATEOASAPIAspect(mock(APIExceptionMapper.class));

		aspect.hateoasEndpoints();
	}

	@Test
	void responseEntityHateoasEndpoints_shouldBeCallableAsAspectPointcut() {
		HATEOASAPIAspect aspect = new HATEOASAPIAspect(mock(APIExceptionMapper.class));

		aspect.responseEntityHateoasEndpoints();
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

}
