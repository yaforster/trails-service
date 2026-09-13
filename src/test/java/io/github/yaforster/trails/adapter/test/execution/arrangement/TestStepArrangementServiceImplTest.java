package io.github.yaforster.trails.adapter.test.execution.arrangement;

import io.github.yaforster.trails.core.test.Action;
import io.github.yaforster.trails.core.test.TestStep;
import io.github.yaforster.trails.core.test.action.element.ClickAction;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class TestStepArrangementServiceImplTest {

	@Test
	void shouldBuildEveryPath_whenTheActionGraphBranches() {
		TestStep testStep = new TestStepArrangementServiceImpl()
			.getTestStepChain(List.of(action(1L, 2L, 3L), action(2L, 4L), action(3L, 4L), action(4L)));

		assertThat(testStep.getTestPathsFromThisStep())
			.extracting(path -> path.stream().map(step -> step.action().getActionID()).toList())
			.containsExactlyInAnyOrder(List.of(1L, 2L, 4L), List.of(1L, 3L, 4L));
	}

	@Test
	void shouldTreatAbsentNextActionsAsNoFollowingActions() {
		TestStep testStep = new TestStepArrangementServiceImpl()
			.getTestStepChain(List.of(actionWithoutNextActions(1L)));

		assertThat(testStep.nextTestSteps()).isEmpty();
	}

	private static Action action(Long actionId, Long... nextActionIds) {
		return ClickAction.builder().actionID(actionId).nextActions(List.of(nextActionIds)).build();
	}

	private static Action actionWithoutNextActions(Long actionId) {
		return ClickAction.builder().actionID(actionId).nextActions(null).build();
	}

}
