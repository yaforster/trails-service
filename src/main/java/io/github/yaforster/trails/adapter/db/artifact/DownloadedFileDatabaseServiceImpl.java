package io.github.yaforster.trails.adapter.db.artifact;

import io.github.yaforster.trails.adapter.db.shared.SpringPageMapper;
import io.github.yaforster.trails.app.services.ArtifactQueryService;
import io.github.yaforster.trails.app.services.DownloadedFilePersistence;
import io.github.yaforster.trails.core.PagedResult;
import io.github.yaforster.trails.core.persisted.PersistedArtifact;
import io.github.yaforster.trails.core.persisted.PersistedArtifactFile;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.Optional;

@AllArgsConstructor
@Service
public class DownloadedFileDatabaseServiceImpl implements ArtifactQueryService, DownloadedFilePersistence {

	private final DownloadedFileRepository repository;

	private final DownloadedFileEntityMapper downloadedFileEntityMapper;

	@Override
	public PagedResult<PersistedArtifact> listArtifacts(PathArtifactPage pathArtifactPage) {
		Pageable pageable = PageRequest.of(pathArtifactPage.page(), pathArtifactPage.size());
		return SpringPageMapper
			.toPagedResult(repository.findByTestPathResultIdOrderByIdAsc(pathArtifactPage.testPathResultId(), pageable)
				.map(downloadedFileEntityMapper::toPersistedArtifact));
	}

	@Override
	public Optional<PersistedArtifactFile> findArtifactInPath(PathArtifact pathArtifact) {
		return repository.findByIdAndTestPathResultId(pathArtifact.artifactId(), pathArtifact.pathResultId())
			.map(downloadedFileEntityMapper::toPersistedArtifactFile);
	}

	@Override
	public void store(DownloadedFile downloadedFile) {
		DownloadedFileEntity fileEntity = new DownloadedFileEntity()
			.setTestPathResultId(downloadedFile.testPathResultId())
			.setFileName(downloadedFile.file().fileName())
			.setContent(downloadedFile.file().content());
		repository.save(fileEntity);
	}

}
