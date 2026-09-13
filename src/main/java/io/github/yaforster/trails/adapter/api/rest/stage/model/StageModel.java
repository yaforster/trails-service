package io.github.yaforster.trails.adapter.api.rest.stage.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.hateoas.RepresentationModel;

@AllArgsConstructor
@Getter
public class StageModel extends RepresentationModel<StageModel> {

	private final Long id;

	private final String label;

	private final String url;

	private final boolean retired;

	public StageModel(Long id, String label, String url) {
		this(id, label, url, false);
	}

}
