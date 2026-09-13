package io.github.yaforster.trails.adapter.api.rest.artifact.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.hateoas.RepresentationModel;

@AllArgsConstructor
@Getter
public class ArtifactModel extends RepresentationModel<ArtifactModel> {

	private final Long id;

	private final Type type;

	private final String filename;

	public enum Type {

		SCREENSHOT, PDF, LOG, OTHER

	}

}
