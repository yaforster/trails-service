package io.github.yaforster.trails.adapter.api.rest.artifact.mapper;

import io.github.yaforster.trails.core.persisted.PersistedArtifactFile;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

@Component
public class ArtifactResponseMapper {

	public ResponseEntity<Resource> toResponse(PersistedArtifactFile file, MediaType contentType, boolean inline) {
		Resource resource = new ByteArrayResource(file.content());
		String dispositionType = inline ? "inline" : "attachment";
		return ResponseEntity.ok()
			.contentType(contentType)
			.contentLength(file.content().length)
			.header(HttpHeaders.CONTENT_DISPOSITION, dispositionType + "; filename=\"" + file.fileName() + "\"")
			.body(resource);
	}

}
