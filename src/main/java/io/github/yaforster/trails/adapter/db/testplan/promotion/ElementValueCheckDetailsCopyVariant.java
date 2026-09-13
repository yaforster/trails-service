package io.github.yaforster.trails.adapter.db.testplan.promotion;

import io.github.yaforster.trails.adapter.db.testplan.entity.ActionDetailsEntity;
import io.github.yaforster.trails.adapter.db.testplan.entity.details.ElementValueCheckDetailsEntity;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
class ElementValueCheckDetailsCopyVariant extends AbstractActionDetailsCopyVariant<ElementValueCheckDetailsEntity> {

	ElementValueCheckDetailsCopyVariant() {
		super(ElementValueCheckDetailsEntity.class);
	}

	@Override
	protected Optional<ActionDetailsEntity> copyDetails(ElementValueCheckDetailsEntity details,
			ActionDetailsCopyContext context) {
		return context.targetElementId(details.getElementID())
			.map(targetElementId -> new ElementValueCheckDetailsEntity(targetElementId, details.getValueSource(),
					details.getValueName(), context.copyValueComputation(details.getValueComputation())));
	}

}
