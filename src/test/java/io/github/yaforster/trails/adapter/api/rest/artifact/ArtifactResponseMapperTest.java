package io.github.yaforster.trails.adapter.api.rest.artifact;

import io.github.yaforster.trails.adapter.api.rest.artifact.mapper.ArtifactResponseMapper;
import io.github.yaforster.trails.core.persisted.PersistedArtifactFile;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.*;

class ArtifactResponseMapperTest {

	private final ArtifactResponseMapper mapper = new ArtifactResponseMapper();

	@Test
	void toResponse_shouldBuildAttachmentResponse() throws Exception {
		PersistedArtifactFile file = new PersistedArtifactFile(1L, "report.txt", "text/plain", new byte[] { 1, 2, 3 });

		ResponseEntity<Resource> response = mapper.toResponse(file, MediaType.TEXT_PLAIN, false);

		assertEquals(MediaType.TEXT_PLAIN, response.getHeaders().getContentType());
		assertEquals(3, response.getHeaders().getContentLength());
		assertEquals("attachment; filename=\"report.txt\"",
				response.getHeaders().getFirst(HttpHeaders.CONTENT_DISPOSITION));
		assertNotNull(response.getBody());
		assertArrayEquals(new byte[] { 1, 2, 3 }, response.getBody().getContentAsByteArray());
	}

}
