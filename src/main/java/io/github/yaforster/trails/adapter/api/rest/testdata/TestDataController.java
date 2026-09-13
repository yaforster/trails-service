package io.github.yaforster.trails.adapter.api.rest.testdata;

import io.github.yaforster.trails.adapter.api.rest.model.DatabaseDeletionResultDTO;
import io.github.yaforster.trails.adapter.api.rest.model.PagedTestDataSetDTO;
import io.github.yaforster.trails.adapter.api.rest.model.TestDataSetDTO;
import io.github.yaforster.trails.adapter.api.rest.model.TestDataSetDefinitionDTO;
import io.github.yaforster.trails.app.services.TestDataDatabaseService;
import io.github.yaforster.trails.core.PagedResult;
import io.github.yaforster.trails.core.data.TestDataSet;
import io.github.yaforster.trails.core.definition.TestDataSetDefinition;
import io.github.yaforster.trails.core.deletion.DatabaseDeletionResult;
import lombok.AllArgsConstructor;
import org.jspecify.annotations.Nullable;
import org.openapitools.api.TestDataApi;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.RestController;

@AllArgsConstructor
@RestController
public class TestDataController implements TestDataApi {

	private final TestDataDatabaseService databaseService;

	private final TestDataHATEOASFacade hateoasFacade;

	private final TestDataControllerValidator validator;

	@Override
	@PreAuthorize("@resourceAuthorization.canAccessTestData(authentication)")
	public ResponseEntity<TestDataSetDTO> createNewTestData(@Nullable TestDataSetDefinitionDTO dto) {
		validator.validateCreateNewTestData(dto);
		TestDataSetDefinition definition = hateoasFacade.toDomain(dto);
		TestDataSet stored = databaseService.storeTestData(definition);
		return ResponseEntity.created(hateoasFacade.location(stored)).body(hateoasFacade.toDTO(stored));
	}

	@Override
	@PreAuthorize("@resourceAuthorization.canAccessTestData(authentication)")
	public ResponseEntity<DatabaseDeletionResultDTO> deleteTestData(Long testDataId) {
		validator.validateDeleteTestData(testDataId);
		DatabaseDeletionResult deletionResult = databaseService.deleteTestData(testDataId);
		DatabaseDeletionResultDTO dto = hateoasFacade.toDTO(deletionResult);
		return ResponseEntity.status(hateoasFacade.toHTTPStatus(deletionResult)).body(dto);
	}

	@Override
	@PreAuthorize("@resourceAuthorization.canAccessTestData(authentication)")
	public ResponseEntity<TestDataSetDTO> getTestData(Long testDataId, boolean includeRetired) {
		validator.validateGetTestData(testDataId);
		return databaseService.getTestData(new TestDataDatabaseService.TestDataDetails(testDataId, includeRetired))
			.map(hateoasFacade::toDTO)
			.map(ResponseEntity::ok)
			.orElse(ResponseEntity.notFound().build());
	}

	@Override
	@PreAuthorize("@resourceAuthorization.canAccessTestData(authentication)")
	public ResponseEntity<PagedTestDataSetDTO> listTestData(Integer page, Integer size, boolean includeRetired) {
		validator.validateListTestData(page, size);
		PagedResult<TestDataSet> testData = databaseService
			.listTestData(new TestDataDatabaseService.TestDataPage(page, size, includeRetired));
		return ResponseEntity.ok(hateoasFacade.toPagedDTO(testData, includeRetired));
	}

	@Override
	@PreAuthorize("@resourceAuthorization.canAccessTestData(authentication)")
	public ResponseEntity<DatabaseDeletionResultDTO> restoreTestData(Long testDataId) {
		validator.validateRestoreTestData(testDataId);
		DatabaseDeletionResult deletionResult = databaseService.restoreTestData(testDataId);
		DatabaseDeletionResultDTO dto = hateoasFacade.toDTO(deletionResult);
		return ResponseEntity.status(hateoasFacade.toHTTPStatus(deletionResult)).body(dto);
	}

	@Override
	@PreAuthorize("@resourceAuthorization.canAccessTestData(authentication)")
	public ResponseEntity<TestDataSetDTO> updateTestData(Long testDataId, @Nullable TestDataSetDefinitionDTO dto) {
		validator.validateUpdateTestData(testDataId, dto);
		TestDataSetDefinition definition = hateoasFacade.toDomain(dto);
		return databaseService.updateTestData(new TestDataDatabaseService.TestDataUpdate(testDataId, definition))
			.map(hateoasFacade::toDTO)
			.map(ResponseEntity::ok)
			.orElse(ResponseEntity.notFound().build());
	}

}
