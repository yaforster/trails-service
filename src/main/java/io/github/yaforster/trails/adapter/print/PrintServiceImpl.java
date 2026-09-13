package io.github.yaforster.trails.adapter.print;

import com.google.common.annotations.VisibleForTesting;
import com.openhtmltopdf.pdfboxout.PdfRendererBuilder;
import io.github.yaforster.trails.app.services.ActionResultScreenshotQueryService;
import io.github.yaforster.trails.app.services.PrintService;
import io.github.yaforster.trails.core.persisted.PersistedActionResult;
import io.github.yaforster.trails.core.persisted.PersistedArtifactFile;
import io.github.yaforster.trails.core.print.TestPathPrintContext;
import io.github.yaforster.trails.core.print.TestRunPrintContext;
import io.github.yaforster.trails.core.print.TestSetPrintContext;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.io.IOException;
import java.io.OutputStream;
import java.util.*;
import java.util.stream.Collectors;

@AllArgsConstructor
@Service
public class PrintServiceImpl implements PrintService {

	private static final int PRINT_QUERY_BATCH_SIZE = 100;

	private static final String TEST_PATH_TEMPLATE_NAME = "print_test_path_template";

	private static final String TEST_SET_TEMPLATE_NAME = "print_test_set_template";

	private static final String TEST_RUN_TEMPLATE_NAME = "print_test_run_template";

	private final TemplateEngine templateEngine;

	private final ActionResultScreenshotQueryService actionResultScreenshotQueryService;

	@VisibleForTesting
	protected void writePdfToStream(String html, OutputStream outputStream) throws IOException {
		PdfRendererBuilder builder = new PdfRendererBuilder();
		builder.withHtmlContent(html, null);
		builder.toStream(outputStream);
		builder.run();
	}

	@Override
	public void renderTestPathPdf(TestPathPdf testPathPdf) throws IOException {
		String html = renderTestPathHtml(testPathPdf.testPathPrintContext());
		renderHtmlToPdf(html, testPathPdf.outputStream());
	}

	@Override
	public void renderTestSetPdf(TestSetPdf testSetPdf) throws IOException {
		String html = renderTestSetHtml(testSetPdf.testSetPrintContext());
		renderHtmlToPdf(html, testSetPdf.outputStream());
	}

	@Override
	public void renderTestRunPdf(TestRunPdf testRunPdf) throws IOException {
		String html = renderTestRunHtml(testRunPdf.testRunPrintContext());
		renderHtmlToPdf(html, testRunPdf.outputStream());
	}

	@VisibleForTesting
	protected String renderTestPathHtml(TestPathPrintContext testPathPrintContext) {
		Context context = new Context();
		context.setVariable("context", testPathPrintContext);
		context.setVariable("actionResultScreenshotDataUris",
				resolveActionResultScreenshotDataUris(testPathPrintContext));
		return templateEngine.process(TEST_PATH_TEMPLATE_NAME, context);
	}

	@VisibleForTesting
	protected String renderTestSetHtml(TestSetPrintContext testSetPrintContext) {
		Context context = new Context();
		context.setVariable("context", testSetPrintContext);
		context.setVariable("actionResultScreenshotDataUrisByPathId",
				resolveActionResultScreenshotDataUrisByPathId(testSetPrintContext));
		return templateEngine.process(TEST_SET_TEMPLATE_NAME, context);
	}

	@VisibleForTesting
	protected String renderTestRunHtml(TestRunPrintContext testRunPrintContext) {
		Context context = new Context();
		context.setVariable("context", testRunPrintContext);
		context.setVariable("actionResultScreenshotDataUrisByPathId",
				resolveActionResultScreenshotDataUrisByPathId(testRunPrintContext));
		return templateEngine.process(TEST_RUN_TEMPLATE_NAME, context);
	}

	@VisibleForTesting
	protected void renderHtmlToPdf(String html, OutputStream outputStream) throws IOException {
		try {
			writePdfToStream(html, outputStream);
		}
		catch (Exception e) {
			throw new PrintException("Failed to render PDF", e);
		}
	}

	@VisibleForTesting
	protected Map<Long, String> resolveActionResultScreenshotDataUris(TestPathPrintContext testPathPrintContext) {
		if (testPathPrintContext == null || testPathPrintContext.actionResults() == null
				|| testPathPrintContext.pathResultId() == null) {
			return Map.of();
		}
		return toScreenshotDataUris(testPathPrintContext.actionResults());
	}

