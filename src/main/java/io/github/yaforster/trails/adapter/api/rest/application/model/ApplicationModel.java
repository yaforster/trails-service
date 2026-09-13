package io.github.yaforster.trails.adapter.api.rest.application.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.hateoas.RepresentationModel;
import org.springframework.hateoas.server.core.Relation;

@AllArgsConstructor
@Getter
@Relation(collectionRelation = "items", itemRelation = "application")
public class ApplicationModel extends RepresentationModel<ApplicationModel> {

	private final Long id;

	private final String label;

	private final boolean retired;

	public ApplicationModel(Long id, String label) {
		this(id, label, false);
	}

}
