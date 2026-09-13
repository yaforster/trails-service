package io.github.yaforster.trails.adapter.test.webdriver.document;

import io.github.yaforster.trails.core.test.FileData;
import io.github.yaforster.trails.core.test.action.document.PdfVisualProof;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PdfBoxDocumentExecutionTest {

	@Test
	void isPdf_identifiesPdfContentWithoutPdfExtension() throws Exception {
		PdfBoxDocumentExecution execution = new PdfBoxDocumentExecution();

		assertTrue(execution.isPdf(new FileData(pdf("Invoice total"), "download")));
	}

	@Test
	void extract_returnsPlainTextContentForNonPdfDownload() throws Exception {
		PdfBoxDocumentExecution execution = new PdfBoxDocumentExecution();

		assertEquals("Approved",
				execution.extract(new FileData("Approved".getBytes(StandardCharsets.UTF_8), "report.txt")).text());
	}

	@Test
	void extract_returnsPdfText() throws Exception {
		PdfBoxDocumentExecution execution = new PdfBoxDocumentExecution();

		assertEquals("Invoice total 42",
				execution.extract(new FileData(pdf("Invoice total 42"), "invoice.pdf")).text().trim());
	}

	@Test
	void renderPdfProof_returnsTheMatchingPageAndImage() throws Exception {
		PdfBoxDocumentExecution execution = new PdfBoxDocumentExecution();

		Optional<PdfVisualProof> proof = execution.renderPdfProof(new FileData(pdf("Invoice total 42"), "invoice.pdf"),
				"total 42", false);

		assertTrue(proof.orElseThrow().pageNumber() == 1 && !proof.orElseThrow().pageImageBase64().isBlank());
	}

	private static byte[] pdf(String text) throws Exception {
		try (PDDocument document = new PDDocument(); ByteArrayOutputStream output = new ByteArrayOutputStream()) {
			document.addPage(new PDPage());
			try (PDPageContentStream stream = new PDPageContentStream(document, document.getPage(0))) {
				stream.beginText();
				stream.setFont(PDType1Font.HELVETICA, 12);
				stream.newLineAtOffset(72, 720);
				stream.showText(text);
				stream.endText();
			}
			document.save(output);
			return output.toByteArray();
		}
	}

}
