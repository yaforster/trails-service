package io.github.yaforster.trails.integration.api;

import io.github.yaforster.trails.app.services.TestService;
import io.github.yaforster.trails.core.test.Browser;
import io.github.yaforster.trails.core.test.TestPlanRunDefinition;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.util.List;
import java.util.UUID;

import static org.hamcrest.Matchers.containsString;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.request;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class TestApiIT extends RestApiIntegrationTestBase {

	@MockitoBean
	private TestService testService;

	@Test
	void runTest_shouldMapRequestAndReturnAcceptedResponseWithLinks() throws Exception {
		performPostJson("/test", """
				{
				  "testPlanID": 333,
				  "browsersToTest": ["CHROME", "FIREFOX"]
				}
				""").andExpect(status().isAccepted())
			.andExpect(jsonPath("$.executionId").isNotEmpty())
			.andExpect(jsonPath("$.status").value("ACCEPTED"))
			.andExpect(jsonPath("$.message").value("Test execution accepted"))
			.andExpect(jsonPath("$._links.events.href", containsString("/api/test/events/")))
			.andExpect(jsonPath("$._links.result").doesNotExist());

		ArgumentCaptor<TestService.TestRunExecution> testRunExecutionCaptor = ArgumentCaptor
			.forClass(TestService.TestRunExecution.class);
		verify(testService, timeout(1000)).runTest(testRunExecutionCaptor.capture());
		assertEquals(333L, testRunExecutionCaptor.getValue().runDefinition().testPlanID());
		assertEquals(List.of(Browser.CHROME, Browser.FIREFOX),
				testRunExecutionCaptor.getValue().runDefinition().browsersToTest());
	}

	@Test
	void runTest_shouldReturnBadRequest_forMalformedJson() throws Exception {
		performPostJson("/test", "{ invalid-json }").andExpect(status().isBadRequest());

		verifyNoInteractions(testService);
	}

	@Test
	void streamTestExecutionEvents_shouldStartSseStream() throws Exception {
		performGet("/test/events/" + UUID.randomUUID(), org.springframework.http.MediaType.TEXT_EVENT_STREAM)
			.andExpect(status().isOk())
			.andExpect(request().asyncStarted());
	}

}
