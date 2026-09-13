package io.github.yaforster.trails.adapter.db.result.action;

import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class ResultScreenshotFactoryTest {

	private final ResultScreenshotFactory factory = new ResultScreenshotFactory();

	@Test
	void fromBase64_shouldDecodeDataUri() {
		Optional<ResultScreenshotEntity> result = factory.fromBase64(7L, "data:image/png;base64,AQID");

		assertTrue(result.isPresent());
		assertEquals(7L, result.get().getResultId());
		assertEquals("image/png", result.get().getContentType());
		assertEquals("action-result-7.png", result.get().getFileName());
		assertArrayEquals(new byte[] { 1, 2, 3 }, result.get().getContent());
	}

	@Test
	void fromBase64_shouldReturnEmpty_whenInputInvalid() {
		Optional<ResultScreenshotEntity> result = factory.fromBase64(7L, "%%%");

		assertTrue(result.isEmpty());
	}

}
