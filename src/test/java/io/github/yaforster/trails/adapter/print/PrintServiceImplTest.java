package io.github.yaforster.trails.adapter.print;

import com.openhtmltopdf.util.XRRuntimeException;
import io.github.yaforster.trails.app.services.ActionResultScreenshotQueryService;
import io.github.yaforster.trails.app.services.PrintService;
import io.github.yaforster.trails.core.TrailsTest;
import io.github.yaforster.trails.core.persisted.ActionResultType;
import io.github.yaforster.trails.core.persisted.PersistedActionResult;
import io.github.yaforster.trails.core.persisted.PersistedArtifactFile;
import io.github.yaforster.trails.core.print.TestPathPrintContext;
import io.github.yaforster.trails.core.print.TestRunPrintContext;
import io.github.yaforster.trails.core.print.TestSetPrintContext;
import io.github.yaforster.trails.core.test.Browser;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.sql.Timestamp;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class PrintServiceImplTest extends TrailsTest {

	private final TemplateEngine templateEngine = mock(TemplateEngine.class);

	private final ActionResultScreenshotQueryService artifactQueryService = mock(
			ActionResultScreenshotQueryService.class);

	@Test
	void renderTestPathPdf_shouldComposeHtmlAndPdfRendering() throws IOException {
		TestPathPrintContext TestPathPrintContext = getInstancioOf(TestPathPrintContext.class).create();
		String html = getInstancioOf(String.class).create();
		byte[] expectedPdf = getInstancioOf(byte[].class).create();
		ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
		PrintServiceImpl service = spy(new PrintServiceImpl(templateEngine, artifactQueryService));

		doReturn(html).when(service).renderTestPathHtml(same(TestPathPrintContext));
		doAnswer(invocation -> {
			invocation.getArgument(1, ByteArrayOutputStream.class).write(expectedPdf);
			return null;
		}).when(service).renderHtmlToPdf(same(html), same(outputStream));

		service.renderTestPathPdf(new PrintService.TestPathPdf(TestPathPrintContext, outputStream));

		assertArrayEquals(expectedPdf, outputStream.toByteArray());
		verify(service).renderTestPathHtml(TestPathPrintContext);
		verify(service).renderHtmlToPdf(html, outputStream);
	}

	@Test
	void renderTestPathHtml_shouldBindContextAndCallTemplateEngine() {
		PersistedActionResult actionResult = new PersistedActionResult(101L, 10L, 21L, "label", "message",
				ActionResultType.SUCCESS, null);
		TestPathPrintContext TestPathPrintContext = new TestPathPrintContext(10L, 5L, "path", Browser.CHROME,
				List.of(actionResult));
		String renderedHtml = getInstancioOf(String.class).create();
		PersistedArtifactFile screenshotFile = new PersistedArtifactFile(101L, "action-result-101.png", "image/png",
				new byte[] { 1, 2, 3 });
		when(templateEngine.process(eq("print_test_path_template"), any(Context.class))).thenReturn(renderedHtml);
		when(artifactQueryService.findAll(new ActionResultScreenshotQueryService.ActionScreenshots(List.of(101L))))
			.thenReturn(Map.of(101L, screenshotFile));
		PrintServiceImpl service = new PrintServiceImpl(templateEngine, artifactQueryService);

		String result = service.renderTestPathHtml(TestPathPrintContext);

		assertEquals(renderedHtml, result);
		ArgumentCaptor<Context> contextCaptor = ArgumentCaptor.forClass(Context.class);
		verify(templateEngine).process(eq("print_test_path_template"), contextCaptor.capture());
		assertSame(TestPathPrintContext, contextCaptor.getValue().getVariable("context"));
		Object screenshotMap = contextCaptor.getValue().getVariable("actionResultScreenshotDataUris");
		assertInstanceOf(Map.class, screenshotMap);
		Map<?, ?> screenshotDataUris = (Map<?, ?>) screenshotMap;
		assertEquals(1, screenshotDataUris.size());
		assertTrue(String.valueOf(screenshotDataUris.get(101L)).startsWith("data:image/png;base64,"));
	}

	@Test
	void renderTestSetPdf_shouldComposeHtmlAndPdfRendering() throws IOException {
		TestSetPrintContext TestPathPrintContext = getInstancioOf(TestSetPrintContext.class).create();
		String html = getInstancioOf(String.class).create();
		byte[] expectedPdf = getInstancioOf(byte[].class).create();
		ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
		PrintServiceImpl service = spy(new PrintServiceImpl(templateEngine, artifactQueryService));

		doReturn(html).when(service).renderTestSetHtml(same(TestPathPrintContext));
		doAnswer(invocation -> {
			invocation.getArgument(1, ByteArrayOutputStream.class).write(expectedPdf);
			return null;
		}).when(service).renderHtmlToPdf(same(html), same(outputStream));

		service.renderTestSetPdf(new PrintService.TestSetPdf(TestPathPrintContext, outputStream));

		assertArrayEquals(expectedPdf, outputStream.toByteArray());
		verify(service).renderTestSetHtml(TestPathPrintContext);
		verify(service).renderHtmlToPdf(html, outputStream);
	}

	@Test
	void renderTestSetHtml_shouldBindContextAndCallTemplateEngine() {
		TestSetPrintContext TestPathPrintContext = getInstancioOf(TestSetPrintContext.class).create();
		String renderedHtml = getInstancioOf(String.class).create();
		when(templateEngine.process(eq("print_test_set_template"), any(Context.class))).thenReturn(renderedHtml);
		PrintServiceImpl service = new PrintServiceImpl(templateEngine, artifactQueryService);

		String result = service.renderTestSetHtml(TestPathPrintContext);

		assertEquals(renderedHtml, result);
		ArgumentCaptor<Context> contextCaptor = ArgumentCaptor.forClass(Context.class);
		verify(templateEngine).process(eq("print_test_set_template"), contextCaptor.capture());
		assertSame(TestPathPrintContext, contextCaptor.getValue().getVariable("context"));
	}

	@Test
	void renderTestRunPdf_shouldComposeHtmlAndPdfRendering() throws IOException {
		TestRunPrintContext TestPathPrintContext = getInstancioOf(TestRunPrintContext.class).create();
		String html = getInstancioOf(String.class).create();
		byte[] expectedPdf = getInstancioOf(byte[].class).create();
		ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
		PrintServiceImpl service = spy(new PrintServiceImpl(templateEngine, artifactQueryService));

		doReturn(html).when(service).renderTestRunHtml(same(TestPathPrintContext));
		doAnswer(invocation -> {
			invocation.getArgument(1, ByteArrayOutputStream.class).write(expectedPdf);
			return null;
		}).when(service).renderHtmlToPdf(same(html), same(outputStream));

		service.renderTestRunPdf(new PrintService.TestRunPdf(TestPathPrintContext, outputStream));

		assertArrayEquals(expectedPdf, outputStream.toByteArray());
		verify(service).renderTestRunHtml(TestPathPrintContext);
		verify(service).renderHtmlToPdf(html, outputStream);
	}

	@Test
	void renderTestRunHtml_shouldBindContextAndCallTemplateEngine() {
		PersistedActionResult actionResult = new PersistedActionResult(501L, 41L, 21L, "label", "message",
				ActionResultType.SUCCESS, null);
		TestPathPrintContext pathContext = new TestPathPrintContext(41L, 31L, "path", Browser.CHROME,
				List.of(actionResult));
		TestSetPrintContext testSetContext = new TestSetPrintContext(31L, 11L, 1L, 2L, Browser.CHROME,
				List.of(pathContext));
		TestRunPrintContext TestPathPrintContext = new TestRunPrintContext(11L, 1L, 2L, 99L,
				Timestamp.valueOf("2026-01-01 00:00:00"),
				getInstancioOf(io.github.yaforster.trails.core.data.ResultIndicator.class).create(), "run",
				List.of(testSetContext));
		String renderedHtml = getInstancioOf(String.class).create();
		PersistedArtifactFile screenshotFile = new PersistedArtifactFile(501L, "action-result-501.png", "image/png",
				new byte[] { 9, 8, 7 });
		when(templateEngine.process(eq("print_test_run_template"), any(Context.class))).thenReturn(renderedHtml);
		when(artifactQueryService.findAll(new ActionResultScreenshotQueryService.ActionScreenshots(List.of(501L))))
			.thenReturn(Map.of(501L, screenshotFile));
		PrintServiceImpl service = new PrintServiceImpl(templateEngine, artifactQueryService);

		String result = service.renderTestRunHtml(TestPathPrintContext);

		assertEquals(renderedHtml, result);
		ArgumentCaptor<Context> contextCaptor = ArgumentCaptor.forClass(Context.class);
		verify(templateEngine).process(eq("print_test_run_template"), contextCaptor.capture());
		assertSame(TestPathPrintContext, contextCaptor.getValue().getVariable("context"));
		Object screenshotMap = contextCaptor.getValue().getVariable("actionResultScreenshotDataUrisByPathId");
		assertInstanceOf(Map.class, screenshotMap);
		Map<?, ?> screenshotDataUrisByPathId = (Map<?, ?>) screenshotMap;
		assertTrue(screenshotDataUrisByPathId.containsKey(41L));
		Object screenshotDataUrisForPath = screenshotDataUrisByPathId.get(41L);
		assertInstanceOf(Map.class, screenshotDataUrisForPath);
		assertTrue(
				String.valueOf(((Map<?, ?>) screenshotDataUrisForPath).get(501L)).startsWith("data:image/png;base64,"));
	}

	@Test
	void writePdfToStream_shouldRenderHtmlToPdfStream() throws IOException {
		String html = """
				<!DOCTYPE html>
				<html>
				<head><meta charset="UTF-8"/></head>
				<body><p>pdf</p></body>
				</html>
				""";
		ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
		PrintServiceImpl service = new PrintServiceImpl(templateEngine, artifactQueryService);

		service.writePdfToStream(html, outputStream);
		byte[] result = outputStream.toByteArray();

		assertNotNull(result);
		assertTrue(result.length > 0, "Rendered PDF should not be empty");
	}

	@Test
	void writePdfToStream_shouldThrowRuntimeExceptionForMalformedMarkup() {
		String html = getInstancioOf(String.class).create();
		ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
		PrintServiceImpl service = new PrintServiceImpl(templateEngine, artifactQueryService);

		assertThrows(XRRuntimeException.class, () -> service.writePdfToStream(html, outputStream));
	}

	@Test
	void resolveActionResultScreenshotDataUris_shouldOnlyIncludeFoundScreenshots() {
		PersistedActionResult first = new PersistedActionResult(101L, 10L, 1L, "a", "m", ActionResultType.SUCCESS,
				null);
		PersistedActionResult second = new PersistedActionResult(102L, 10L, 2L, "b", "m", ActionResultType.SUCCESS,
				null);
		TestPathPrintContext TestPathPrintContext = new TestPathPrintContext(10L, 5L, "path", Browser.CHROME,
				List.of(first, second));
		PersistedArtifactFile screenshot = new PersistedArtifactFile(101L, "action-result-101.png", "image/png",
				new byte[] { 1, 2 });
		when(artifactQueryService
			.findAll(new ActionResultScreenshotQueryService.ActionScreenshots(List.of(101L, 102L))))
			.thenReturn(Map.of(101L, screenshot));
		PrintServiceImpl service = new PrintServiceImpl(templateEngine, artifactQueryService);

		Map<Long, String> result = service.resolveActionResultScreenshotDataUris(TestPathPrintContext);

		assertEquals(1, result.size());
		assertTrue(result.containsKey(101L));
		assertTrue(result.get(101L).startsWith("data:image/png;base64,"));
		assertFalse(result.containsKey(102L));
	}

	@Test
	void resolveActionResultScreenshotDataUrisByPathId_shouldGroupByPathForTestSetContext() {
		PersistedActionResult firstAction = new PersistedActionResult(201L, 20L, 1L, "a", "m", ActionResultType.SUCCESS,
				null);
		PersistedActionResult secondAction = new PersistedActionResult(202L, 21L, 2L, "b", "m",
				ActionResultType.SUCCESS, null);
		TestPathPrintContext firstPath = new TestPathPrintContext(20L, 5L, "path-a", Browser.CHROME,
				List.of(firstAction));
		TestPathPrintContext secondPath = new TestPathPrintContext(21L, 5L, "path-b", Browser.CHROME,
				List.of(secondAction));
		TestSetPrintContext testSetContext = new TestSetPrintContext(5L, 2L, 1L, 1L, Browser.CHROME,
				List.of(firstPath, secondPath));
		when(artifactQueryService
			.findAll(new ActionResultScreenshotQueryService.ActionScreenshots(List.of(201L, 202L))))
			.thenReturn(Map.of(201L, new PersistedArtifactFile(201L, "201.png", "image/png", new byte[] { 1 }), 202L,
					new PersistedArtifactFile(202L, "202.png", "image/png", new byte[] { 2 })));
		PrintServiceImpl service = new PrintServiceImpl(templateEngine, artifactQueryService);

		Map<Long, Map<Long, String>> result = service.resolveActionResultScreenshotDataUrisByPathId(testSetContext);

		assertEquals(2, result.size());
		assertTrue(result.containsKey(20L));
		assertTrue(result.containsKey(21L));
		assertTrue(result.get(20L).get(201L).startsWith("data:image/png;base64,"));
		assertTrue(result.get(21L).get(202L).startsWith("data:image/png;base64,"));
	}

	@Test
	void resolveActionResultScreenshotDataUrisByPathId_shouldFlattenAcrossTestSetsForRunContext() {
		PersistedActionResult action = new PersistedActionResult(301L, 30L, 1L, "a", "m", ActionResultType.SUCCESS,
				null);
		TestPathPrintContext path = new TestPathPrintContext(30L, 7L, "path", Browser.CHROME, List.of(action));
		TestSetPrintContext testSetContext = new TestSetPrintContext(7L, 3L, 1L, 1L, Browser.CHROME, List.of(path));
		TestRunPrintContext runContext = new TestRunPrintContext(3L, 1L, 1L, 10L,
				Timestamp.valueOf("2026-01-01 00:00:00"),
				getInstancioOf(io.github.yaforster.trails.core.data.ResultIndicator.class).create(), "run",
				List.of(testSetContext));
		when(artifactQueryService.findAll(new ActionResultScreenshotQueryService.ActionScreenshots(List.of(301L))))
			.thenReturn(Map.of(301L, new PersistedArtifactFile(301L, "301.png", "image/png", new byte[] { 3 })));
		PrintServiceImpl service = new PrintServiceImpl(templateEngine, artifactQueryService);

		Map<Long, Map<Long, String>> result = service.resolveActionResultScreenshotDataUrisByPathId(runContext);

		assertEquals(1, result.size());
		assertTrue(result.containsKey(30L));
		assertTrue(result.get(30L).get(301L).startsWith("data:image/png;base64,"));
	}

	@Test
	void toImageDataUri_shouldFallbackToPngWhenContentTypeIsMissing() {
		PersistedArtifactFile screenshot = new PersistedArtifactFile(1L, "shot.png", " ", new byte[] { 1, 2 });
		PrintServiceImpl service = new PrintServiceImpl(templateEngine, artifactQueryService);

		Optional<String> result = service.toImageDataUri(screenshot);

		assertTrue(result.isPresent());
		assertTrue(result.get().startsWith("data:image/png;base64,"));
	}

}
