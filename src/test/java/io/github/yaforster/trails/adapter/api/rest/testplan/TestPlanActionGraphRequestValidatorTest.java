package io.github.yaforster.trails.adapter.api.rest.testplan;

import io.github.yaforster.trails.adapter.api.APIRequestValidationException;
import io.github.yaforster.trails.adapter.api.rest.model.ActionDefinitionDTO;
import io.github.yaforster.trails.adapter.api.rest.model.TestPlanDefinitionDTO;
import io.github.yaforster.trails.core.ValidationViolation;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.junit.jupiter.api.Assertions.assertThrows;

class TestPlanActionGraphRequestValidatorTest {

	@Test
	void validate_shouldRejectEmptyActionGraph() {
		List<ValidationViolation> violations = violationsFor(new TestPlanDefinitionDTO().testSteps(List.of()));

		assertThat(violations.getFirst().code()).isEqualTo("TEST_PLAN_TEST_STEPS_EMPTY");
	}

	@Test
	void validate_shouldRejectDuplicateActionReferences() {
		List<ValidationViolation> violations = violationsFor(definition(action(1L), action(1L)));

		assertThat(violations.getFirst().code()).isEqualTo("TEST_PLAN_ACTION_REFERENCE_ID_DUPLICATE");
	}

	@Test
	void validate_shouldRejectActionWithoutReference() {
		List<ValidationViolation> violations = violationsFor(definition(new ActionDefinitionDTO()));

		assertThat(violations.getFirst().code()).isEqualTo("TEST_PLAN_ACTION_REFERENCE_ID_NULL");
	}

	@Test
	void validate_shouldRejectUnknownNextActionReference() {
		List<ValidationViolation> violations = violationsFor(definition(action(1L, 2L)));

		assertThat(violations.getFirst().code()).isEqualTo("TEST_PLAN_NEXT_ACTION_REFERENCE_ID_UNKNOWN");
	}

	@Test
	void validate_shouldRejectNullNextActionReference() {
		ActionDefinitionDTO action = new ActionDefinitionDTO().referenceID(1L).nextActions(Arrays.asList((Long) null));
		List<ValidationViolation> violations = violationsFor(definition(action));

		assertThat(violations.getFirst().code()).isEqualTo("TEST_PLAN_NEXT_ACTION_REFERENCE_ID_NULL");
	}

	@Test
	void validate_shouldRejectDuplicateNextActionReference() {
		List<ValidationViolation> violations = violationsFor(definition(action(1L, 2L, 2L), action(2L)));

		assertThat(violations.getFirst().code()).isEqualTo("TEST_PLAN_NEXT_ACTION_REFERENCE_ID_DUPLICATE");
	}

	@Test
	void validate_shouldRejectCyclicActionGraph() {
		List<ValidationViolation> violations = violationsFor(definition(action(1L, 2L), action(2L, 1L)));

		assertThat(violations.getFirst().code()).isEqualTo("TEST_PLAN_ACTION_GRAPH_CYCLIC");
	}

	@Test
	void validate_shouldRejectActionGraphWithMultipleStartingActions() {
		List<ValidationViolation> violations = violationsFor(definition(action(1L), action(2L)));

		assertThat(violations.getFirst().code()).isEqualTo("TEST_PLAN_ACTION_GRAPH_STARTING_ACTION_COUNT_INVALID");
	}

	@Test
	void validate_shouldAcceptAcyclicActionGraphWithOneStartingAction() {
		assertThatCode(() -> new TestPlanActionGraphRequestValidator()
			.validate(definition(action(1L, 2L, 3L), action(2L, 4L), action(3L, 4L), action(4L))))
			.doesNotThrowAnyException();
	}

	private static TestPlanDefinitionDTO definition(ActionDefinitionDTO... actions) {
		return new TestPlanDefinitionDTO().testSteps(List.of(actions));
	}

	private static ActionDefinitionDTO action(Long referenceId, Long... nextActionReferenceIds) {
		return new ActionDefinitionDTO().referenceID(referenceId).nextActions(List.of(nextActionReferenceIds));
	}

	private static List<ValidationViolation> violationsFor(TestPlanDefinitionDTO definition) {
		APIRequestValidationException exception = assertThrows(APIRequestValidationException.class,
				() -> new TestPlanActionGraphRequestValidator().validate(definition));
		return exception.getValidationViolations();
	}

}
