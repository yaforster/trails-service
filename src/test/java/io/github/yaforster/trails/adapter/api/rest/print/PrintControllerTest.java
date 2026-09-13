package io.github.yaforster.trails.adapter.api.rest.print;

import io.github.yaforster.trails.app.services.PrintFacadeService;
import io.github.yaforster.trails.core.GeneratedFile;
import io.github.yaforster.trails.core.TrailsTest;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

import java.io.IOException;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class PrintControllerTest extends TrailsTest {

	private final PrintFacadeService printFacadeService = mock(PrintFacadeService.class);

	private final PrintResponseMapper responseMapper = new PrintResponseMapper();

	private final PrintControllerValidator validator = mock(PrintControllerValidator.class);

	private final PrintController controller = new PrintController(printFacadeService, responseMapper, validator);

	@Test
	void printPathResultInTestSet_shouldDelegateAndReturnPdfResponse() throws IOException {
		Long applicationId = randomId();
		Long stageId = randomId();
		Long testRunId = randomId();
		Long testSetResultId = randomId();
		Long pathResultId = randomId();
		byte[] pdf = getInstancioOf(byte[].class).create();
		when(printFacadeService.printPath(
				new PrintFacadeService.TestPathPrint(applicationId, stageId, testRunId, testSetResultId, pathResultId)))
			.thenReturn(Optional.of(new GeneratedFile(pdf, "test-path-" + pathResultId + ".pdf")));

		ResponseEntity<Resource> response = controller.printPathResultInTestSet(applicationId, stageId, testRunId,
				testSetResultId, pathResultId);

		assertEquals(HttpStatus.OK, response.getStatusCode());
		assertEquals(MediaType.APPLICATION_PDF, response.getHeaders().getContentType());
		assertEquals(-1, response.getHeaders().getContentLength());
		assertEquals("inline; filename=\"test-path-" + pathResultId + ".pdf\"",
				response.getHeaders().getFirst(HttpHeaders.CONTENT_DISPOSITION));
		assertNotNull(response.getBody());
		assertArrayEquals(pdf, response.getBody().getContentAsByteArray());
		verify(validator).validatePrintPathResultInTestSet(applicationId, stageId, testRunId, testSetResultId,
				pathResultId);
		verify(printFacadeService).printPath(
				new PrintFacadeService.TestPathPrint(applicationId, stageId, testRunId, testSetResultId, pathResultId));
	}

	@Test
	void printTestSetResultInTestRun_shouldReturnNotFound_whenFacadeReturnsEmpty() {
		Long applicationId = randomId();
		Long stageId = randomId();
		Long testRunId = randomId();
		Long testSetResultId = randomId();
		when(printFacadeService
			.printTestSet(new PrintFacadeService.TestSetPrint(applicationId, stageId, testRunId, testSetResultId)))
			.thenReturn(Optional.empty());

		ResponseEntity<Resource> response = controller.printTestSetResultInTestRun(applicationId, stageId, testRunId,
				testSetResultId);

		assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
		verify(validator).validatePrintTestSetResultInTestRun(applicationId, stageId, testRunId, testSetResultId);
		verify(printFacadeService)
			.printTestSet(new PrintFacadeService.TestSetPrint(applicationId, stageId, testRunId, testSetResultId));
	}

	@Test
	void printTestRun_shouldDelegateAndReturnPdfResponse() throws IOException {
		Long applicationId = randomId();
		Long stageId = randomId();
		Long testRunId = randomId();
		byte[] pdf = getInstancioOf(byte[].class).create();
		when(printFacadeService.printTestRun(new PrintFacadeService.TestRunPrint(applicationId, stageId, testRunId)))
			.thenReturn(Optional.of(new GeneratedFile(pdf, "test-run-" + testRunId + ".pdf")));

		ResponseEntity<Resource> response = controller.printTestRun(applicationId, stageId, testRunId);

		assertEquals(HttpStatus.OK, response.getStatusCode());
		assertEquals(MediaType.APPLICATION_PDF, response.getHeaders().getContentType());
		assertEquals(-1, response.getHeaders().getContentLength());
		assertEquals("inline; filename=\"test-run-" + testRunId + ".pdf\"",
				response.getHeaders().getFirst(HttpHeaders.CONTENT_DISPOSITION));
		assertNotNull(response.getBody());
		assertArrayEquals(pdf, response.getBody().getContentAsByteArray());
		verify(validator).validatePrintTestRun(applicationId, stageId, testRunId);
		verify(printFacadeService).printTestRun(new PrintFacadeService.TestRunPrint(applicationId, stageId, testRunId));
	}

	private Long randomId() {
		Long value = getInstancioOf(Long.class).create();
		if (value == Long.MIN_VALUE) {
			return 1L;
		}
		return Math.abs(value) + 1L;
	}

}