	@VisibleForTesting
	protected Map<Long, Map<Long, String>> resolveActionResultScreenshotDataUrisByPathId(
			TestSetPrintContext testSetPrintContext) {
		if (testSetPrintContext == null || testSetPrintContext.pathPrintContexts() == null) {
			return Map.of();
		}
		Map<Long, PersistedArtifactFile> screenshotsByActionResultId = findScreenshots(
				testSetPrintContext.pathPrintContexts());
		return testSetPrintContext.pathPrintContexts()
			.stream()
			.filter(pathContext -> pathContext != null && pathContext.pathResultId() != null)
			.collect(Collectors.toMap(TestPathPrintContext::pathResultId,
					pathContext -> toScreenshotDataUris(pathContext.actionResults(), screenshotsByActionResultId),
					(left, _) -> left, LinkedHashMap::new));
	}

	@VisibleForTesting
	protected Map<Long, Map<Long, String>> resolveActionResultScreenshotDataUrisByPathId(
			TestRunPrintContext testRunPrintContext) {
		if (testRunPrintContext == null || testRunPrintContext.testSetPrintContexts() == null) {
			return Map.of();
		}
		List<TestPathPrintContext> pathPrintContexts = testRunPrintContext.testSetPrintContexts()
			.stream()
			.filter(Objects::nonNull)
			.flatMap(testSetPrintContext -> testSetPrintContext.pathPrintContexts().stream())
			.toList();
		Map<Long, PersistedArtifactFile> screenshotsByActionResultId = findScreenshots(pathPrintContexts);
		return pathPrintContexts.stream()
			.filter(pathContext -> pathContext != null && pathContext.pathResultId() != null)
			.collect(Collectors.toMap(TestPathPrintContext::pathResultId,
					pathContext -> toScreenshotDataUris(pathContext.actionResults(), screenshotsByActionResultId),
					(left, _) -> left, LinkedHashMap::new));
	}

	private Map<Long, PersistedArtifactFile> findScreenshots(List<TestPathPrintContext> pathPrintContexts) {
		List<Long> actionResultIds = pathPrintContexts.stream()
			.filter(Objects::nonNull)
			.map(TestPathPrintContext::actionResults)
			.filter(Objects::nonNull)
			.flatMap(List::stream)
			.map(PersistedActionResult::id)
			.filter(Objects::nonNull)
			.distinct()
			.toList();
		return loadScreenshots(actionResultIds);
	}

	private Map<Long, PersistedArtifactFile> loadScreenshots(List<Long> actionResultIds) {
		Map<Long, PersistedArtifactFile> screenshotsByActionResultId = new LinkedHashMap<>();
		for (int offset = 0; offset < actionResultIds.size(); offset += PRINT_QUERY_BATCH_SIZE) {
			int end = Math.min(offset + PRINT_QUERY_BATCH_SIZE, actionResultIds.size());
			screenshotsByActionResultId.putAll(actionResultScreenshotQueryService.findAll(
					new ActionResultScreenshotQueryService.ActionScreenshots(actionResultIds.subList(offset, end))));
		}
		return screenshotsByActionResultId;
	}

	private Map<Long, String> toScreenshotDataUris(List<PersistedActionResult> actionResults) {
		if (actionResults == null) {
			return Map.of();
		}
		Map<Long, PersistedArtifactFile> screenshotsByActionResultId = loadScreenshots(
				actionResults.stream().map(PersistedActionResult::id).filter(Objects::nonNull).distinct().toList());
		return toScreenshotDataUris(actionResults, screenshotsByActionResultId);
	}

	private Map<Long, String> toScreenshotDataUris(List<PersistedActionResult> actionResults,
			Map<Long, PersistedArtifactFile> screenshotsByActionResultId) {
		if (actionResults == null) {
			return Map.of();
		}
		return actionResults.stream()
			.map(PersistedActionResult::id)
			.filter(Objects::nonNull)
			.distinct()
			.map(actionResultId -> Optional.ofNullable(screenshotsByActionResultId.get(actionResultId))
				.map(this::toImageDataUri)
				.filter(Optional::isPresent)
				.map(Optional::get)
				.map(dataUri -> Map.entry(actionResultId, dataUri)))
			.flatMap(Optional::stream)
			.collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (left, _) -> left, LinkedHashMap::new));
	}

	@VisibleForTesting
	protected Optional<String> toImageDataUri(PersistedArtifactFile screenshotFile) {
		if (screenshotFile == null || screenshotFile.content() == null || screenshotFile.content().length == 0) {
			return Optional.empty();
		}
		String contentType = screenshotFile.contentType();
		if (contentType == null || contentType.isBlank()) {
			contentType = "image/png";
		}
		String base64 = Base64.getEncoder().encodeToString(screenshotFile.content());
		return Optional.of("data:" + contentType + ";base64," + base64);
	}

}
