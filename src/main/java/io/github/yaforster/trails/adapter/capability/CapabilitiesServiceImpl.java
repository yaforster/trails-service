package io.github.yaforster.trails.adapter.capability;

import io.github.yaforster.trails.app.services.CapabilitiesService;
import io.github.yaforster.trails.app.services.CapabilityContribution;
import io.github.yaforster.trails.app.services.CapabilityContributor;
import io.github.yaforster.trails.core.Capability;
import io.github.yaforster.trails.core.CapabilityLabel;
import lombok.Getter;

import java.util.Arrays;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;

@Getter
public class CapabilitiesServiceImpl implements CapabilitiesService {

	private final List<Capability> capabilities;

	public CapabilitiesServiceImpl(List<CapabilityContributor> contributors) {
		Map<CapabilityLabel, String> values = collectValues(contributors);
		this.capabilities = Arrays.stream(CapabilityLabel.values())
			.map(label -> new Capability(label, values.get(label)))
			.toList();
	}

	private Map<CapabilityLabel, String> collectValues(List<CapabilityContributor> contributors) {
		Map<CapabilityLabel, String> values = new EnumMap<>(CapabilityLabel.class);
		EnumSet<CapabilityLabel> duplicateLabels = EnumSet.noneOf(CapabilityLabel.class);

		for (CapabilityContributor contributor : contributors) {
			for (CapabilityContribution contribution : contributor.contribute()) {
				if (values.putIfAbsent(contribution.label(), contribution.value()) != null) {
					duplicateLabels.add(contribution.label());
				}
			}
		}

		if (!duplicateLabels.isEmpty()) {
			throw new IllegalStateException("Duplicate capability labels: " + duplicateLabels);
		}

		EnumSet<CapabilityLabel> missingLabels = EnumSet.allOf(CapabilityLabel.class);
		missingLabels.removeAll(values.keySet());
		if (!missingLabels.isEmpty()) {
			throw new IllegalStateException("Missing capability labels: " + missingLabels);
		}
		return values;
	}

}
