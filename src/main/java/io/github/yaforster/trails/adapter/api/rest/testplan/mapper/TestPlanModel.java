package io.github.yaforster.trails.adapter.api.rest.testplan.mapper;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.hateoas.RepresentationModel;
import io.github.yaforster.trails.core.definition.TestPlanGroup;
import java.util.List;

@AllArgsConstructor
@Getter
public class TestPlanModel extends RepresentationModel<TestPlanModel> {

	private Long id;

	private String label;

	private boolean retired;

	private List<TestPlanGroup> groups;

	public TestPlanModel(Long id, String label) {
		this(id, label, false, List.of());
	}

	public TestPlanModel(Long id, String label, boolean retired) {
		this(id, label, retired, List.of());
	}

}
