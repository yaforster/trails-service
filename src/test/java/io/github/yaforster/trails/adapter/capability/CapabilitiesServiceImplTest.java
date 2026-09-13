package io.github.yaforster.trails.adapter.capability;

import io.github.yaforster.trails.app.services.CapabilityContribution;
import io.github.yaforster.trails.app.services.CapabilityContributor;
import io.github.yaforster.trails.core.Capability;
import io.github.yaforster.trails.core.CapabilityLabel;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalStateException;

class CapabilitiesServiceImplTest {

	@Test
	void capabilities_shouldUseCanonicalLabelOrder_whenContributorsUseAnotherOrder() {
		CapabilitiesServiceImpl service = new CapabilitiesServiceImpl(
				List.of(() -> List.of(new CapabilityContribution(CapabilityLabel.USER_FEATURES_ACTIVE, "true"),
						new CapabilityContribution(CapabilityLabel.FILE_DOWNLOAD_TIMEOUT_MILLIS, "123"),
						new CapabilityContribution(CapabilityLabel.SUPPORTED_IMAGE_FORMATS, "PNG"),
						new CapabilityContribution(CapabilityLabel.USER_IMAGE_MAX_SIZE_BYTES, "3"),
						new CapabilityContribution(CapabilityLabel.USER_IMAGE_MAX_WIDTH, "2"),
						new CapabilityContribution(CapabilityLabel.USER_IMAGE_MAX_HEIGHT, "1"))));

		assertThat(service.getCapabilities()).containsExactly(
				new Capability(CapabilityLabel.USER_IMAGE_MAX_HEIGHT, "1"),
				new Capability(CapabilityLabel.USER_IMAGE_MAX_WIDTH, "2"),
				new Capability(CapabilityLabel.USER_IMAGE_MAX_SIZE_BYTES, "3"),
				new Capability(CapabilityLabel.SUPPORTED_IMAGE_FORMATS, "PNG"),
				new Capability(CapabilityLabel.FILE_DOWNLOAD_TIMEOUT_MILLIS, "123"),
				new Capability(CapabilityLabel.USER_FEATURES_ACTIVE, "true"));
	}

	@Test
	void construction_shouldFail_whenContributorsOmitALabel() {
		CapabilityContributor incompleteContributor = () -> List
			.of(new CapabilityContribution(CapabilityLabel.USER_FEATURES_ACTIVE, "true"));

		assertThatIllegalStateException().isThrownBy(() -> new CapabilitiesServiceImpl(List.of(incompleteContributor)))
			.withMessageContaining("Missing capability labels");
	}

	@Test
	void construction_shouldFail_whenContributorsDuplicateALabel() {
		CapabilityContributor firstContributor = () -> List
			.of(new CapabilityContribution(CapabilityLabel.USER_FEATURES_ACTIVE, "true"));
		CapabilityContributor secondContributor = () -> List
			.of(new CapabilityContribution(CapabilityLabel.USER_FEATURES_ACTIVE, "false"));

		assertThatIllegalStateException()
			.isThrownBy(() -> new CapabilitiesServiceImpl(List.of(firstContributor, secondContributor)))
			.withMessageContaining("Duplicate capability labels");
	}

}
