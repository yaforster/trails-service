package io.github.yaforster.trails.adapter.api.rest.run;

import io.github.yaforster.trails.adapter.api.rest.hateoas.HATEOASMapper;
import io.github.yaforster.trails.adapter.api.rest.model.TestExecutionAcceptedDTO;
import io.github.yaforster.trails.adapter.api.rest.model.TestExecutionStatusDTO;
import io.github.yaforster.trails.adapter.api.rest.run.model.TestExecutionAcceptedModel;
import org.springframework.stereotype.Component;

import java.util.UUID;

import static org.springframework.hateoas.server.core.DummyInvocationUtils.methodOn;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;

@Component
public class TestExecutionHATEOASFacade implements HATEOASMapper {

	public TestExecutionAcceptedDTO toAcceptedDTO(UUID executionId) {
		TestExecutionAcceptedModel model = new TestExecutionAcceptedModel(executionId,
				TestExecutionAcceptedModel.Status.ACCEPTED, "Test execution accepted");
		model.add(linkTo(methodOn(TestExecutionController.class).streamTestExecutionEvents(executionId))
			.withRel("events"));
		return toAcceptedDTO(model);
	}

	private TestExecutionAcceptedDTO toAcceptedDTO(TestExecutionAcceptedModel model) {
		return new TestExecutionAcceptedDTO().executionId(model.getExecutionId())
			.status(toDTO(model.getStatus()))
			.message(model.getMessage())
			.links(mapLinks(model.getLinks()));
	}

	private TestExecutionStatusDTO toDTO(TestExecutionAcceptedModel.Status status) {
		return switch (status) {
			case ACCEPTED -> TestExecutionStatusDTO.ACCEPTED;
		};
	}

}
