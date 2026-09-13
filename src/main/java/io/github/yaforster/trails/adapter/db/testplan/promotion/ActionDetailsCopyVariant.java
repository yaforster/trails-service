package io.github.yaforster.trails.adapter.db.testplan.promotion;

import io.github.yaforster.trails.adapter.db.testplan.entity.ActionDetailsEntity;

import java.util.Optional;

public interface ActionDetailsCopyVariant {

	Class<? extends ActionDetailsEntity> detailsType();

	Optional<ActionDetailsEntity> copy(ActionDetailsEntity details, ActionDetailsCopyContext context);

}
