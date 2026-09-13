package io.github.yaforster.trails.adapter.api.rest.deletion;

import io.github.yaforster.trails.adapter.api.rest.common.mapper.PagedContentMapper;
import io.github.yaforster.trails.adapter.api.rest.model.DatabaseDeletionResultDTO;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@AllArgsConstructor
public class DatabaseDeletionResultMapper extends PagedContentMapper {

	public DatabaseDeletionResultDTO toDTO(DatabaseDeletionResultModel databaseDeletionResult) {
		DatabaseDeletionResultDTO dto = new DatabaseDeletionResultDTO();
		switch (databaseDeletionResult) {
			case DeletionFailureModel deletionFailure -> {
				dto.setMessage(deletionFailure.getMessage());
				dto.setDeletionResult(false);
				dto.setDeletionRequestedForID(deletionFailure.getId());
				dto.setLinks(mapLinks(deletionFailure.getLinks()));
			}
			case DeletionNotFoundModel deletionNotFound -> {
				dto.setMessage("No database entry with the given values was found.");
				dto.setDeletionResult(false);
				dto.setDeletionRequestedForID(deletionNotFound.getId());
				dto.setLinks(mapLinks(deletionNotFound.getLinks()));
			}
			case DeletionSuccessModel deletionSuccess -> {
				dto.setMessage("Database entry was successfully deleted.");
				dto.setDeletionResult(true);
				dto.setDeletionRequestedForID(deletionSuccess.getId());
				dto.setLinks(mapLinks(deletionSuccess.getLinks()));
			}
		}
		return dto;
	}

}
