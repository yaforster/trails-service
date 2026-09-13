package io.github.yaforster.trails.adapter.api.rest.testrun.model;

import io.github.yaforster.trails.core.data.ResultIndicator;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.hateoas.RepresentationModel;

import java.time.OffsetDateTime;

@AllArgsConstructor
@Getter
public class TestRunResultModel extends RepresentationModel<TestRunResultModel> {

	private final Long id;

	private final OffsetDateTime timestamp;

	private final ResultIndicator indicator;

	private final Long applicationId;

	private final Long stageId;

	private final String label;

}
