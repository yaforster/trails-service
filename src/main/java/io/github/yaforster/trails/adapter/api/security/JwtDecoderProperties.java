package io.github.yaforster.trails.adapter.api.security;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("spring.security.oauth2.resourceserver.jwt")
public record JwtDecoderProperties(String issuerUri, String jwkSetUri) {

}
