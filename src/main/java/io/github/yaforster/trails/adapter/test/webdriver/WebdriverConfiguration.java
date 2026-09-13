package io.github.yaforster.trails.adapter.test.webdriver;

import io.github.yaforster.trails.app.services.CapabilityContribution;
import io.github.yaforster.trails.app.services.CapabilityContributor;
import io.github.yaforster.trails.core.CapabilityLabel;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
@EnableConfigurationProperties(WebdriverProperties.class)
public class WebdriverConfiguration {

	@Bean
	CapabilityContributor fileDownloadCapabilityContributor(WebdriverProperties properties) {
		return () -> List.of(new CapabilityContribution(CapabilityLabel.FILE_DOWNLOAD_TIMEOUT_MILLIS,
				String.valueOf(properties.fileDownload().timeoutMillis())));
	}

}
