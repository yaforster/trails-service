package io.github.yaforster.trails.adapter.db.testplan.promotion;

import io.github.yaforster.trails.adapter.db.testplan.entity.ActionDetailsEntity;
import io.github.yaforster.trails.adapter.db.testplan.entity.details.LocalStorageSearchDetailsEntity;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
class LocalStorageSearchDetailsCopyVariant extends AbstractActionDetailsCopyVariant<LocalStorageSearchDetailsEntity> {

	LocalStorageSearchDetailsCopyVariant() {
		super(LocalStorageSearchDetailsEntity.class);
	}

	@Override
	protected Optional<ActionDetailsEntity> copyDetails(LocalStorageSearchDetailsEntity details,
			ActionDetailsCopyContext context) {
		return Optional.of(new LocalStorageSearchDetailsEntity(details.getStorageKey(),
				context.copyValueComputation(details.getValueComputation())));
	}

}
