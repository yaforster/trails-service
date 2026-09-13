package io.github.yaforster.trails.adapter.api.rest.artifact;

import io.github.yaforster.trails.adapter.api.rest.artifact.mapper.ArtifactResponseMapper;
import io.github.yaforster.trails.adapter.api.rest.model.PagedArtifactDTO;
import io.github.yaforster.trails.app.services.ActionResultScreenshotQueryService;
import io.github.yaforster.trails.app.services.ArtifactQueryService;
import io.github.yaforster.trails.core.PagedResult;
import io.github.yaforster.trails.app.services.HierarchyValidationService;
import io.github.yaforster.trails.core.persisted.PersistedArtifact;
import lombok.AllArgsConstructor;
import org.openapitools.api.ArtifactApi;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

@AllArgsConstructor
@RestController
public class ArtifactController implements ArtifactApi {

	private final ArtifactQueryService artifactQueryService;

	private final ActionResultScreenshotQueryService actionResultScreenshotQueryService;

	private final HierarchyValidationService hierarchyValidationService;

	private final ArtifactHATEOASFacade hateoasFacade;

	private final ArtifactResponseMapper artifactResponseMapper;

	private final ArtifactControllerValidator validator;

	@Override
	public ResponseEntity<Resource> downloadArtifactInTestPath(Long applicationId, Long stageId, Long testRunId,
			Long testSetResultId, Long pathResultId, Long artifactId) {
		validator.validateDownloadArtifact(applicationId, stageId, testRunId, testSetResultId, pathResultId,
				artifactId);
		if (!hierarchyValidationService.pathBelongsToHierarchy(new HierarchyValidationService.TestPathHierarchy(
				applicationId, stageId, testRunId, testSetResultId, pathResultId))) {
			return ResponseEntity.notFound().build();
		}

		return artifactQueryService.findArtifactInPath(new ArtifactQueryService.PathArtifact(pathResultId, artifactId))
			.map(file -> artifactResponseMapper.toResponse(file, MediaType.APPLICATION_OCTET_STREAM, false))
			.orElse(ResponseEntity.notFound().build());
	}

	@Override
	public ResponseEntity<Resource> downloadActionResultScreenshotInTestPath(Long applicationId, Long stageId,
			Long testRunId, Long testSetResultId, Long pathResultId, Long actionResultId) {
		validator.validateDownloadActionResultScreenshot(applicationId, stageId, testRunId, testSetResultId,
				pathResultId, actionResultId);
		if (!hierarchyValidationService.pathBelongsToHierarchy(new HierarchyValidationService.TestPathHierarchy(
				applicationId, stageId, testRunId, testSetResultId, pathResultId))) {
			return ResponseEntity.notFound().build();
		}

		return actionResultScreenshotQueryService
			.findInPath(new ActionResultScreenshotQueryService.PathActionScreenshot(pathResultId, actionResultId))
			.map(file -> artifactResponseMapper.toResponse(file, MediaType.parseMediaType(file.contentType()), true))
			.orElse(ResponseEntity.notFound().build());
	}

	@Override
	public ResponseEntity<PagedArtifactDTO> listArtifactsInTestPath(Long applicationId, Long stageId, Long testRunId,
			Long testSetResultId, Long pathResultId, Integer page, Integer size) {
		validator.validateListArtifacts(applicationId, stageId, testRunId, testSetResultId, pathResultId, page, size);
		if (!hierarchyValidationService.pathBelongsToHierarchy(new HierarchyValidationService.TestPathHierarchy(
				applicationId, stageId, testRunId, testSetResultId, pathResultId))) {
			return ResponseEntity.notFound().build();
		}

		PagedResult<PersistedArtifact> pagedFiles = artifactQueryService
			.listArtifacts(new ArtifactQueryService.PathArtifactPage(pathResultId, page, size));
		PagedArtifactDTO dto = hateoasFacade.toPagedDTOInHierarchy(applicationId, stageId, testRunId, testSetResultId,
				pathResultId, pagedFiles);
		return ResponseEntity.ok(dto);
	}

}
