package io.github.yaforster.trails.adapter.api.rest.testplan.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.hateoas.RepresentationModel;
import io.github.yaforster.trails.core.definition.TestPlanGroup;
import java.util.List;

@AllArgsConstructor
@Getter
public class PersistedTestPlanModel extends RepresentationModel<PersistedTestPlanModel> {

	private Long id;

	private Long applicationID;

	private Long stageID;

	private String label;

	private boolean retired;

	private List<TestPlanGroup> groups;

	public PersistedTestPlanModel(Long id, Long applicationID, Long stageID, String label) {
		this(id, applicationID, stageID, label, false, List.of());
	}

	public PersistedTestPlanModel(Long id, Long applicationID, Long stageID, String label, boolean retired) {
		this(id, applicationID, stageID, label, retired, List.of());
	}

}
