package io.github.yaforster.trails.adapter.db.testplan.promotion;

import io.github.yaforster.trails.adapter.db.testplan.entity.ActionDetailsEntity;
import io.github.yaforster.trails.adapter.db.testplan.entity.details.TypingDetailsEntity;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
class TypingDetailsCopyVariant extends AbstractActionDetailsCopyVariant<TypingDetailsEntity> {

	TypingDetailsCopyVariant() {
		super(TypingDetailsEntity.class);
	}

	@Override
	protected Optional<ActionDetailsEntity> copyDetails(TypingDetailsEntity details, ActionDetailsCopyContext context) {
		return context.targetElementId(details.getElementID())
			.map(targetElementId -> new TypingDetailsEntity(targetElementId, details.isClearBeforeTyping(),
					details.getDelayAfterClearMillis(), context.copyValueComputation(details.getValueComputation())));
	}

}
