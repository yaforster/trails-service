package io.github.yaforster.trails.adapter.api.security;

import jakarta.servlet.http.HttpServletRequest;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aopalliance.intercept.MethodInvocation;
import org.springframework.aop.support.AopUtils;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.context.event.EventListener;
import org.springframework.security.authorization.event.AuthorizationDeniedEvent;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerExecutionChain;
import org.springframework.web.servlet.HandlerMapping;
import org.springframework.web.servlet.mvc.method.RequestMappingInfo;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Slf4j
@Component
@AllArgsConstructor
public class AuthorizationDeniedEventLogger {

	private final ObjectProvider<RequestMappingHandlerMapping> requestMappingHandlerMapping;

	@EventListener
	public void logDeniedAuthorization(AuthorizationDeniedEvent<?> event) {
		Authentication authentication = event.getAuthentication().get();
		String requestedTarget = findRequestedTarget(event.getObject());
		String principal = findPrincipalName(authentication);
		List<String> authorities = findAuthorities(authentication);
		log.warn("Denied access to '{}' for principal '{}' with authorities '{}'", requestedTarget, principal,
				authorities);
	}

	private String findRequestedTarget(Object object) {
		if (object instanceof MethodInvocation methodInvocation) {
			return formatMethod(methodInvocation.getMethod(), methodInvocation.getThis());
		}
		if (object instanceof HttpServletRequest request) {
			return findHandlerMethod(request).map(this::formatHandlerMethod)
				.orElseGet(() -> "request " + request.getMethod() + " " + request.getRequestURI());
		}
		return String.valueOf(object);
	}

	private Optional<HandlerMethod> findHandlerMethod(HttpServletRequest request) {
		Object mappedHandler = request.getAttribute(HandlerMapping.BEST_MATCHING_HANDLER_ATTRIBUTE);
		if (mappedHandler instanceof HandlerMethod handlerMethod) {
			return Optional.of(handlerMethod);
		}
		try {
			RequestMappingHandlerMapping handlerMapping = requestMappingHandlerMapping.getIfAvailable();
			if (handlerMapping == null) {
				return Optional.empty();
			}
			Optional<HandlerMethod> matchingHandlerMethod = findMatchingHandlerMethod(request, handlerMapping);
			if (matchingHandlerMethod.isPresent()) {
				return matchingHandlerMethod;
			}
			HandlerExecutionChain handler = handlerMapping.getHandler(request);
			if (handler != null && handler.getHandler() instanceof HandlerMethod handlerMethod) {
				return Optional.of(handlerMethod);
			}
		}
		catch (Exception exception) {
			log.debug("Could not resolve denied request to a handler method", exception);
		}
		return Optional.empty();
	}

	private Optional<HandlerMethod> findMatchingHandlerMethod(HttpServletRequest request,
			RequestMappingHandlerMapping handlerMapping) {
		return handlerMapping.getHandlerMethods()
			.entrySet()
			.stream()
			.filter(entry -> matches(entry.getKey(), request))
			.map(java.util.Map.Entry::getValue)
			.findFirst();
	}

	private boolean matches(RequestMappingInfo requestMappingInfo, HttpServletRequest request) {
		return requestMappingInfo.getMatchingCondition(request) != null
				|| matchesDirectPath(requestMappingInfo, request);
	}

	private boolean matchesDirectPath(RequestMappingInfo requestMappingInfo, HttpServletRequest request) {
		String requestPath = request.getRequestURI().substring(request.getContextPath().length());
		Set<RequestMethod> configuredMethods = requestMappingInfo.getMethodsCondition().getMethods();
		boolean methodMatches = configuredMethods.isEmpty()
				|| configuredMethods.contains(RequestMethod.valueOf(request.getMethod()));
		return methodMatches && (requestMappingInfo.getPatternValues().contains(requestPath)
				|| requestMappingInfo.toString().contains(requestPath));
	}

	private String formatHandlerMethod(HandlerMethod handlerMethod) {
		return "secured method " + handlerMethod.getBeanType().getSimpleName() + "."
				+ handlerMethod.getMethod().getName();
	}

	private String formatMethod(Method method, Object invocationTarget) {
		if (invocationTarget == null) {
			return "secured method " + method.getDeclaringClass().getSimpleName() + "." + method.getName();
		}
		Method targetMethod = AopUtils.getMostSpecificMethod(method, invocationTarget.getClass());
		return "secured method " + targetMethod.getDeclaringClass().getSimpleName() + "." + targetMethod.getName();
	}

	private String findPrincipalName(Authentication authentication) {
		if (authentication == null || authentication.getName() == null || authentication.getName().isBlank()) {
			return "unknown principal";
		}
		return authentication.getName();
	}

	private List<String> findAuthorities(Authentication authentication) {
		if (authentication == null) {
			return List.of();
		}
		return authentication.getAuthorities().stream().map(GrantedAuthority::getAuthority).sorted().toList();
	}

}
