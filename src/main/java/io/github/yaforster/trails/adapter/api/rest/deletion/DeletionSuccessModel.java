package io.github.yaforster.trails.adapter.api.rest.deletion;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.hateoas.RepresentationModel;

@AllArgsConstructor
@Getter
public final class DeletionSuccessModel extends RepresentationModel<DeletionSuccessModel>
		implements DatabaseDeletionResultModel {

	private final Long id;

	private final String message;

}
