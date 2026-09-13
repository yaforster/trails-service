package io.github.yaforster.trails.adapter.api.rest.deletion;

import io.github.yaforster.trails.adapter.api.rest.deletion.DatabaseDeletionResultMapper;
import io.github.yaforster.trails.adapter.api.rest.deletion.DeletionFailureModel;
import io.github.yaforster.trails.adapter.api.rest.deletion.DeletionNotFoundModel;
import io.github.yaforster.trails.adapter.api.rest.deletion.DeletionSuccessModel;
import io.github.yaforster.trails.adapter.api.rest.model.DatabaseDeletionResultDTO;
import io.github.yaforster.trails.core.deletion.ErrorDetails;
import org.junit.jupiter.api.Test;
import org.springframework.hateoas.Link;

import static org.junit.jupiter.api.Assertions.*;

class DatabaseDeletionResultMapperTest {

	private final DatabaseDeletionResultMapper mapper = new DatabaseDeletionResultMapper();

	@Test
	void mapsDatabaseDeletionResultModelToDto() {
		DeletionSuccessModel successModel = new DeletionSuccessModel(21L, "ok");
		successModel.add(Link.of("/deletions/21", "self"));
		DeletionNotFoundModel notFoundModel = new DeletionNotFoundModel(22L, "missing");
		notFoundModel.add(Link.of("/deletions/22", "self"));
		DeletionFailureModel failureModel = new DeletionFailureModel(24L, "failure",
				new ErrorDetails("RuntimeException", "x", "trace"));
		failureModel.add(Link.of("/deletions/24", "self"));

		DatabaseDeletionResultDTO success = mapper.toDTO(successModel);
		DatabaseDeletionResultDTO notFound = mapper.toDTO(notFoundModel);
		DatabaseDeletionResultDTO failure = mapper.toDTO(failureModel);

		assertTrue(success.getDeletionResult());
		assertFalse(notFound.getDeletionResult());
		assertFalse(failure.getDeletionResult());
		assertEquals("/deletions/21", success.getLinks().get("self").getHref());
		assertEquals("/deletions/22", notFound.getLinks().get("self").getHref());
		assertEquals("/deletions/24", failure.getLinks().get("self").getHref());
	}

}
