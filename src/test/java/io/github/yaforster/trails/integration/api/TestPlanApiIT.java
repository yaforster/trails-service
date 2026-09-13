package io.github.yaforster.trails.integration.api;

import io.github.yaforster.trails.core.PagedResult;

import io.github.yaforster.trails.app.services.TestPlanDatabaseService;
import io.github.yaforster.trails.core.deletion.*;
import io.github.yaforster.trails.core.persisted.PersistedTestPlan;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.ResultActions;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class TestPlanApiIT extends RestApiIntegrationTestBase {

	@MockitoBean
	private TestPlanDatabaseService testPlanDatabaseService;

	@Test
	void createNewTestPlan_shouldReturnPersistedPlanWithLinks() throws Exception {
		PersistedTestPlan persisted = new PersistedTestPlan(1001L, 77L, 88L, "Checkout Plan");
		when(testPlanDatabaseService.storeTestPlan(any())).thenReturn(persisted);
		when(testPlanDatabaseService.hasActions(1001L)).thenReturn(true);

		performPutJson("/applications/77/stages/88/testPlans", """
				{
				  "label": "Checkout Plan",
				  "testSteps": [
				    {
				      "referenceID": 1,
				      "details": {
				        "detailsType": "EXPLICIT_WAIT",
				        "delayMillis": 0
				      }
				    }
				  ]
				}
				""").andExpect(status().isOk())
			.andExpect(jsonPath("$.id").value(1001))
			.andExpect(jsonPath("$._links.self.href", containsString("/applications/77/stages/88/testPlans/1001")))
			.andExpect(jsonPath("$._links.collection.href", containsString("/applications/77/stages/88/testPlans")))
			.andExpect(jsonPath("$._links.statistics.href",
					containsString("/applications/77/stages/88/testPlans/1001/statistics")))
			.andExpect(jsonPath("$._links.execute.href", containsString("/api/test")))
			.andExpect(jsonPath("$._links.steps.href",
					containsString("/applications/77/stages/88/testPlans/1001/actions")));
	}

	@Test
	void createNewTestPlan_shouldReturnBadRequest_forMalformedJson() throws Exception {
		performPutJson("/applications/77/stages/88/testPlans", "{ invalid-json }").andExpect(status().isBadRequest());
	}

	@ParameterizedTest
	@MethodSource("coordinateBounds")
	void createNewTestPlan_shouldAcceptCoordinateClickAtInt32Bounds(String xCoordinate, String yCoordinate)
			throws Exception {
		PersistedTestPlan persisted = new PersistedTestPlan(1002L, 77L, 88L, "Coordinate Plan");
		when(testPlanDatabaseService.storeTestPlan(any())).thenReturn(persisted);
		when(testPlanDatabaseService.hasActions(1002L)).thenReturn(true);

		performPutJson("/applications/77/stages/88/testPlans", coordinateClickPlan(xCoordinate, yCoordinate))
			.andExpect(status().isOk());

		verify(testPlanDatabaseService).storeTestPlan(any());
	}

	@ParameterizedTest
	@MethodSource("invalidCoordinateFields")
	void createNewTestPlan_shouldRejectInvalidCoordinateValues(String field, String value,
			ExpectedValidationError expected) throws Exception {
		String xCoordinate = "xCoordinate".equals(field) ? value : "0";
		String yCoordinate = "yCoordinate".equals(field) ? value : "0";

		assertCoordinateErrors(coordinateClickPlan(xCoordinate, yCoordinate), expected);
	}

	@Test
	void createNewTestPlan_shouldReportCoordinateErrorsInFieldOrder() throws Exception {
		assertCoordinateErrors(coordinateClickPlan("-1", "false"), invalidCoordinateError("xCoordinate", 0),
				invalidCoordinateError("yCoordinate", 0));
	}

	@Test
	void createNewTestPlan_shouldReportSecondActionCoordinateErrorsAtSecondActionIndex() throws Exception {
		assertCoordinateErrors(twoActionCoordinateClickPlan("[]", "{}"), invalidCoordinateError("xCoordinate", 1),
				invalidCoordinateError("yCoordinate", 1));
	}

	@Test
	void createNewTestPlan_shouldAcceptResizeViewportAtPositiveInt32Bounds() throws Exception {
		PersistedTestPlan persisted = new PersistedTestPlan(1004L, 77L, 88L, "Resize Plan");
		when(testPlanDatabaseService.storeTestPlan(any())).thenReturn(persisted);
		when(testPlanDatabaseService.hasActions(1004L)).thenReturn(true);

		performPutJson("/applications/77/stages/88/testPlans", resizeViewportPlan("1", "2147483647"))
			.andExpect(status().isOk());

		verify(testPlanDatabaseService).storeTestPlan(any());
	}

	@Test
	void createNewTestPlan_shouldRejectResizeViewportDimensionsInWidthHeightOrder() throws Exception {
		assertCoordinateErrors(resizeViewportPlan("0", "false"),
				new ExpectedValidationError("TEST_PLAN_RESIZE_VIEWPORT_VIEWPORT_WIDTH_INVALID",
						"Viewport width must be an integer between 1 and 2147483647.",
						"/testSteps/0/details/viewportWidth"),
				new ExpectedValidationError("TEST_PLAN_RESIZE_VIEWPORT_VIEWPORT_HEIGHT_INVALID",
						"Viewport height must be an integer between 1 and 2147483647.",
						"/testSteps/0/details/viewportHeight"));
	}

	@Test
	void createNewTestPlan_shouldLeaveFractionalExplicitWaitOnGeneratedDeserializationPath() throws Exception {
		PersistedTestPlan persisted = new PersistedTestPlan(1003L, 77L, 88L, "Wait Plan");
		when(testPlanDatabaseService.storeTestPlan(any())).thenReturn(persisted);
		when(testPlanDatabaseService.hasActions(1003L)).thenReturn(true);

		performPutJson("/applications/77/stages/88/testPlans", explicitWaitPlan("1.5")).andExpect(status().isOk());

		verify(testPlanDatabaseService).storeTestPlan(any());
	}

	@Test
	void createNewTestPlan_shouldReturnValidationError_whenLabelAlreadyExists() throws Exception {
		when(testPlanDatabaseService
			.existsByLabel(new TestPlanDatabaseService.TestPlanLabel(77L, 88L, "Checkout Plan"))).thenReturn(true);

		performPutJson("/applications/77/stages/88/testPlans", """
				{
				  "label": "Checkout Plan",
				  "testSteps": [
				    {
				      "referenceID": 1,
				      "details": {
				        "detailsType": "EXPLICIT_WAIT",
				        "delayMillis": 0
				      }
				    }
				  ]
				}
				""", MediaType.APPLICATION_PROBLEM_JSON).andExpect(status().isBadRequest())
			.andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
			.andExpect(jsonPath("$[0].code").value("TEST_PLAN_LABEL_ALREADY_EXISTS"));

		verify(testPlanDatabaseService, never()).storeTestPlan(any());
	}

	@Test
	void listTestPlans_shouldReturnPagedResponseWithExpectedLinks() throws Exception {
		PersistedTestPlan first = new PersistedTestPlan(1101L, 77L, 88L, "Plan A");
		PersistedTestPlan second = new PersistedTestPlan(1102L, 77L, 88L, "Plan B");
		PagedResult<PersistedTestPlan> pagedPlans = new PagedResult<>(List.of(first, second), 1, 2, 5);

		when(testPlanDatabaseService.getTestPlans(new TestPlanDatabaseService.TestPlanPage(77L, 88L, 1, 2, false)))
			.thenReturn(pagedPlans);
		when(testPlanDatabaseService
			.findTestPlanIdsWithActions(new TestPlanDatabaseService.TestPlanActions(77L, 88L, List.of(1101L, 1102L))))
			.thenReturn(Set.of(1101L));

		ResultActions response = performGet("/applications/77/stages/88/testPlans?page=1&size=2")
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.items[0].id").value(1101))
			.andExpect(jsonPath("$.items[0].label").value("Plan A"))
			.andExpect(jsonPath("$.items[0]._links.self.href",
					containsString("/applications/77/stages/88/testPlans/1101")))
			.andExpect(jsonPath("$.items[0]._links.steps.href",
					containsString("/applications/77/stages/88/testPlans/1101/actions")))
			.andExpect(jsonPath("$.items[1].id").value(1102))
			.andExpect(jsonPath("$.items[1].label").value("Plan B"))
			.andExpect(jsonPath("$.items[1]._links.self.href",
					containsString("/applications/77/stages/88/testPlans/1102")))
			.andExpect(jsonPath("$.items[1]._links.steps").doesNotExist())
			.andExpect(jsonPath("$._links.self.href", containsString("/applications/77/stages/88/testPlans")));

		expectPage(response, 1, 2, 5, 3);
		expectPagingLinks(response, 0, 2, 0, 2);
	}

	@Test
	void listTestPlans_shouldUseDefaultPagingParameters_whenQueryParamsMissing() throws Exception {
		PersistedTestPlan persisted = new PersistedTestPlan(1111L, 77L, 88L, "Default Plan");
		PagedResult<PersistedTestPlan> pagedPlans = new PagedResult<>(List.of(persisted), DEFAULT_PAGE, DEFAULT_SIZE,
				1);

		when(testPlanDatabaseService
			.getTestPlans(new TestPlanDatabaseService.TestPlanPage(77L, 88L, DEFAULT_PAGE, DEFAULT_SIZE, false)))
			.thenReturn(pagedPlans);
		when(testPlanDatabaseService
			.findTestPlanIdsWithActions(new TestPlanDatabaseService.TestPlanActions(77L, 88L, List.of(1111L))))
			.thenReturn(Set.of());

		ResultActions response = performGet("/applications/77/stages/88/testPlans").andExpect(status().isOk());

		expectPage(response, DEFAULT_PAGE, DEFAULT_SIZE, 1, 1);
		expectDefaultSelfPagingParams(response);
	}

	@Test
	void listTestPlans_shouldReturnEmptyPageWithoutPrevAndNext() throws Exception {
		PagedResult<PersistedTestPlan> emptyPage = new PagedResult<>(Collections.emptyList(), DEFAULT_PAGE,
				DEFAULT_SIZE, 0);

		when(testPlanDatabaseService
			.getTestPlans(new TestPlanDatabaseService.TestPlanPage(77L, 88L, DEFAULT_PAGE, DEFAULT_SIZE, false)))
			.thenReturn(emptyPage);

		ResultActions response = performGet("/applications/77/stages/88/testPlans?page=0&size=20")
			.andExpect(status().isOk());

		expectEmptyItems(response);
		expectPage(response, DEFAULT_PAGE, DEFAULT_SIZE, 0, 0);
		expectPagingLinks(response, 0, 0, null, null);
	}

	@Test
	void getTestPlan_shouldReturnPlanWithStepsLink_whenActionsExist() throws Exception {
		PersistedTestPlan testPlan = new PersistedTestPlan(1201L, 77L, 88L, "Detailed Plan");
		when(testPlanDatabaseService
			.getPersistedTestPlan(new TestPlanDatabaseService.PersistedTestPlanDetails(77L, 88L, 1201L, false)))
			.thenReturn(Optional.of(testPlan));
		when(testPlanDatabaseService.hasActions(1201L)).thenReturn(true);

		performGet("/applications/77/stages/88/testPlans/1201").andExpect(status().isOk())
			.andExpect(jsonPath("$.id").value(1201))
			.andExpect(jsonPath("$._links.self.href", containsString("/applications/77/stages/88/testPlans/1201")))
			.andExpect(jsonPath("$._links.collection.href", containsString("/applications/77/stages/88/testPlans")))
			.andExpect(jsonPath("$._links.steps.href",
					containsString("/applications/77/stages/88/testPlans/1201/actions")));
	}

	@Test
	void getTestPlan_shouldReturnNotFound_whenMissing() throws Exception {
		when(testPlanDatabaseService
			.getPersistedTestPlan(new TestPlanDatabaseService.PersistedTestPlanDetails(77L, 88L, 9999L, false)))
			.thenReturn(Optional.empty());

		performGet("/applications/77/stages/88/testPlans/9999").andExpect(status().isNotFound());
	}

	@Test
	void deleteTestPlan_shouldReturnOk_forDeletionSuccess() throws Exception {
		when(testPlanDatabaseService.deleteTestPlan(new TestPlanDatabaseService.TestPlanReference(77L, 88L, 1301L)))
			.thenReturn(new DeletionSuccess(1301L));

		performDelete("/applications/77/stages/88/testPlans/1301").andExpect(status().isOk())
			.andExpect(jsonPath("$.deletionRequestedForID").value(1301))
			.andExpect(jsonPath("$.deletionResult").value(true));
	}

	@Test
	void deleteTestPlan_shouldReturnNotFound_forDeletionNotFound() throws Exception {
		when(testPlanDatabaseService.deleteTestPlan(new TestPlanDatabaseService.TestPlanReference(77L, 88L, 1302L)))
			.thenReturn(new DeletionNotFound(1302L));

		performDelete("/applications/77/stages/88/testPlans/1302").andExpect(status().isNotFound())
			.andExpect(jsonPath("$.deletionRequestedForID").value(1302))
			.andExpect(jsonPath("$.deletionResult").value(false))
			.andExpect(jsonPath("$._links.collection.href", containsString("/applications/77/stages/88/testPlans")));
	}

	@Test
	void deleteTestPlan_shouldReturnConflict_forDeletionConflict() throws Exception {
		when(testPlanDatabaseService.deleteTestPlan(new TestPlanDatabaseService.TestPlanReference(77L, 88L, 1303L)))
			.thenReturn(new DeletionFailure(1303L, "conflict", null));

		performDelete("/applications/77/stages/88/testPlans/1303").andExpect(status().isConflict())
			.andExpect(jsonPath("$.deletionRequestedForID").value(1303))
			.andExpect(jsonPath("$.deletionResult").value(false));
	}

	@Test
	void deleteTestPlan_shouldReturnInternalServerError_forDeletionFailure() throws Exception {
		DeletionFailure failure = new DeletionFailure(1304L, "boom",
				new ErrorDetails("RuntimeException", "boom", "stacktrace"));
		when(testPlanDatabaseService.deleteTestPlan(new TestPlanDatabaseService.TestPlanReference(77L, 88L, 1304L)))
			.thenReturn(failure);

		performDelete("/applications/77/stages/88/testPlans/1304").andExpect(status().isInternalServerError())
			.andExpect(jsonPath("$.deletionRequestedForID").value(1304))
			.andExpect(jsonPath("$.deletionResult").value(false))
			.andExpect(jsonPath("$.message").value("boom"));
	}

	private static Stream<Arguments> coordinateBounds() {
		return Stream.of(Arguments.of("0", "2147483647"), Arguments.of("2147483647", "0"));
	}

	private static Stream<Arguments> invalidCoordinateFields() {
		return Stream.of("xCoordinate", "yCoordinate")
			.flatMap(field -> Stream.of(Arguments.of(field, null, requiredCoordinateError(field, 0)),
					Arguments.of(field, "null", requiredCoordinateError(field, 0)),
					Arguments.of(field, "\"1\"", invalidCoordinateError(field, 0)),
					Arguments.of(field, "1.5", invalidCoordinateError(field, 0)),
					Arguments.of(field, "-1", invalidCoordinateError(field, 0)),
					Arguments.of(field, "2147483648", invalidCoordinateError(field, 0)),
					Arguments.of(field, "true", invalidCoordinateError(field, 0)),
					Arguments.of(field, "{}", invalidCoordinateError(field, 0)),
					Arguments.of(field, "[]", invalidCoordinateError(field, 0))));
	}

	private static String coordinateClickPlan(String xCoordinate, String yCoordinate) {
		String xField = xCoordinate == null ? "" : ", \"xCoordinate\": " + xCoordinate;
		String yField = yCoordinate == null ? "" : ", \"yCoordinate\": " + yCoordinate;
		return "{ \"label\": \"Coordinate Plan\", \"testSteps\": [{ \"referenceID\": 1, \"details\": { "
				+ "\"detailsType\": \"COORDINATE_CLICK\"" + xField + yField + " } }] }";
	}

	private static String explicitWaitPlan(String delayMillis) {
		return "{ \"label\": \"Wait Plan\", \"testSteps\": [{ \"referenceID\": 1, \"details\": { "
				+ "\"detailsType\": \"EXPLICIT_WAIT\", \"delayMillis\": " + delayMillis + " } }] }";
	}

	private static String resizeViewportPlan(String viewportWidth, String viewportHeight) {
		return "{ \"label\": \"Resize Plan\", \"testSteps\": [{ \"referenceID\": 1, \"details\": { "
				+ "\"detailsType\": \"RESIZE_VIEWPORT\", \"viewportWidth\": " + viewportWidth + ", \"viewportHeight\": "
				+ viewportHeight + " } }] }";
	}

	private static ExpectedValidationError requiredCoordinateError(String field, int actionIndex) {
		String label = "xCoordinate".equals(field) ? "X" : "Y";
		return new ExpectedValidationError("TEST_PLAN_COORDINATE_CLICK_" + label + "_COORDINATE_REQUIRED",
				label + " coordinate is required.", coordinatePath(field, actionIndex));
	}

	private static ExpectedValidationError invalidCoordinateError(String field, int actionIndex) {
		String label = "xCoordinate".equals(field) ? "X" : "Y";
		return new ExpectedValidationError("TEST_PLAN_COORDINATE_CLICK_" + label + "_COORDINATE_INVALID",
				label + " coordinate must be an integer between 0 and 2147483647.", coordinatePath(field, actionIndex));
	}

	private static String coordinatePath(String field, int actionIndex) {
		return "/testSteps/" + actionIndex + "/details/" + field;
	}

	private void assertCoordinateErrors(String body, ExpectedValidationError... expectedErrors) throws Exception {
		for (MediaType accept : List.of(MediaType.APPLICATION_JSON, MediaType.APPLICATION_PROBLEM_JSON)) {
			ResultActions response = performPutJson("/applications/77/stages/88/testPlans", body, accept)
				.andExpect(status().isBadRequest())
				.andExpect(content().contentTypeCompatibleWith(accept));
			for (int index = 0; index < expectedErrors.length; index++) {
				ExpectedValidationError expected = expectedErrors[index];
				response.andExpect(jsonPath("$[" + index + "].code").value(expected.code()))
					.andExpect(jsonPath("$[" + index + "].message").value(expected.message()))
					.andExpect(jsonPath("$[" + index + "].path").value(expected.path()));
			}
			response.andExpect(jsonPath("$[" + expectedErrors.length + "]").doesNotExist());
		}
		verifyNoInteractions(testPlanDatabaseService);
	}

	private static String twoActionCoordinateClickPlan(String xCoordinate, String yCoordinate) {
		return "{ \"label\": \"Coordinate Plan\", \"testSteps\": ["
				+ "{ \"referenceID\": 1, \"details\": { \"detailsType\": \"EXPLICIT_WAIT\", \"delayMillis\": 0 } },"
				+ "{ \"referenceID\": 2, \"details\": { \"detailsType\": \"COORDINATE_CLICK\", \"xCoordinate\": "
				+ xCoordinate + ", \"yCoordinate\": " + yCoordinate + " } }] }";
	}

	private record ExpectedValidationError(String code, String message, String path) {
	}

}
