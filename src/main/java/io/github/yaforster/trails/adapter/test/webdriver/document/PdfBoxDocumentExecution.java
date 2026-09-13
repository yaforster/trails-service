package io.github.yaforster.trails.adapter.test.webdriver.document;

import io.github.yaforster.trails.core.test.FileData;
import io.github.yaforster.trails.core.test.action.document.DocumentExecution;
import io.github.yaforster.trails.core.test.action.document.DocumentTextExtractionException;
import io.github.yaforster.trails.core.test.action.document.ExtractedDocumentContent;
import io.github.yaforster.trails.core.test.action.document.PdfVisualProof;
import org.apache.commons.lang3.StringUtils;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.rendering.ImageType;
import org.apache.pdfbox.rendering.PDFRenderer;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.pdfbox.text.TextPosition;
import org.springframework.stereotype.Component;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Component
public class PdfBoxDocumentExecution implements DocumentExecution {

	private static final byte[] PDF_HEADER = "%PDF".getBytes(StandardCharsets.US_ASCII);

	private static final float DPI = 150F;

	@Override
	public boolean isPdf(FileData file) {
		String fileName = StringUtils.defaultString(file.fileName()).toLowerCase();
		return fileName.endsWith(".pdf") || startsWith(file.content() == null ? new byte[0] : file.content());
	}

	@Override
	public ExtractedDocumentContent extract(FileData file) throws DocumentTextExtractionException {
		if (file.content() == null || file.content().length == 0) {
			throw new DocumentTextExtractionException("The downloaded document is empty.");
		}
		if (!isPdf(file)) {
			return new ExtractedDocumentContent(file.fileName(), new String(file.content(), StandardCharsets.UTF_8),
					"Plain text");
		}
		try (PDDocument document = PDDocument.load(file.content())) {
			ensureUnencrypted(document);
			return new ExtractedDocumentContent(file.fileName(), new PDFTextStripper().getText(document), "PDF");
		}
		catch (IOException exception) {
			throw new DocumentTextExtractionException("The PDF document could not be parsed.", exception);
		}
	}

	@Override
	public Optional<PdfVisualProof> renderPdfProof(FileData file, String expectedText, boolean caseSensitive)
			throws DocumentTextExtractionException {
		try (PDDocument document = PDDocument.load(file.content())) {
			ensureUnencrypted(document);
			Optional<Match> match = new TextMatchLocator(expectedText, caseSensitive).find(document);
			if (match.isEmpty()) {
				return Optional.empty();
			}
			BufferedImage image = new PDFRenderer(document).renderImageWithDPI(match.get().pageIndex(), DPI,
					ImageType.RGB);
			return Optional.of(new PdfVisualProof(file.fileName(), expectedText, match.get().pageIndex() + 1,
					Math.round(match.get().topY() * DPI / 72F), image.getWidth(), image.getHeight(),
					encodedPng(image)));
		}
		catch (IOException exception) {
			throw new DocumentTextExtractionException("The PDF document could not be parsed.", exception);
		}
	}

	private void ensureUnencrypted(PDDocument document) throws DocumentTextExtractionException {
		if (document.isEncrypted()) {
			throw new DocumentTextExtractionException("Encrypted PDF documents are not supported.");
		}
	}

	private boolean startsWith(byte[] content) {
		if (content.length < PDF_HEADER.length) {
			return false;
		}
		for (int index = 0; index < PDF_HEADER.length; index++) {
			if (content[index] != PDF_HEADER[index]) {
				return false;
			}
		}
		return true;
	}

	private String encodedPng(BufferedImage image) throws IOException {
		ByteArrayOutputStream output = new ByteArrayOutputStream();
		ImageIO.write(image, "png", output);
		return Base64.getEncoder().encodeToString(output.toByteArray());
	}

	private static class TextMatchLocator extends PDFTextStripper {

		private final String expectedText;

		private final boolean caseSensitive;

		private final Map<Integer, List<TextPosition>> positionsByPage = new LinkedHashMap<>();

		TextMatchLocator(String expectedText, boolean caseSensitive) throws IOException {
			this.expectedText = expectedText;
			this.caseSensitive = caseSensitive;
			setSortByPosition(true);
		}

		Optional<Match> find(PDDocument document) throws IOException {
			getText(document);
			if (StringUtils.isBlank(expectedText)) {
				return Optional.empty();
			}
			for (Map.Entry<Integer, List<TextPosition>> entry : positionsByPage.entrySet()) {
				Optional<Match> match = findOnPage(entry.getKey(), entry.getValue());
				if (match.isPresent()) {
					return match;
				}
			}
			return Optional.empty();
		}

		@Override
		protected void writeString(String text, List<TextPosition> positions) {
			positionsByPage.computeIfAbsent(getCurrentPageNo() - 1, ignored -> new ArrayList<>()).addAll(positions);
		}

		private Optional<Match> findOnPage(int pageIndex, List<TextPosition> positions) {
			List<IndexedCharacter> characters = indexedCharacters(positions);
			String text = characters.stream()
				.map(character -> String.valueOf(character.value()))
				.reduce("", String::concat);
			String expected = expectedText.chars()
				.filter(character -> !Character.isWhitespace(character))
				.mapToObj(character -> String.valueOf(searchable((char) character)))
				.reduce("", String::concat);
			int matchIndex = text.indexOf(expected);
			if (matchIndex < 0) {
				return Optional.empty();
			}
			float topY = characters.subList(matchIndex, matchIndex + expected.length())
				.stream()
				.map(character -> character.position().getYDirAdj() - character.position().getHeightDir())
				.min(Float::compare)
				.orElse(0F);
			return Optional.of(new Match(pageIndex, topY));
		}

		private List<IndexedCharacter> indexedCharacters(List<TextPosition> positions) {
			List<IndexedCharacter> characters = new ArrayList<>();
			for (TextPosition position : positions) {
				for (char character : position.getUnicode().toCharArray()) {
					if (!Character.isWhitespace(character)) {
						characters.add(new IndexedCharacter(searchable(character), position));
					}
				}
			}
			return characters;
		}

		private char searchable(char character) {
			return caseSensitive ? character : Character.toLowerCase(character);
		}

	}

	private record IndexedCharacter(char value, TextPosition position) {
	}

	private record Match(int pageIndex, float topY) {
	}

}
