package io.github.yaforster.trails.adapter.db.testplan.promotion;

import io.github.yaforster.trails.adapter.db.testplan.entity.ActionDetailsEntity;
import io.github.yaforster.trails.adapter.db.testplan.entity.details.CookieSearchDetailsEntity;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
class CookieSearchDetailsCopyVariant extends AbstractActionDetailsCopyVariant<CookieSearchDetailsEntity> {

	CookieSearchDetailsCopyVariant() {
		super(CookieSearchDetailsEntity.class);
	}

	@Override
	protected Optional<ActionDetailsEntity> copyDetails(CookieSearchDetailsEntity details,
			ActionDetailsCopyContext context) {
		return Optional.of(new CookieSearchDetailsEntity(details.getCookieName(),
				context.copyValueComputation(details.getValueComputation())));
	}

}
