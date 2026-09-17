package io.github.yaforster.trails.adapter.api.rest.actionresult.model;

import io.github.yaforster.trails.core.persisted.ActionResultType;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.hateoas.RepresentationModel;

@AllArgsConstructor
@Getter
public class ActionResultModel extends RepresentationModel<ActionResultModel> {

	private final Long id;

	private final Long actionID;

	private final String label;

	private final String resultMessage;

	private final ActionResultType resultType;

}
