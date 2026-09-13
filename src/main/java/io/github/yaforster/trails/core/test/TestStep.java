package io.github.yaforster.trails.core.test;

import org.apache.commons.collections4.CollectionUtils;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public record TestStep(Action action, List<TestStep> nextTestSteps) {

	/**
	 * Retrieves all possible paths from a given TestStep.
	 * @param step The TestStep from which to retrieve the paths.
	 * @return A list of lists, where each inner list represents a path of TestSteps.
	 */
	private List<List<TestStep>> getPathsFrom(TestStep step) {
		List<List<TestStep>> paths = new ArrayList<>();

		if (CollectionUtils.isEmpty(step.nextTestSteps)) {
			List<TestStep> nodeList = new LinkedList<>();
			nodeList.add(step);
			paths.add(nodeList);
		}
		else {
			paths.addAll(step.nextTestSteps.stream()
				.flatMap(node -> getPathsFrom(node).stream())
				.peek(nodeList -> nodeList.addFirst(step))
				.toList());
		}
		return paths;
	}

	public List<List<TestStep>> getTestPathsFromThisStep() {
		return getPathsFrom(this);
	}
}
