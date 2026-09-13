package io.github.yaforster.trails.adapter.db.testplan.promotion;

import io.github.yaforster.trails.adapter.db.testplan.entity.ActionDetailsEntity;
import io.github.yaforster.trails.adapter.db.testplan.entity.details.SelectionDetailsEntity;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
class SelectionDetailsCopyVariant extends AbstractActionDetailsCopyVariant<SelectionDetailsEntity> {

	SelectionDetailsCopyVariant() {
		super(SelectionDetailsEntity.class);
	}

	@Override
	protected Optional<ActionDetailsEntity> copyDetails(SelectionDetailsEntity details,
			ActionDetailsCopyContext context) {
		return context.targetElementId(details.getElementID())
			.map(targetElementId -> new SelectionDetailsEntity(targetElementId,
					context.copyValueComputation(details.getValueComputation())));
	}

}
