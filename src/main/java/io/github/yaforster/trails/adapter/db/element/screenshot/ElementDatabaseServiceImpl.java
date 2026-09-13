package io.github.yaforster.trails.adapter.db.element.screenshot;

import io.github.yaforster.trails.adapter.db.shared.SpringPageMapper;
import io.github.yaforster.trails.app.services.ElementDatabaseService;
import io.github.yaforster.trails.core.EntityMissingException;
import io.github.yaforster.trails.core.MappingException;
import io.github.yaforster.trails.core.PagedResult;
import io.github.yaforster.trails.core.definition.ElementDefinition;
import io.github.yaforster.trails.core.deletion.DatabaseDeletionResult;
import io.github.yaforster.trails.core.deletion.DeletionFailure;
import io.github.yaforster.trails.core.deletion.DeletionNotFound;
import io.github.yaforster.trails.core.deletion.DeletionSuccess;
import io.github.yaforster.trails.core.persisted.PersistedElement;
import io.github.yaforster.trails.core.test.action.element.Locator;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.Optional;
import java.util.Set;

@AllArgsConstructor
@Service
public class ElementDatabaseServiceImpl implements ElementDatabaseService {

	private final ElementRepository repository;

	private final ScreenshotRepository screenshotRepository;

	private final ElementEntityMapper elementEntityMapper;

	@Override
	public PersistedElement storeElement(ElementCreation elementCreation) {
		ElementEntity entity = elementEntityMapper.toEntity(elementCreation.applicationId(), elementCreation.stageId(),
				elementCreation.elementDefinition());
		ElementEntity storedEntity = repository.save(entity);
		return elementEntityMapper.toPersisted(storedEntity);
	}

	@Override
	public Optional<PersistedElement> updateElement(ElementUpdate elementUpdate) {
		return repository
			.findByIdAndApplicationIdAndStageIdAndRetiredFalse(elementUpdate.elementId(), elementUpdate.applicationId(),
					elementUpdate.stageId())
			.map(entity -> elementEntityMapper.updateEntity(entity, elementUpdate.elementDefinition()))
			.map(repository::save)
			.map(elementEntityMapper::toPersisted);
	}

	@Override
	public Optional<PersistedElement> getElement(ElementDetails elementDetails) {
		Optional<ElementEntity> entity = elementDetails.includeRetired()
				? repository.findByIdAndApplicationIdAndStageId(elementDetails.elementId(),
						elementDetails.applicationId(), elementDetails.stageId())
				: repository.findByIdAndApplicationIdAndStageIdAndRetiredFalse(elementDetails.elementId(),
						elementDetails.applicationId(), elementDetails.stageId());
		return entity.map(elementEntityMapper::toPersisted);
	}

	@Override
	public DatabaseDeletionResult deleteElement(ElementReference elementReference) {
		Optional<ElementEntity> entity = repository.findByIdAndApplicationIdAndStageId(elementReference.elementId(),
				elementReference.applicationId(), elementReference.stageId());
		if (entity.isEmpty()) {
			return new DeletionNotFound(elementReference.elementId());
		}
		try {
			repository.save(entity.get().setRetired(true));
			return new DeletionSuccess(elementReference.elementId());
		}
		catch (Exception exception) {
			return new DeletionFailure(elementReference.elementId(), exception);
		}
	}

	@Override
	public DatabaseDeletionResult restoreElement(ElementReference elementReference) {
		Optional<ElementEntity> entity = repository.findByIdAndApplicationIdAndStageId(elementReference.elementId(),
				elementReference.applicationId(), elementReference.stageId());
		if (entity.isEmpty()) {
			return new DeletionNotFound(elementReference.elementId());
		}
		try {
			repository.save(entity.get().setRetired(false));
			return new DeletionSuccess(elementReference.elementId());
		}
		catch (Exception exception) {
			return new DeletionFailure(elementReference.elementId(), exception);
		}
	}

	@Override
	@Transactional
	public Optional<PersistedElement> promoteElement(ElementPromotion elementPromotion) {
		Optional<ElementEntity> sourceElement = repository.findByIdAndApplicationIdAndStageIdAndRetiredFalse(
				elementPromotion.elementId(), elementPromotion.applicationId(), elementPromotion.sourceStageId());
		if (sourceElement.isEmpty()) {
			return Optional.empty();
		}

		ElementEntity promotedElement = copyToStage(sourceElement.get(), elementPromotion.targetStageId());
		ElementEntity storedElement = repository.save(promotedElement);
		copyScreenshot(elementPromotion.elementId(), storedElement.getId());
		return Optional.of(elementEntityMapper.toPersisted(storedElement));
	}

