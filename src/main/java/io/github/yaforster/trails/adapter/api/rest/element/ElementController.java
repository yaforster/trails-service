package io.github.yaforster.trails.adapter.api.rest.element;

import io.github.yaforster.trails.adapter.api.rest.common.mapper.ScreenshotFileRequestMapper;
import io.github.yaforster.trails.adapter.api.rest.element.assembler.PersistedElementContext;
import io.github.yaforster.trails.adapter.api.rest.model.*;
import io.github.yaforster.trails.app.services.ElementDatabaseService;
import io.github.yaforster.trails.app.services.ScreenshotDatabaseService;
import io.github.yaforster.trails.app.services.StageDatabaseService;
import io.github.yaforster.trails.core.PagedResult;
import io.github.yaforster.trails.core.data.TrailsScreenshotFile;
import io.github.yaforster.trails.core.definition.ElementDefinition;
import io.github.yaforster.trails.core.deletion.DatabaseDeletionResult;
import io.github.yaforster.trails.core.deletion.DeletionNotFound;
import io.github.yaforster.trails.core.persisted.PersistedElement;
import lombok.AllArgsConstructor;
import org.jspecify.annotations.Nullable;
import org.openapitools.api.ElementApi;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.net.URI;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@AllArgsConstructor
@RestController
public class ElementController implements ElementApi {

	private final ElementDatabaseService elementDatabaseService;

	private final ScreenshotDatabaseService screenshotDatabaseService;

	private final StageDatabaseService stageDatabaseService;

	private final ElementHATEOASFacade hateoasFacade;

	private final ScreenshotFileRequestMapper screenshotFileRequestMapper;

	private final ElementControllerValidator validator;

	@Override
	@PreAuthorize("@resourceAuthorization.canManageResources(authentication)")
	public ResponseEntity<PersistedElementDTO> createNewElement(Long applicationId, Long stageId,
			@Nullable ElementDefinitionDTO dto) {
		validator.validateCreateNewElement(applicationId, stageId, dto);
		ElementDefinition elementDefinition = hateoasFacade.toDomain(dto);
		PersistedElement persistedElement = elementDatabaseService
			.storeElement(new ElementDatabaseService.ElementCreation(applicationId, stageId, elementDefinition));
		PersistedElementDTO persistedElementDTO = hateoasFacade.toDTO(persistedElement);
		return ResponseEntity.ok(persistedElementDTO);
	}

	@Override
	@PreAuthorize("@resourceAuthorization.canManageResources(authentication)")
	public ResponseEntity<ElementDTO> updateElement(Long applicationId, Long stageId, Long elementId,
			@Nullable ElementDefinitionDTO dto) {
		validator.validateUpdateElement(applicationId, stageId, elementId, dto);
		ElementDefinition elementDefinition = hateoasFacade.toDomain(dto);
		return elementDatabaseService
			.updateElement(
					new ElementDatabaseService.ElementUpdate(applicationId, stageId, elementId, elementDefinition))
			.map(element -> {
				Optional<Long> screenshotId = screenshotDatabaseService.findIdByElementId(elementId);
				PersistedElementContext context = new PersistedElementContext(applicationId, stageId, element,
						screenshotId);
				return hateoasFacade.mapToElementDTO(context);
			})
			.map(ResponseEntity::ok)
			.orElse(ResponseEntity.notFound().build());
	}

	@Override
	@PreAuthorize("@resourceAuthorization.canManageResources(authentication)")
	public ResponseEntity<DatabaseDeletionResultDTO> deleteElement(Long applicationId, Long stageId, Long elementId) {
		validator.validateDeleteElement(applicationId, stageId, elementId);
		DatabaseDeletionResult deletionResult = elementDatabaseService
			.deleteElement(new ElementDatabaseService.ElementReference(applicationId, stageId, elementId));
		DatabaseDeletionResultDTO dto = hateoasFacade.toDTO(applicationId, stageId, deletionResult);
		return ResponseEntity.status(hateoasFacade.toHTTPStatus(deletionResult)).body(dto);
	}

	@Override
	@PreAuthorize("@resourceAuthorization.canManageResources(authentication)")
	public ResponseEntity<DatabaseDeletionResultDTO> restoreElement(Long applicationId, Long stageId, Long elementId) {
		validator.validateDeleteElement(applicationId, stageId, elementId);
		DatabaseDeletionResult deletionResult = elementDatabaseService
			.restoreElement(new ElementDatabaseService.ElementReference(applicationId, stageId, elementId));
		DatabaseDeletionResultDTO dto = hateoasFacade.toDTO(applicationId, stageId, deletionResult);
		return ResponseEntity.status(hateoasFacade.toHTTPStatus(deletionResult)).body(dto);
	}

	@Override
	@PreAuthorize("@resourceAuthorization.canManageResources(authentication)")
	public ResponseEntity<ElementDTO> promoteElement(Long applicationId, Long sourceStageId, Long elementId,
			Long targetStageId) {
		validator.validatePromoteElement(applicationId, sourceStageId, elementId, targetStageId);
		if (stageDatabaseService.getStage(new StageDatabaseService.StageDetails(applicationId, sourceStageId, false))
			.isEmpty()
				|| stageDatabaseService
					.getStage(new StageDatabaseService.StageDetails(applicationId, targetStageId, false))
					.isEmpty()) {
			return ResponseEntity.notFound().build();
		}
		return elementDatabaseService
			.promoteElement(
					new ElementDatabaseService.ElementPromotion(applicationId, sourceStageId, elementId, targetStageId))
			.map(promotedElement -> {
				Optional<Long> screenshotId = screenshotDatabaseService.findIdByElementId(promotedElement.id());
				PersistedElementContext context = new PersistedElementContext(applicationId, targetStageId,
						promotedElement, screenshotId);
				ElementDTO dto = hateoasFacade.mapToElementDTO(context);
				URI location = URI.create("/api/applications/" + applicationId + "/stages/" + targetStageId
						+ "/elements/" + promotedElement.id());
				return ResponseEntity.created(location).body(dto);
			})
			.orElse(ResponseEntity.notFound().build());
	}

