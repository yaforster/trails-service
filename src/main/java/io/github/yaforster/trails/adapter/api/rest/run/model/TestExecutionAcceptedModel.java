package io.github.yaforster.trails.adapter.api.rest.run.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.hateoas.RepresentationModel;

import java.util.UUID;

@AllArgsConstructor
@Getter
public class TestExecutionAcceptedModel extends RepresentationModel<TestExecutionAcceptedModel> {

	private final UUID executionId;

	private final Status status;

	private final String message;

	public enum Status {

		ACCEPTED

	}

}
