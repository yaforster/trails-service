package io.github.yaforster.trails.adapter.api.rest.testplan;

import io.github.yaforster.trails.adapter.api.rest.model.ResizeViewportDetailsDTO;
import io.github.yaforster.trails.adapter.api.rest.model.TestPlanDefinitionDTO;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.json.JsonMapper;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ResizeViewportDetailsRawDeserializerTest {

	@Test
	void deserialize_acceptsPositiveInt32Bounds() throws Exception {
		TestPlanDefinitionDTO definition = mapper().readValue(plan("1", "2147483647"), TestPlanDefinitionDTO.class);

		ResizeViewportDetailsDTO details = assertInstanceOf(ResizeViewportDetailsDTO.class,
				definition.getTestSteps().getFirst().getDetails());
		assertEquals(1, details.getViewportWidth());
		assertEquals(Integer.MAX_VALUE, details.getViewportHeight());
	}

	@ParameterizedTest
	@ValueSource(strings = { "\"1\"", "1.5", "0", "-1", "2147483648", "false", "{}", "[]" })
	void deserialize_rejectsAllNonPositiveOrNonInt32ViewportWidths(String invalidValue) {
		JacksonException exception = assertThrows(JacksonException.class,
				() -> mapper().readValue(plan(invalidValue, "1"), TestPlanDefinitionDTO.class));

		ActionDetailsValidationException validationException = validationException(exception);
		assertEquals("TEST_PLAN_RESIZE_VIEWPORT_VIEWPORT_WIDTH_INVALID",
				validationException.getValidationViolations().getFirst().code());
		assertEquals("/testSteps/0/details/viewportWidth",
				validationException.getValidationViolations().getFirst().path());
	}

	@Test
	void deserialize_reportsMissingAndNullDimensionsInWidthHeightOrder() {
		JacksonException exception = assertThrows(JacksonException.class, () -> mapper().readValue("""
				{
				  "testSteps": [{ "referenceID": 1, "details": {
				    "detailsType": "RESIZE_VIEWPORT", "viewportHeight": null
				  }}]
				}
				""", TestPlanDefinitionDTO.class));

		assertEquals(
				List.of("TEST_PLAN_RESIZE_VIEWPORT_VIEWPORT_WIDTH_REQUIRED",
						"TEST_PLAN_RESIZE_VIEWPORT_VIEWPORT_HEIGHT_REQUIRED"),
				validationException(exception).getValidationViolations()
					.stream()
					.map(violation -> violation.code())
					.toList());
	}

	private static ObjectMapper mapper() {
		return JsonMapper.builder()
			.addMixIn(ResizeViewportDetailsDTO.class, ResizeViewportDetailsDTOMixin.class)
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

	private static String plan(String viewportWidth, String viewportHeight) {
		return """
				{
				  "testSteps": [{ "referenceID": 1, "details": {
				    "detailsType": "RESIZE_VIEWPORT", "viewportWidth": """ + viewportWidth + ", "
				+ "\"viewportHeight\": " + viewportHeight + """
						  }}]
						}
						""";
	}

}
