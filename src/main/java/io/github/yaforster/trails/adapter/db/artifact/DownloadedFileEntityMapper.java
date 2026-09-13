package io.github.yaforster.trails.adapter.db.artifact;

import io.github.yaforster.trails.core.ArtifactType;
import io.github.yaforster.trails.core.persisted.PersistedArtifact;
import io.github.yaforster.trails.core.persisted.PersistedArtifactFile;
import org.springframework.stereotype.Component;

@Component
public class DownloadedFileEntityMapper {

	public PersistedArtifact toPersistedArtifact(DownloadedFileEntity file) {
		return new PersistedArtifact(file.getId(), file.getTestPathResultId(), ArtifactType.FILE, file.getFileName(),
				null, file.getContent() == null ? 0 : file.getContent().length, null);
	}

	public PersistedArtifactFile toPersistedArtifactFile(DownloadedFileEntity file) {
		return new PersistedArtifactFile(file.getId(), file.getFileName(), null, file.getContent());
	}

}
