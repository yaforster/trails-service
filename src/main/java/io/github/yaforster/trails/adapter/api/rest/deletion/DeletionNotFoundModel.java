package io.github.yaforster.trails.adapter.api.rest.deletion;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.hateoas.RepresentationModel;

@AllArgsConstructor
@Getter
public final class DeletionNotFoundModel extends RepresentationModel<DeletionNotFoundModel>
		implements DatabaseDeletionResultModel {

	private final Long id;

	private final String message;

}
