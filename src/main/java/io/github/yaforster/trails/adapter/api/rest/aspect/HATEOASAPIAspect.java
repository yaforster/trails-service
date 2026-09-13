package io.github.yaforster.trails.adapter.api.rest.aspect;

import io.github.yaforster.trails.adapter.api.APIExceptionMapper;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.stereotype.Component;

@Aspect
@Component
public class HATEOASAPIAspect extends TrailsAPIAspect {

	private final APIExceptionMapper exceptionMapper;

	public HATEOASAPIAspect(APIExceptionMapper exceptionMapper) {
		this.exceptionMapper = exceptionMapper;
	}

	@Pointcut("within(io.github.yaforster.trails.adapter.api.rest..*) && @within(org.springframework.web.bind.annotation.RestController)")
	public void hateoasEndpoints() {
	}

	@Pointcut("hateoasEndpoints() && execution(org.springframework.http.ResponseEntity *(..))")
	public void responseEntityHateoasEndpoints() {
	}

	@Around("responseEntityHateoasEndpoints()")
	public Object wrapHateoasEndpoints(ProceedingJoinPoint joinPoint) throws Throwable {
		try {
			return processAPIRequest(joinPoint);
		}
		catch (Exception exception) {
			return exceptionMapper.toErrorResponse(exception);
		}
	}

}
