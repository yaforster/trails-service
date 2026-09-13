package io.github.yaforster.trails.adapter.api.rest.deletion;

import io.github.yaforster.trails.core.deletion.ErrorDetails;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.hateoas.RepresentationModel;

@AllArgsConstructor
@Getter
public final class DeletionFailureModel extends RepresentationModel<DeletionFailureModel>
		implements DatabaseDeletionResultModel {

	private final Long id;

	private final String message;

	private final ErrorDetails error;

}