	@Override
	public PagedResult<PersistedElement> getElements(ElementPage elementPage) {
		Pageable pageable = PageRequest.of(elementPage.page(), elementPage.size());
		Page<ElementEntity> entities = elementPage.includeRetired()
				? repository.findAllByApplicationIdAndStageId(elementPage.applicationId(), elementPage.stageId(),
						pageable)
				: repository.findAllByApplicationIdAndStageIdAndRetiredFalse(elementPage.applicationId(),
						elementPage.stageId(), pageable);
		return SpringPageMapper.toPagedResult(entities.map(elementEntityMapper::toPersisted));
	}

	@Override
	public boolean hasElements(StageReference stageReference) {
		return repository.existsByApplicationIdAndStageIdAndRetiredFalse(stageReference.applicationId(),
				stageReference.stageId());
	}

	@Override
	public Set<Long> findStageIdsWithElements(CandidateStages candidateStages) {
		if (candidateStages.stageIds().isEmpty()) {
			return Set.of();
		}
		return repository.findStageIdsWithActiveElements(candidateStages.applicationId(), candidateStages.stageIds());
	}

	@Override
	public boolean existsByLabel(ElementLabel elementLabel) {
		return repository.existsByApplicationIdAndStageIdAndLabel(elementLabel.applicationId(), elementLabel.stageId(),
				elementLabel.label());
	}

	@Override
	public boolean existsByLocatorString(ElementLocatorText locatorText) {
		return repository.existsByApplicationIdAndStageIdAndLocator(locatorText.applicationId(), locatorText.stageId(),
				locatorText.locatorString());
	}

	@Override
	public Long getElementIDByLocator(ElementLocator elementLocator) {
		Optional<ElementEntity> elementToActOn = repository.findByApplicationIdAndStageIdAndLocatorAndRetiredFalse(
				elementLocator.applicationId(), elementLocator.stageId(), elementLocator.locator());
		if (elementToActOn.isEmpty()) {
			throw new EntityMissingException(
					"No element with the given locator could not be found in the application stage");
		}
		return elementToActOn.get().getId();
	}

	@Override
	public Locator getElementLocator(Long elementID) {
		Optional<ElementEntity> elementToActOn = repository.findById(elementID);
		if (elementToActOn.isEmpty()) {
			throw new MappingException("Error when loading the action with ID" + elementID + "from the " + "database"
					+ "." + " There is no element to act on with the given ID in the database");
		}
		return new Locator(elementToActOn.get().getLocatorType(), elementToActOn.get().getLocator());

	}

	@Override
	public Optional<Long> findPromotionTargetElementId(ElementPromotionTarget elementPromotionTarget) {
		return repository.findById(elementPromotionTarget.sourceElementId())
			.flatMap(sourceElement -> repository
				.findByApplicationIdAndStageIdAndLocatorAndLocatorTypeAndTypeAndRetiredFalse(
						elementPromotionTarget.applicationId(), elementPromotionTarget.targetStageId(),
						sourceElement.getLocator(), sourceElement.getLocatorType(), sourceElement.getType()))
			.map(ElementEntity::getId);
	}

	private ElementEntity copyToStage(ElementEntity sourceElement, Long targetStageId) {
		return new ElementEntity().setApplicationId(sourceElement.getApplicationId())
			.setStageId(targetStageId)
			.setLabel(sourceElement.getLabel())
			.setLocator(sourceElement.getLocator())
			.setLocatorType(sourceElement.getLocatorType())
			.setType(sourceElement.getType())
			.setRetired(false);
	}

	private void copyScreenshot(Long sourceElementId, Long targetElementId) {
		screenshotRepository.findByElementId(sourceElementId)
			.map(screenshot -> new ElementScreenshotEntity().setElementId(targetElementId)
				.setFileName(screenshot.getFileName())
				.setContentType(screenshot.getContentType())
				.setContent(screenshot.getContent().clone()))
			.ifPresent(screenshotRepository::save);
	}

}
