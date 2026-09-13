package io.github.yaforster.trails.adapter.api.rest.testplan;

import io.github.yaforster.trails.adapter.api.APIRequestValidationException;
import io.github.yaforster.trails.adapter.api.APIExceptionMapper;
import io.github.yaforster.trails.adapter.api.rest.model.DatabaseDeletionResultDTO;
import io.github.yaforster.trails.adapter.api.rest.model.PagedTestPlansDTO;
import io.github.yaforster.trails.adapter.api.rest.model.PersistedTestPlanDTO;
import io.github.yaforster.trails.adapter.api.rest.model.TestPlanDefinitionDTO;
import io.github.yaforster.trails.adapter.api.rest.model.ValidationErrorDTO;
import io.github.yaforster.trails.app.services.StageDatabaseService;
import io.github.yaforster.trails.app.services.TestPlanDatabaseService;
import io.github.yaforster.trails.core.PagedResult;
import io.github.yaforster.trails.core.ValidationViolation;
import io.github.yaforster.trails.core.definition.TestPlanDefinition;
import io.github.yaforster.trails.core.deletion.DatabaseDeletionResult;
import io.github.yaforster.trails.core.persisted.PersistedTestPlan;
import lombok.AllArgsConstructor;
import org.openapitools.api.TestPlanApi;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.net.URI;
import java.util.List;

@AllArgsConstructor
@RestController
public class TestPlanController implements TestPlanApi {

	private final TestPlanDatabaseService databaseService;

	private final StageDatabaseService stageDatabaseService;

	private final TestPlanHATEOASFacade hateoasFacade;

	private final TestPlanRequestValidator validator;

	private final APIExceptionMapper exceptionMapper;

	@ExceptionHandler(ActionDetailsValidationException.class)
	public ResponseEntity<List<ValidationErrorDTO>> handleActionDetailsValidation(
			ActionDetailsValidationException exception) {
		return exceptionMapper.toErrorResponse(new APIRequestValidationException(exception.getValidationViolations()));
	}

	@Override
	@PreAuthorize("@resourceAuthorization.canManageResources(authentication)")
	public ResponseEntity<PersistedTestPlanDTO> createNewTestPlan(Long applicationId, Long stageId,
			TestPlanDefinitionDTO definitionDTO) {
		validator.validateCreateNewTestPlan(applicationId, stageId, definitionDTO);
		TestPlanDefinition definition = hateoasFacade.fromDTO(applicationId, stageId, definitionDTO);
		PersistedTestPlan persistedTestPlan = databaseService.storeTestPlan(definition);
		boolean hasActions = databaseService.hasActions(persistedTestPlan.id());
		PersistedTestPlanDTO persistedTestPlanDTO = hateoasFacade.toDTO(applicationId, stageId, persistedTestPlan,
				hasActions);
		return ResponseEntity.ok(persistedTestPlanDTO);
	}

	@Override
	@PreAuthorize("@resourceAuthorization.canManageResources(authentication)")
	public ResponseEntity<DatabaseDeletionResultDTO> deleteTestPlan(Long applicationId, Long stageId, Long testPlanId) {
		validator.validateDeleteTestPlan(applicationId, stageId, testPlanId);
		DatabaseDeletionResult deletionResult = databaseService
			.deleteTestPlan(new TestPlanDatabaseService.TestPlanReference(applicationId, stageId, testPlanId));
		DatabaseDeletionResultDTO dto = hateoasFacade.toDTO(applicationId, stageId, deletionResult);
		return ResponseEntity.status(hateoasFacade.toHTTPStatus(deletionResult)).body(dto);
	}

	@Override
	@PreAuthorize("@resourceAuthorization.canManageResources(authentication)")
	public ResponseEntity<DatabaseDeletionResultDTO> restoreTestPlan(Long applicationId, Long stageId,
			Long testPlanId) {
		validator.validateDeleteTestPlan(applicationId, stageId, testPlanId);
		DatabaseDeletionResult deletionResult = databaseService
			.restoreTestPlan(new TestPlanDatabaseService.TestPlanReference(applicationId, stageId, testPlanId));
		DatabaseDeletionResultDTO dto = hateoasFacade.toDTO(applicationId, stageId, deletionResult);
		return ResponseEntity.status(hateoasFacade.toHTTPStatus(deletionResult)).body(dto);
	}

	@Override
	@PreAuthorize("@resourceAuthorization.canManageResources(authentication)")
	public ResponseEntity<PersistedTestPlanDTO> promoteTestPlan(Long applicationId, Long sourceStageId, Long testPlanId,
			Long targetStageId) {
		validator.validatePromoteTestPlan(applicationId, sourceStageId, testPlanId, targetStageId);
		if (stageDatabaseService.getStage(new StageDatabaseService.StageDetails(applicationId, sourceStageId, false))
			.isEmpty()
				|| stageDatabaseService
					.getStage(new StageDatabaseService.StageDetails(applicationId, targetStageId, false))
					.isEmpty()) {
			return ResponseEntity.notFound().build();
		}
		if (databaseService
			.getPersistedTestPlan(new TestPlanDatabaseService.PersistedTestPlanDetails(applicationId, sourceStageId,
					testPlanId, false))
			.isEmpty()) {
			return ResponseEntity.notFound().build();
		}
		return databaseService
			.promoteTestPlan(new TestPlanDatabaseService.TestPlanPromotion(applicationId, sourceStageId, testPlanId,
					targetStageId))
			.map(promotedTestPlan -> {
				boolean hasActions = databaseService.hasActions(promotedTestPlan.id());
				PersistedTestPlanDTO dto = hateoasFacade.toDTO(applicationId, targetStageId, promotedTestPlan,
						hasActions);
				URI location = URI.create("/api/applications/" + applicationId + "/stages/" + targetStageId
						+ "/testPlans/" + promotedTestPlan.id());
				return ResponseEntity.created(location).body(dto);
			})
			.orElseThrow(() -> new APIRequestValidationException(new ValidationViolation(
					"TEST_PLAN_PROMOTION_COPY_FAILED",
					"Test plan promotion failed because one or more actions could not be copied to the target stage. "
							+ "Make sure every referenced element has an active matching element in the target stage.",
					"/targetStageId")));
	}

	@Override
	public ResponseEntity<PersistedTestPlanDTO> getTestPlan(Long applicationId, Long stageId, Long testPlanId,
			boolean includeRetired) {
		validator.validateGetTestPlan(applicationId, stageId, testPlanId);
		return databaseService
			.getPersistedTestPlan(new TestPlanDatabaseService.PersistedTestPlanDetails(applicationId, stageId,
					testPlanId, includeRetired))
			.map(persistedTestPlan -> {
				boolean hasActions = databaseService.hasActions(persistedTestPlan.id());
				return hateoasFacade.toDTO(applicationId, stageId, persistedTestPlan, hasActions);
			})
			.map(ResponseEntity::ok)
			.orElse(ResponseEntity.notFound().build());
	}

	@Override
	public ResponseEntity<PagedTestPlansDTO> listTestPlans(Long applicationId, Long stageId, Integer page, Integer size,
			boolean includeRetired) {
		validator.validateListTestPlans(applicationId, stageId, page, size);
		PagedResult<PersistedTestPlan> pagedTestPlans = databaseService
			.getTestPlans(new TestPlanDatabaseService.TestPlanPage(applicationId, stageId, page, size, includeRetired));
		PagedTestPlansDTO dto = hateoasFacade.mapToPagedTestPlanDTO(applicationId, stageId, pagedTestPlans);
		return ResponseEntity.ok(dto);
	}

}
