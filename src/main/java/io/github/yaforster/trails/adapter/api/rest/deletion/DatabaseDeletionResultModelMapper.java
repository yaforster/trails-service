package io.github.yaforster.trails.adapter.api.rest.deletion;

import io.github.yaforster.trails.core.deletion.DatabaseDeletionResult;
import io.github.yaforster.trails.core.deletion.DeletionFailure;
import io.github.yaforster.trails.core.deletion.DeletionNotFound;
import io.github.yaforster.trails.core.deletion.DeletionSuccess;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

@AllArgsConstructor
@Component
public class DatabaseDeletionResultModelMapper {

	public DatabaseDeletionResultModel toModel(DatabaseDeletionResult result) {
		return switch (result) {
			case DeletionSuccess success -> new DeletionSuccessModel(success.id(),
					String.format("Successfully deleted database entry with ID %s", success.id()));
			case DeletionNotFound notFound -> new DeletionNotFoundModel(notFound.id(),
					String.format("No database entry was found with the given ID %d", notFound.id()));
			case DeletionFailure failure -> new DeletionFailureModel(failure.id(), failure.message(), failure.error());
		};
	}

}
