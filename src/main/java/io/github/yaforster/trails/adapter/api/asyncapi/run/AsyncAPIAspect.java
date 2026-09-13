package io.github.yaforster.trails.adapter.api.asyncapi.run;

import io.github.yaforster.trails.adapter.api.rest.aspect.TrailsAPIAspect;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.stereotype.Component;

@Aspect
@Component
public class AsyncAPIAspect extends TrailsAPIAspect {

	private final AsyncAPIExceptionMapper exceptionMapper;

	public AsyncAPIAspect(AsyncAPIExceptionMapper exceptionMapper) {
		this.exceptionMapper = exceptionMapper;
	}

	@Pointcut("execution(* io.github.yaforster.trails.adapter.api.asyncapi.run.TestExecutionEventListener.receive*(..))")
	public void asyncConsumers() {
	}

	@Pointcut("execution(* io.github.yaforster.trails.adapter.api.asyncapi.run.TestExecutionEventPublisherImpl.publish*(..))")
	public void asyncPublishers() {
	}

	@Around("asyncConsumers() || asyncPublishers()")
	public Object wrapAsyncAPI(ProceedingJoinPoint joinPoint) throws Throwable {
		try {
			return processAPIRequest(joinPoint);
		}
		catch (Exception exception) {
			if (exceptionMapper.toFailedEvent(joinPoint.getArgs(), exception)) {
				return null;
			}
			throw exception;
		}
	}

}
