package io.github.yaforster.trails.adapter.api.rest.testplan;

import io.github.yaforster.trails.adapter.api.rest.model.ClickDetailsDTO;
import io.github.yaforster.trails.adapter.api.rest.model.CoordinateClickDetailsDTO;
import io.github.yaforster.trails.adapter.api.rest.model.TestPlanDefinitionDTO;
import org.junit.jupiter.api.Test;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.json.JsonMapper;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;

class CoordinateClickDetailsRawDeserializerTest {

	@Test
	void deserialize_acceptsInt32CoordinateBounds() throws Exception {
		TestPlanDefinitionDTO definition = mapper().readValue(coordinatePlan("0", "2147483647"),
				TestPlanDefinitionDTO.class);

		CoordinateClickDetailsDTO details = assertInstanceOf(CoordinateClickDetailsDTO.class,
				definition.getTestSteps().getFirst().getDetails());
		assertEquals(0, details.getxCoordinate());
		assertEquals(2147483647, details.getyCoordinate());
	}

	@Test
	void deserialize_aggregatesCoordinateErrorsInFieldOrderAtActionIndex() {
		JacksonException exception = assertThrows(JacksonException.class,
				() -> mapper().readValue(twoActionPlan("-1", "1.5"), TestPlanDefinitionDTO.class));

		ActionDetailsValidationException validationException = validationException(exception);
		assertEquals(
				List.of("TEST_PLAN_COORDINATE_CLICK_X_COORDINATE_INVALID",
						"TEST_PLAN_COORDINATE_CLICK_Y_COORDINATE_INVALID"),
				validationException.getValidationViolations().stream().map(violation -> violation.code()).toList());
		assertEquals(List.of("/testSteps/1/details/xCoordinate", "/testSteps/1/details/yCoordinate"),
				validationException.getValidationViolations().stream().map(violation -> violation.path()).toList());
	}

	@Test
	void deserialize_reportsMissingCoordinateAsRequired() {
		JacksonException exception = assertThrows(JacksonException.class,
				() -> mapper().readValue(coordinatePlan(null, "0"), TestPlanDefinitionDTO.class));

		assertEquals("TEST_PLAN_COORDINATE_CLICK_X_COORDINATE_REQUIRED",
				validationException(exception).getValidationViolations().getFirst().code());
	}

	@Test
	void deserialize_leavesLocatorClickOnGeneratedPath() throws Exception {
		TestPlanDefinitionDTO definition = mapper().readValue("""
				{
				  "testSteps": [{
				    "referenceID": 1,
				    "details": { "detailsType": "CLICK", "elementID": 7 }
				  }]
				}
				""", TestPlanDefinitionDTO.class);

		assertInstanceOf(ClickDetailsDTO.class, definition.getTestSteps().getFirst().getDetails());
	}

	private static ObjectMapper mapper() {
		return JsonMapper.builder()
			.addMixIn(CoordinateClickDetailsDTO.class, CoordinateClickDetailsDTOMixin.class)
			.build();
	}

	private static ActionDetailsValidationException validationException(Throwable exception) {
		for (Throwable cause = exception; cause != null; cause = cause.getCause()) {
			if (cause instanceof ActionDetailsValidationException validationException) {
				return validationException;
			}
		}
		throw new AssertionError("Expected ActionDetailsValidationException.", exception);
	}

	private static String coordinatePlan(String xCoordinate, String yCoordinate) {
		String x = xCoordinate == null ? "" : "\"xCoordinate\": " + xCoordinate + ",";
		return """
				{
				  "testSteps": [{
				    "referenceID": 1,
				    "details": {""" + x + """
				"detailsType": "COORDINATE_CLICK",
				"yCoordinate": """ + yCoordinate + """
				    }
				  }]
				}
				""";
	}

	private static String twoActionPlan(String xCoordinate, String yCoordinate) {
		return """
				{
				  "testSteps": [
				    { "referenceID": 1, "details": { "detailsType": "EXPLICIT_WAIT", "delayMillis": 0 } },
				    { "referenceID": 2, "details": {
				      "detailsType": "COORDINATE_CLICK",
				      "xCoordinate": """ + xCoordinate + ", \"yCoordinate\": " + yCoordinate + """
				    }}
				  ]
				}
				""";
	}

}
