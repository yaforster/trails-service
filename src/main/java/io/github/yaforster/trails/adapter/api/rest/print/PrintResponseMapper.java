package io.github.yaforster.trails.adapter.api.rest.print;

import io.github.yaforster.trails.core.GeneratedFile;
import lombok.extern.log4j.Log4j2;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;

@Component
@Log4j2
public class PrintResponseMapper {

	public ResponseEntity<Resource> toPdfResponse(GeneratedFile printPdfResult) {
		PipedInputStream inputStream = new PipedInputStream(64 * 1024);
		startPdfWriter(printPdfResult, inputStream);
		Resource resource = new InputStreamResource(inputStream);
		return ResponseEntity.ok()
			.contentType(MediaType.APPLICATION_PDF)
			.header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + printPdfResult.fileName() + "\"")
			.body(resource);
	}

	private void startPdfWriter(GeneratedFile printPdfResult, PipedInputStream inputStream) {
		Thread.startVirtualThread(() -> {
			try (PipedOutputStream outputStream = new PipedOutputStream(inputStream)) {
				printPdfResult.writeTo(outputStream);
			}
			catch (IOException | RuntimeException exception) {
				log.warn("PDF rendering terminated before the response completed", exception);
			}
		});
	}

}
