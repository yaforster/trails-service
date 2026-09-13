package io.github.yaforster.trails.adapter.db.testplan.promotion;

import io.github.yaforster.trails.adapter.db.testplan.entity.ActionDetailsEntity;
import io.github.yaforster.trails.adapter.db.testplan.entity.details.SessionStorageSearchDetailsEntity;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
class SessionStorageSearchDetailsCopyVariant
		extends AbstractActionDetailsCopyVariant<SessionStorageSearchDetailsEntity> {

	SessionStorageSearchDetailsCopyVariant() {
		super(SessionStorageSearchDetailsEntity.class);
	}

	@Override
	protected Optional<ActionDetailsEntity> copyDetails(SessionStorageSearchDetailsEntity details,
			ActionDetailsCopyContext context) {
		return Optional.of(new SessionStorageSearchDetailsEntity(details.getStorageKey(),
				context.copyValueComputation(details.getValueComputation())));
	}

}
