package io.github.yaforster.trails.adapter.api.rest.testplan;

import io.github.yaforster.trails.core.PagedResult;

import io.github.yaforster.trails.adapter.api.APIRequestValidationException;
import io.github.yaforster.trails.adapter.api.APIExceptionMapper;
import io.github.yaforster.trails.adapter.api.rest.action.mapper.ActionMapper;
import io.github.yaforster.trails.adapter.api.rest.deletion.DatabaseDeletionResultModelMapper;
import io.github.yaforster.trails.adapter.api.rest.deletion.DatabaseDeletionResultMapper;
import io.github.yaforster.trails.adapter.api.rest.model.DatabaseDeletionResultDTO;
import io.github.yaforster.trails.adapter.api.rest.model.PagedTestPlansDTO;
import io.github.yaforster.trails.adapter.api.rest.model.PersistedTestPlanDTO;
import io.github.yaforster.trails.adapter.api.rest.model.TestPlanDefinitionDTO;
import io.github.yaforster.trails.adapter.api.rest.model.ValidationErrorDTO;
import io.github.yaforster.trails.app.services.StageDatabaseService;
import io.github.yaforster.trails.app.services.TestPlanDatabaseService;
import io.github.yaforster.trails.core.definition.TestPlanDefinition;
import io.github.yaforster.trails.core.deletion.*;
import io.github.yaforster.trails.core.persisted.PersistedStage;
import io.github.yaforster.trails.core.persisted.PersistedTestPlan;
import io.github.yaforster.trails.core.ValidationViolation;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class TestPlanControllerTest {

	private final TestPlanDatabaseService databaseService = mock(TestPlanDatabaseService.class);

	private final StageDatabaseService stageDatabaseService = mock(StageDatabaseService.class);

	private final TestPlanHATEOASFacade facade = mock(TestPlanHATEOASFacade.class);

	private final TestPlanRequestValidator validator = mock(TestPlanRequestValidator.class);

	private final APIExceptionMapper exceptionMapper = mock(APIExceptionMapper.class);

	private final TestPlanController controller = new TestPlanController(databaseService, stageDatabaseService, facade,
			validator, exceptionMapper);

	@Test
	void handleActionDetailsValidation_mapsTypedValidationErrorThroughExceptionMapper() {
		ActionDetailsValidationException exception = new ActionDetailsValidationException(null,
				"Coordinate click details validation failed.",
				List.of(new ValidationViolation("TEST_PLAN_COORDINATE_CLICK_X_COORDINATE_INVALID",
						"X coordinate must be an integer between 0 and 2147483647.",
						"/testSteps/0/details/xCoordinate")));
		ResponseEntity<List<ValidationErrorDTO>> expected = ResponseEntity.badRequest()
			.body(List.of(new ValidationErrorDTO().code("TEST_PLAN_COORDINATE_CLICK_X_COORDINATE_INVALID")));
		when(exceptionMapper.toErrorResponse(any(APIRequestValidationException.class))).thenReturn(expected);

		ResponseEntity<List<ValidationErrorDTO>> response = controller.handleActionDetailsValidation(exception);

		assertSame(expected, response);
		verify(exceptionMapper).toErrorResponse(any(APIRequestValidationException.class));
	}

	@Test
	void createNewTestPlan_shouldMapStoreAndReturnOk() {
		TestPlanDefinitionDTO definitionDTO = new TestPlanDefinitionDTO().label("plan");
		TestPlanDefinition definition = new TestPlanDefinition(1L, 2L, "plan", List.of());
		PersistedTestPlan persisted = new PersistedTestPlan(9L, 1L, 2L, "plan");
		PersistedTestPlanDTO dto = new PersistedTestPlanDTO().id(9L);
		when(facade.fromDTO(1L, 2L, definitionDTO)).thenReturn(definition);
		when(databaseService.storeTestPlan(definition)).thenReturn(persisted);
		when(databaseService.hasActions(9L)).thenReturn(true);
		when(facade.toDTO(1L, 2L, persisted, true)).thenReturn(dto);

		ResponseEntity<PersistedTestPlanDTO> response = controller.createNewTestPlan(1L, 2L, definitionDTO);

		assertEquals(HttpStatus.OK, response.getStatusCode());
		assertSame(dto, response.getBody());
		verify(validator).validateCreateNewTestPlan(1L, 2L, definitionDTO);
		verify(facade).fromDTO(1L, 2L, definitionDTO);
		verify(databaseService).storeTestPlan(definition);
		verify(databaseService).hasActions(9L);
		verify(facade).toDTO(1L, 2L, persisted, true);
	}

	@Test
	void deleteTestPlan_shouldReturnStatusFromDeletionResult() {
		DatabaseDeletionResult deletionResult = new DeletionFailure(9L, "conflict", null);
		DatabaseDeletionResultDTO dto = new DatabaseDeletionResultDTO();
		when(databaseService.deleteTestPlan(new TestPlanDatabaseService.TestPlanReference(1L, 2L, 9L)))
			.thenReturn(deletionResult);
		when(facade.toDTO(1L, 2L, deletionResult)).thenReturn(dto);
		when(facade.toHTTPStatus(deletionResult)).thenReturn(HttpStatus.CONFLICT);

		ResponseEntity<DatabaseDeletionResultDTO> response = controller.deleteTestPlan(1L, 2L, 9L);

		assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
		assertSame(dto, response.getBody());
		verify(validator).validateDeleteTestPlan(1L, 2L, 9L);
		verify(databaseService).deleteTestPlan(new TestPlanDatabaseService.TestPlanReference(1L, 2L, 9L));
		verify(facade).toDTO(1L, 2L, deletionResult);
		verify(facade).toHTTPStatus(deletionResult);
	}

	@Test
	void getTestPlan_shouldReturnOkWhenFound() {
		PersistedTestPlan persisted = new PersistedTestPlan(9L, 1L, 2L, "plan");
		PersistedTestPlanDTO dto = new PersistedTestPlanDTO().id(9L);
		when(databaseService
			.getPersistedTestPlan(new TestPlanDatabaseService.PersistedTestPlanDetails(1L, 2L, 9L, false)))
			.thenReturn(Optional.of(persisted));
		when(databaseService.hasActions(9L)).thenReturn(true);
		when(facade.toDTO(1L, 2L, persisted, true)).thenReturn(dto);

		ResponseEntity<PersistedTestPlanDTO> response = controller.getTestPlan(1L, 2L, 9L, false);

		assertEquals(HttpStatus.OK, response.getStatusCode());
		assertSame(dto, response.getBody());
		verify(validator).validateGetTestPlan(1L, 2L, 9L);
		verify(databaseService)
			.getPersistedTestPlan(new TestPlanDatabaseService.PersistedTestPlanDetails(1L, 2L, 9L, false));
		verify(databaseService).hasActions(9L);
		verify(facade).toDTO(1L, 2L, persisted, true);
	}

	@Test
	void promoteTestPlan_shouldReturnCreatedWithLocation_whenStagesExist() {
		PersistedStage sourceStage = new PersistedStage(2L, 1L, "dev", "https://dev.example");
		PersistedStage targetStage = new PersistedStage(3L, 1L, "test", "https://test.example");
		PersistedTestPlan promoted = new PersistedTestPlan(12L, 1L, 3L, "plan");
		PersistedTestPlanDTO dto = new PersistedTestPlanDTO().id(12L);
		when(stageDatabaseService.getStage(new StageDatabaseService.StageDetails(1L, 2L, false)))
			.thenReturn(Optional.of(sourceStage));
		when(stageDatabaseService.getStage(new StageDatabaseService.StageDetails(1L, 3L, false)))
			.thenReturn(Optional.of(targetStage));
		when(databaseService
			.getPersistedTestPlan(new TestPlanDatabaseService.PersistedTestPlanDetails(1L, 2L, 9L, false)))
			.thenReturn(Optional.of(new PersistedTestPlan(9L, 1L, 2L, "plan")));
		when(databaseService.promoteTestPlan(new TestPlanDatabaseService.TestPlanPromotion(1L, 2L, 9L, 3L)))
			.thenReturn(Optional.of(promoted));
		when(databaseService.hasActions(12L)).thenReturn(true);
		when(facade.toDTO(1L, 3L, promoted, true)).thenReturn(dto);

		ResponseEntity<PersistedTestPlanDTO> response = controller.promoteTestPlan(1L, 2L, 9L, 3L);

		assertEquals(HttpStatus.CREATED, response.getStatusCode());
		assertEquals(java.net.URI.create("/api/applications/1/stages/3/testPlans/12"),
				response.getHeaders().getLocation());
		assertSame(dto, response.getBody());
		verify(validator).validatePromoteTestPlan(1L, 2L, 9L, 3L);
	}

	@Test
	void promoteTestPlan_shouldReturnNotFound_whenTargetStageIsMissing() {
		PersistedStage sourceStage = new PersistedStage(2L, 1L, "dev", "https://dev.example");
		when(stageDatabaseService.getStage(new StageDatabaseService.StageDetails(1L, 2L, false)))
			.thenReturn(Optional.of(sourceStage));
		when(stageDatabaseService.getStage(new StageDatabaseService.StageDetails(1L, 3L, false)))
			.thenReturn(Optional.empty());

		ResponseEntity<PersistedTestPlanDTO> response = controller.promoteTestPlan(1L, 2L, 9L, 3L);

		assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
		verify(validator).validatePromoteTestPlan(1L, 2L, 9L, 3L);
		verify(databaseService, never()).promoteTestPlan(any());
	}

	@Test
	void promoteTestPlan_shouldReturnNotFound_whenSourceTestPlanIsMissing() {
		PersistedStage sourceStage = new PersistedStage(2L, 1L, "dev", "https://dev.example");
		PersistedStage targetStage = new PersistedStage(3L, 1L, "test", "https://test.example");
		when(stageDatabaseService.getStage(new StageDatabaseService.StageDetails(1L, 2L, false)))
			.thenReturn(Optional.of(sourceStage));
		when(stageDatabaseService.getStage(new StageDatabaseService.StageDetails(1L, 3L, false)))
			.thenReturn(Optional.of(targetStage));
		when(databaseService
			.getPersistedTestPlan(new TestPlanDatabaseService.PersistedTestPlanDetails(1L, 2L, 9L, false)))
			.thenReturn(Optional.empty());

		ResponseEntity<PersistedTestPlanDTO> response = controller.promoteTestPlan(1L, 2L, 9L, 3L);

		assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
		verify(validator).validatePromoteTestPlan(1L, 2L, 9L, 3L);
		verify(databaseService, never()).promoteTestPlan(any());
	}

	@Test
	void promoteTestPlan_shouldThrowValidationException_whenPromotionCopyFails() {
		PersistedStage sourceStage = new PersistedStage(2L, 1L, "dev", "https://dev.example");
		PersistedStage targetStage = new PersistedStage(3L, 1L, "test", "https://test.example");
		when(stageDatabaseService.getStage(new StageDatabaseService.StageDetails(1L, 2L, false)))
			.thenReturn(Optional.of(sourceStage));
		when(stageDatabaseService.getStage(new StageDatabaseService.StageDetails(1L, 3L, false)))
			.thenReturn(Optional.of(targetStage));
		when(databaseService
			.getPersistedTestPlan(new TestPlanDatabaseService.PersistedTestPlanDetails(1L, 2L, 9L, false)))
			.thenReturn(Optional.of(new PersistedTestPlan(9L, 1L, 2L, "plan")));
		when(databaseService.promoteTestPlan(new TestPlanDatabaseService.TestPlanPromotion(1L, 2L, 9L, 3L)))
			.thenReturn(Optional.empty());

		APIRequestValidationException exception = assertThrows(APIRequestValidationException.class,
				() -> controller.promoteTestPlan(1L, 2L, 9L, 3L));

		assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusCode());
		assertEquals("TEST_PLAN_PROMOTION_COPY_FAILED", exception.getValidationViolations().getFirst().code());
		assertEquals("/targetStageId", exception.getValidationViolations().getFirst().path());
		verify(validator).validatePromoteTestPlan(1L, 2L, 9L, 3L);
		verify(databaseService).promoteTestPlan(new TestPlanDatabaseService.TestPlanPromotion(1L, 2L, 9L, 3L));
	}

	@Test
	void getTestPlan_shouldReturnNotFoundWhenMissing() {
		when(databaseService
			.getPersistedTestPlan(new TestPlanDatabaseService.PersistedTestPlanDetails(1L, 2L, 9L, false)))
			.thenReturn(Optional.empty());

		ResponseEntity<PersistedTestPlanDTO> response = controller.getTestPlan(1L, 2L, 9L, false);

		assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
		assertNull(response.getBody());
		verify(validator).validateGetTestPlan(1L, 2L, 9L);
		verify(databaseService)
			.getPersistedTestPlan(new TestPlanDatabaseService.PersistedTestPlanDetails(1L, 2L, 9L, false));
		verifyNoInteractions(facade);
	}

	@Test
	void listTestPlans_shouldReturnOkWithMappedPage() {
		PagedResult<PersistedTestPlan> page = PagedResult
			.singlePage(List.of(new PersistedTestPlan(9L, 1L, 2L, "plan")));
		PagedTestPlansDTO dto = new PagedTestPlansDTO();
		when(databaseService.getTestPlans(new TestPlanDatabaseService.TestPlanPage(1L, 2L, 0, 20, false)))
			.thenReturn(page);
		when(facade.mapToPagedTestPlanDTO(1L, 2L, page)).thenReturn(dto);

		ResponseEntity<PagedTestPlansDTO> response = controller.listTestPlans(1L, 2L, 0, 20, false);

		assertEquals(HttpStatus.OK, response.getStatusCode());
		assertSame(dto, response.getBody());
		verify(validator).validateListTestPlans(1L, 2L, 0, 20);
		verify(databaseService).getTestPlans(new TestPlanDatabaseService.TestPlanPage(1L, 2L, 0, 20, false));
		verify(facade).mapToPagedTestPlanDTO(1L, 2L, page);
	}

	@Test
	void toHTTPStatus_shouldMapAllResultTypes() {
		TestPlanHATEOASFacade realFacade = new TestPlanHATEOASFacade(
				mock(io.github.yaforster.trails.adapter.api.rest.action.mapper.ActionMapper.class),
				mock(io.github.yaforster.trails.adapter.api.rest.testplan.mapper.PersistedTestPlanDTOMapper.class),
				mock(io.github.yaforster.trails.adapter.api.rest.testplan.assembler.TestPlanModelAssembler.class),
				mock(io.github.yaforster.trails.adapter.api.rest.testplan.assembler.PagedTestPlanModelAssembler.class),
				new DatabaseDeletionResultMapper(),
				new io.github.yaforster.trails.adapter.api.rest.deletion.DatabaseDeletionResultModelMapper(),
				mock(io.github.yaforster.trails.adapter.api.rest.testplan.mapper.TestPlanMapper.class),
				mock(TestPlanDatabaseService.class),
				mock(io.github.yaforster.trails.adapter.api.rest.testplan.mapper.PagedTestPlanMapper.class));
		assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, realFacade
			.toHTTPStatus(new DeletionFailure(1L, "x", new ErrorDetails("RuntimeException", "x", "trace"))));
		assertEquals(HttpStatus.NOT_FOUND, realFacade.toHTTPStatus(new DeletionNotFound(1L)));
		assertEquals(HttpStatus.OK, realFacade.toHTTPStatus(new DeletionSuccess(1L)));
		assertEquals(HttpStatus.CONFLICT, realFacade.toHTTPStatus(new DeletionFailure(1L, "conflict", null)));
	}

}
