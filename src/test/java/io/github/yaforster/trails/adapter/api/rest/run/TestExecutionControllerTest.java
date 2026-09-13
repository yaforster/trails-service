package io.github.yaforster.trails.adapter.api.rest.run;

import io.github.yaforster.trails.adapter.api.TestExecutionEventHub;
import io.github.yaforster.trails.adapter.api.rest.model.TestExecutionAcceptedDTO;
import io.github.yaforster.trails.adapter.api.rest.model.TestExecutionStatusDTO;
import io.github.yaforster.trails.adapter.api.rest.model.TestPlanRunDefinitionDTO;
import io.github.yaforster.trails.adapter.api.rest.testrun.mapper.TestRunDefinitionMapper;
import io.github.yaforster.trails.app.services.TestExecutionFacadeService;
import io.github.yaforster.trails.core.TrailsTest;
import io.github.yaforster.trails.core.test.TestPlanRunDefinition;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.List;
import java.util.UUID;

import static org.instancio.Select.field;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.*;

class TestExecutionControllerTest extends TrailsTest {

	private final TestExecutionFacadeService testExecutionFacadeService = mock(TestExecutionFacadeService.class);

	private final TestRunDefinitionMapper definitionMapper = mock(TestRunDefinitionMapper.class);

	private final TestExecutionHATEOASFacade facade = mock(TestExecutionHATEOASFacade.class);

	private final TestExecutionEventHub eventHub = mock(TestExecutionEventHub.class);

	private final TestExecutionControllerValidator validator = mock(TestExecutionControllerValidator.class);

	private final TestExecutionController controller = new TestExecutionController(testExecutionFacadeService,
			definitionMapper, facade, eventHub, validator);

	@Test
	void runTest_ShouldMapDefinitionScheduleExecutionAndReturnAcceptedDto() {
		getInstancioOf(TestPlanRunDefinitionDTO.class).stream().limit(TEST_REPETITIONS).forEach(request -> {
			TestPlanRunDefinition runDefinition = runDefinition();
			TestExecutionAcceptedDTO acceptedDTO = acceptedDTO();

			when(definitionMapper.fromDTO(request)).thenReturn(runDefinition);
			when(facade.toAcceptedDTO(any(UUID.class))).thenReturn(acceptedDTO);

			ResponseEntity<TestExecutionAcceptedDTO> response = controller.runTest(request);

			ArgumentCaptor<UUID> uuidForFacade = ArgumentCaptor.forClass(UUID.class);
			ArgumentCaptor<TestExecutionFacadeService.TestExecution> testExecutionCaptor = ArgumentCaptor
				.forClass(TestExecutionFacadeService.TestExecution.class);
			verify(facade).toAcceptedDTO(uuidForFacade.capture());
			verify(testExecutionFacadeService).executeTest(testExecutionCaptor.capture());
			verify(validator).validateRunTest(request);

			assertEquals(HttpStatus.ACCEPTED, response.getStatusCode());
			assertSame(acceptedDTO, response.getBody());
			assertSame(uuidForFacade.getValue(), testExecutionCaptor.getValue().executionId());
			assertSame(runDefinition, testExecutionCaptor.getValue().runDefinition());

			clearInvocations(definitionMapper, facade, testExecutionFacadeService, validator);
		});
	}

	@Test
	void runTest_ShouldPassNullRunDefinitionThroughToService() {
		TestPlanRunDefinitionDTO request = getInstancioOf(TestPlanRunDefinitionDTO.class).create();
		TestExecutionAcceptedDTO acceptedDTO = new TestExecutionAcceptedDTO().status(TestExecutionStatusDTO.ACCEPTED);

		when(definitionMapper.fromDTO(request)).thenReturn(null);
		when(facade.toAcceptedDTO(any(UUID.class))).thenReturn(acceptedDTO);

		ResponseEntity<TestExecutionAcceptedDTO> response = controller.runTest(request);

		assertEquals(HttpStatus.ACCEPTED, response.getStatusCode());
		assertSame(acceptedDTO, response.getBody());
		verify(validator).validateRunTest(request);
		verify(testExecutionFacadeService)
			.executeTest(argThat(executionRequest -> executionRequest.runDefinition() == null));
	}

	@Test
	void streamTestExecutionEvents_ShouldDelegateToHub() {
		getInstancioOf(UUID.class).stream().limit(TEST_REPETITIONS).forEach(executionId -> {
			SseEmitter emitter = new SseEmitter();
			when(eventHub.subscribe(executionId)).thenReturn(emitter);

			SseEmitter result = controller.streamTestExecutionEvents(executionId);

			assertSame(emitter, result);
			verify(validator).validateStreamTestExecutionEvents(executionId);
			verify(eventHub).subscribe(executionId);
			clearInvocations(eventHub, validator);
		});
	}

	private TestPlanRunDefinition runDefinition() {
		return new TestPlanRunDefinition(getInstancioOf(Long.class).create(), List.of());
	}

	private TestExecutionAcceptedDTO acceptedDTO() {
		return getInstancioOf(TestExecutionAcceptedDTO.class)
			.set(field(TestExecutionAcceptedDTO::getStatus), TestExecutionStatusDTO.ACCEPTED)
			.create();
	}

}
