package io.github.yaforster.trails.adapter.api.rest.action.model;

import io.github.yaforster.trails.core.test.Action;
import io.github.yaforster.trails.core.test.Position;
import lombok.Getter;
import org.springframework.hateoas.RepresentationModel;

import java.util.List;

@Getter
public class ActionModel extends RepresentationModel<ActionModel> {

	private final Long referenceID;

	private final Long applicationId;

	private final Long stageId;

	private final Action action;

	public ActionModel(Long referenceID, Action action) {
		this(referenceID, null, null, action);
	}

	public ActionModel(Long referenceID, Long applicationId, Long stageId, Action action) {
		this.referenceID = referenceID;
		this.applicationId = applicationId;
		this.stageId = stageId;
		this.action = action;
	}

	public Long getActionID() {
		return action.getActionID();
	}

	public String getLabel() {
		return action.getLabel();
	}

	public List<Long> getNextActions() {
		return action.getNextActions();
	}

	public Position getPosition() {
		return action.getPosition();
	}

}
