package io.github.yaforster.trails.adapter.api.rest.run;

import io.github.yaforster.trails.adapter.api.TestExecutionEventHub;
import io.github.yaforster.trails.adapter.api.rest.model.TestExecutionAcceptedDTO;
import io.github.yaforster.trails.adapter.api.rest.model.TestPlanRunDefinitionDTO;
import io.github.yaforster.trails.adapter.api.rest.testrun.mapper.TestRunDefinitionMapper;
import io.github.yaforster.trails.app.services.TestExecutionFacadeService;
import io.github.yaforster.trails.core.test.TestPlanRunDefinition;
import lombok.AllArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.UUID;

@AllArgsConstructor
@RestController
public class TestExecutionController {

	private final TestExecutionFacadeService testExecutionFacadeService;

	private final TestRunDefinitionMapper mapper;

	private final TestExecutionHATEOASFacade hateoasFacade;

	private final TestExecutionEventHub eventHub;

	private final TestExecutionControllerValidator validator;

	@PostMapping(value = "/api/test", consumes = MediaType.APPLICATION_JSON_VALUE,
			produces = MediaType.APPLICATION_JSON_VALUE)
	@PreAuthorize("@resourceAuthorization.canExecuteTests(authentication)")
	public ResponseEntity<TestExecutionAcceptedDTO> runTest(@RequestBody TestPlanRunDefinitionDTO dto) {
		validator.validateRunTest(dto);
		TestPlanRunDefinition runDefinition = mapper.fromDTO(dto);
		UUID executionId = UUID.randomUUID();
		TestExecutionAcceptedDTO acceptedDTO = hateoasFacade.toAcceptedDTO(executionId);

		testExecutionFacadeService
			.executeTest(new TestExecutionFacadeService.TestExecution(executionId, runDefinition));

		return ResponseEntity.accepted().body(acceptedDTO);
	}

	@GetMapping(value = "/api/test/events/{executionId}", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
	public SseEmitter streamTestExecutionEvents(@PathVariable UUID executionId) {
		validator.validateStreamTestExecutionEvents(executionId);
		return eventHub.subscribe(executionId);
	}

}
