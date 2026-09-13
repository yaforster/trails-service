package io.github.yaforster.trails.adapter.db.testplan.promotion;

import io.github.yaforster.trails.adapter.db.testplan.entity.ActionDetailsEntity;
import io.github.yaforster.trails.adapter.db.testplan.entity.details.CheckExistenceActionDetailsEntity;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
class CheckExistenceDetailsCopyVariant extends AbstractActionDetailsCopyVariant<CheckExistenceActionDetailsEntity> {

	CheckExistenceDetailsCopyVariant() {
		super(CheckExistenceActionDetailsEntity.class);
	}

	@Override
	protected Optional<ActionDetailsEntity> copyDetails(CheckExistenceActionDetailsEntity details,
			ActionDetailsCopyContext context) {
		return context.targetElementId(details.getElementID()).map(CheckExistenceActionDetailsEntity::new);
	}

}
