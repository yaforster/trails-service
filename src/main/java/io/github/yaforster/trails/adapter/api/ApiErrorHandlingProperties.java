package io.github.yaforster.trails.adapter.api;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("service.api.error-handling")
public record ApiErrorHandlingProperties(Boolean addStacktraceToResponse) {

	public ApiErrorHandlingProperties {
		addStacktraceToResponse = Boolean.TRUE.equals(addStacktraceToResponse);
	}

}
