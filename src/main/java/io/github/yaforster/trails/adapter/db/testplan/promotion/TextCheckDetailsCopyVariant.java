package io.github.yaforster.trails.adapter.db.testplan.promotion;

import io.github.yaforster.trails.adapter.db.testplan.entity.ActionDetailsEntity;
import io.github.yaforster.trails.adapter.db.testplan.entity.details.TextCheckDetailsEntity;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
class TextCheckDetailsCopyVariant extends AbstractActionDetailsCopyVariant<TextCheckDetailsEntity> {

	TextCheckDetailsCopyVariant() {
		super(TextCheckDetailsEntity.class);
	}

	@Override
	protected Optional<ActionDetailsEntity> copyDetails(TextCheckDetailsEntity details,
			ActionDetailsCopyContext context) {
		return context.targetElementId(details.getElementID())
			.map(targetElementId -> new TextCheckDetailsEntity(targetElementId,
					context.copyValueComputation(details.getValueComputation())));
	}

}
