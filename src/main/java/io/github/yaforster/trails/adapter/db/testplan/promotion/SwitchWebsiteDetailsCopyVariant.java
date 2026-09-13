package io.github.yaforster.trails.adapter.db.testplan.promotion;

import io.github.yaforster.trails.adapter.db.testplan.entity.ActionDetailsEntity;
import io.github.yaforster.trails.adapter.db.testplan.entity.details.SwitchWebsiteDetailsEntity;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
class SwitchWebsiteDetailsCopyVariant extends AbstractActionDetailsCopyVariant<SwitchWebsiteDetailsEntity> {

	SwitchWebsiteDetailsCopyVariant() {
		super(SwitchWebsiteDetailsEntity.class);
	}

	@Override
	protected Optional<ActionDetailsEntity> copyDetails(SwitchWebsiteDetailsEntity details,
			ActionDetailsCopyContext context) {
		return Optional.of(new SwitchWebsiteDetailsEntity(context.copyValueComputation(details.getValueComputation())));
	}

}
