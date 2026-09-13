package io.github.yaforster.trails.core;

public record Capability(CapabilityLabel label, String description, String value) {

	public Capability(CapabilityLabel capabilityLabel, String value) {
		this(capabilityLabel, capabilityLabel.getDescription(), value);
	}
}
