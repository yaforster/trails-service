package io.github.yaforster.trails.adapter.api.rest.testset.model;

import io.github.yaforster.trails.core.test.Browser;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.hateoas.RepresentationModel;

import java.util.Optional;

@AllArgsConstructor
@Getter
public class TestSetResultModel extends RepresentationModel<TestSetResultModel> {

	private final Long id;

	private final Integer totalRunTime;

	private final Browser browserToRunIn;

	private final String testPlanLabel;

	private final Optional<TestCaseResultModel> testCaseResult;

}
