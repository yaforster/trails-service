package io.github.yaforster.trails.adapter.api.rest.hateoas;

import io.github.yaforster.trails.adapter.api.rest.deletion.DatabaseDeletionResultMapper;
import io.github.yaforster.trails.adapter.api.rest.deletion.DatabaseDeletionResultModel;
import io.github.yaforster.trails.adapter.api.rest.deletion.DatabaseDeletionResultModelMapper;
import io.github.yaforster.trails.adapter.api.rest.deletion.DeletionNotFoundModel;
import io.github.yaforster.trails.adapter.api.rest.model.DatabaseDeletionResultDTO;
import io.github.yaforster.trails.core.deletion.DatabaseDeletionResult;
import io.github.yaforster.trails.core.deletion.DeletionFailure;
import io.github.yaforster.trails.core.deletion.DeletionNotFound;
import io.github.yaforster.trails.core.deletion.DeletionSuccess;
import org.springframework.http.HttpStatus;

import java.util.function.Consumer;

public abstract class HATEOASFacade implements HATEOASMapper {

	private final DatabaseDeletionResultMapper databaseDeletionResultMapper;

	private final DatabaseDeletionResultModelMapper databaseDeletionResultModelMapper;

	protected HATEOASFacade(DatabaseDeletionResultMapper databaseDeletionResultMapper,
			DatabaseDeletionResultModelMapper databaseDeletionResultModelMapper) {
		this.databaseDeletionResultMapper = databaseDeletionResultMapper;
		this.databaseDeletionResultModelMapper = databaseDeletionResultModelMapper;
	}

	public HttpStatus toHTTPStatus(DatabaseDeletionResult deletionResult) {
		return switch (deletionResult) {
			case DeletionFailure failure when failure.error() == null -> HttpStatus.CONFLICT;
			case DeletionFailure _ -> HttpStatus.INTERNAL_SERVER_ERROR;
			case DeletionNotFound _ -> HttpStatus.NOT_FOUND;
			case DeletionSuccess _ -> HttpStatus.OK;
		};
	}

	protected DatabaseDeletionResultDTO toDeletionDTO(DatabaseDeletionResult deletionResult,
			Consumer<DeletionNotFoundModel> deletionNotFoundModelCustomizer) {
		DatabaseDeletionResultModel model = databaseDeletionResultModelMapper.toModel(deletionResult);
		if (model instanceof DeletionNotFoundModel deletionNotFoundModel) {
			deletionNotFoundModelCustomizer.accept(deletionNotFoundModel);
		}
		return databaseDeletionResultMapper.toDTO(model);
	}

}
