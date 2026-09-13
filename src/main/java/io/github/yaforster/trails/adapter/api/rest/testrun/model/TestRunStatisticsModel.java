package io.github.yaforster.trails.adapter.api.rest.testrun.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.hateoas.RepresentationModel;

@AllArgsConstructor
@Getter
public class TestRunStatisticsModel extends RepresentationModel<TestRunStatisticsModel> {

	private final Long applicationId;

	private final Long stageId;

	private final Long testPlanId;

	private final Long totalRuns;

	private final Long successfulRuns;

	private final Long partialSuccessRuns;

	private final Long failedRuns;

}
