package io.github.yaforster.trails.adapter.api.rest.deletion;

public sealed interface DatabaseDeletionResultModel
		permits DeletionSuccessModel, DeletionNotFoundModel, DeletionFailureModel {

}
