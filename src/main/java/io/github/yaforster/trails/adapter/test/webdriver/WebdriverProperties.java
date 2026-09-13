package io.github.yaforster.trails.adapter.test.webdriver;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("service.webdrivers.general")
public record WebdriverProperties(String gridUrl, FileDownload fileDownload, String browserBaseUrl) {

	public record FileDownload(int timeoutMillis) {

	}

}