	@Override
	@PreAuthorize("@resourceAuthorization.canManageResources(authentication)")
	public ResponseEntity<DatabaseDeletionResultDTO> deleteElementScreenshot(Long applicationId, Long stageId,
			Long elementId) {
		validator.validateDeleteElementScreenshot(applicationId, stageId, elementId);
		if (elementDatabaseService
			.getElement(new ElementDatabaseService.ElementDetails(applicationId, stageId, elementId, false))
			.isEmpty()) {
			DatabaseDeletionResult deletionResult = new DeletionNotFound(elementId);
			DatabaseDeletionResultDTO dto = hateoasFacade.toDTO(applicationId, stageId, deletionResult);
			return ResponseEntity.status(hateoasFacade.toHTTPStatus(deletionResult)).body(dto);
		}
		DatabaseDeletionResult deletionResult = screenshotDatabaseService.deleteElementScreenshot(elementId);
		DatabaseDeletionResultDTO dto = hateoasFacade.toDTO(applicationId, stageId, deletionResult);
		return ResponseEntity.status(hateoasFacade.toHTTPStatus(deletionResult)).body(dto);
	}

	@Override
	public ResponseEntity<ElementDTO> getElement(Long applicationId, Long stageId, Long elementId,
			boolean includeRetired) {
		validator.validateGetElement(applicationId, stageId, elementId);
		return elementDatabaseService
			.getElement(new ElementDatabaseService.ElementDetails(applicationId, stageId, elementId, includeRetired))
			.map(element -> {
				Optional<Long> screenshotId = screenshotDatabaseService.findIdByElementId(elementId);
				PersistedElementContext context = new PersistedElementContext(applicationId, stageId, element,
						screenshotId);
				return hateoasFacade.mapToElementDTO(context);
			})
			.map(ResponseEntity::ok)
			.orElse(ResponseEntity.notFound().build());
	}

	@Override
	public ResponseEntity<PagedElementDTO> listElements(Long applicationId, Long stageId, Integer page, Integer size,
			boolean includeRetired) {
		validator.validateListElements(applicationId, stageId, page, size);
		PagedResult<PersistedElement> elements = elementDatabaseService
			.getElements(new ElementDatabaseService.ElementPage(applicationId, stageId, page, size, includeRetired));
		List<Long> elementIds = elements.items().stream().map(PersistedElement::id).toList();
		Map<Long, Long> screenshotMap = screenshotDatabaseService
			.findAllIdsByElementIds(new ScreenshotDatabaseService.ElementScreenshotIds(elementIds));
		PagedResult<PersistedElementContext> contexts = elements.map(el -> new PersistedElementContext(applicationId,
				stageId, el, Optional.ofNullable(screenshotMap.get(el.id()))));
		PagedElementDTO dto = hateoasFacade.mapContextsToPagedElementDTO(applicationId, stageId, contexts);
		return ResponseEntity.ok(dto);
	}

	@Override
	@PreAuthorize("@resourceAuthorization.canManageResources(authentication)")
	public ResponseEntity<ArtifactDTO> uploadElementScreenshot(Long applicationId, Long stageId, Long elementId,
			MultipartFile file) {
		validator.validateUploadElementScreenshot(applicationId, stageId, elementId, file);
		TrailsScreenshotFile screenshotFile = screenshotFileRequestMapper.from(file);
		if (elementDatabaseService
			.getElement(new ElementDatabaseService.ElementDetails(applicationId, stageId, elementId, false))
			.isEmpty()) {
			return ResponseEntity.notFound().build();
		}
		screenshotDatabaseService
			.setScreenshot(new ScreenshotDatabaseService.ElementScreenshot(elementId, screenshotFile));
		Long screenshotId = screenshotDatabaseService.findIdByElementId(elementId).orElse(null);
		ArtifactDTO artifactDTO = hateoasFacade.toDTO(applicationId, stageId, elementId, screenshotId, screenshotFile);
		return ResponseEntity.ok(artifactDTO);
	}

	@Override
	public ResponseEntity<Resource> getElementScreenshot(Long applicationId, Long stageId, Long elementId) {
		validator.validateGetElementScreenshot(applicationId, stageId, elementId);
		if (elementDatabaseService
			.getElement(new ElementDatabaseService.ElementDetails(applicationId, stageId, elementId, false))
			.isEmpty()) {
			return ResponseEntity.notFound().build();
		}

		return screenshotDatabaseService.loadScreenshotByElementId(elementId)
			.map(screenshot -> ResponseEntity.ok()
				.contentType(MediaType.parseMediaType(screenshot.contentType()))
				.contentLength(screenshot.size())
				.header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + screenshot.fileName() + "\"")
				.<Resource>body(new ByteArrayResource(screenshot.content().bytes())))
			.orElse(ResponseEntity.notFound().build());
	}

}
