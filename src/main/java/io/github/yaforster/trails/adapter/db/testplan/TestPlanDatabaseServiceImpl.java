package io.github.yaforster.trails.adapter.db.testplan;

import io.github.yaforster.trails.adapter.db.shared.SpringPageMapper;
import io.github.yaforster.trails.adapter.db.testplan.entity.TestPlanEntity;
import io.github.yaforster.trails.adapter.db.testplan.mapper.TestPlanEntityMapper;
import io.github.yaforster.trails.adapter.db.testplan.repo.ActionRepository;
import io.github.yaforster.trails.adapter.db.testplan.repo.TestPlanRepository;
import io.github.yaforster.trails.app.services.TestDataValueResolutionService;
import io.github.yaforster.trails.app.services.TestPlanDatabaseService;
import io.github.yaforster.trails.core.PagedResult;
import io.github.yaforster.trails.core.data.TestPlan;
import io.github.yaforster.trails.core.definition.TestPlanDefinition;
import io.github.yaforster.trails.core.deletion.DatabaseDeletionResult;
import io.github.yaforster.trails.core.deletion.DeletionFailure;
import io.github.yaforster.trails.core.deletion.DeletionNotFound;
import io.github.yaforster.trails.core.deletion.DeletionSuccess;
import io.github.yaforster.trails.core.persisted.PersistedTestPlan;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.Optional;
import java.util.Set;

@AllArgsConstructor
@Service
public class TestPlanDatabaseServiceImpl implements TestPlanDatabaseService {

	private final TestPlanRepository testPlanRepository;

	private final ActionRepository actionRepository;

	private final TestPlanEntityMapper testPlanEntityMapper;

	private final TestDataValueResolutionService valueResolutionService;

	private final TestPlanActionReferenceResolver actionReferenceResolver;

	private final TestPlanPromotionService promotionService;

	@Override
	@Transactional
	public PersistedTestPlan storeTestPlan(TestPlanDefinition definition) {
		TestPlanEntity entity = testPlanEntityMapper.toEntity(definition);
		TestPlanEntity storedEntity = testPlanRepository.save(entity);

		actionReferenceResolver.resolveReferences(storedEntity);
		actionRepository.saveAll(storedEntity.getTestSteps());

		return testPlanEntityMapper.mapEntityToPersistedTestPlan(storedEntity);
	}

	@Override
	public DatabaseDeletionResult deleteTestPlan(TestPlanReference testPlanReference) {
		Optional<TestPlanEntity> entity = testPlanRepository.findByApplicationIdAndStageIdAndId(
				testPlanReference.applicationId(), testPlanReference.stageId(), testPlanReference.testPlanId());
		if (entity.isEmpty()) {
			return new DeletionNotFound(testPlanReference.testPlanId());
		}
		try {
			testPlanRepository.save(entity.get().setRetired(true));
			return new DeletionSuccess(testPlanReference.testPlanId());
		}
		catch (Exception e) {
			return new DeletionFailure(testPlanReference.testPlanId(), e);
		}
	}

	@Override
	public DatabaseDeletionResult restoreTestPlan(TestPlanReference testPlanReference) {
		Optional<TestPlanEntity> entity = testPlanRepository.findByApplicationIdAndStageIdAndId(
				testPlanReference.applicationId(), testPlanReference.stageId(), testPlanReference.testPlanId());
		if (entity.isEmpty()) {
			return new DeletionNotFound(testPlanReference.testPlanId());
		}
		try {
			testPlanRepository.save(entity.get().setRetired(false));
			return new DeletionSuccess(testPlanReference.testPlanId());
		}
		catch (Exception exception) {
			return new DeletionFailure(testPlanReference.testPlanId(), exception);
		}
	}

	@Override
	@Transactional
	public Optional<PersistedTestPlan> promoteTestPlan(TestPlanPromotion testPlanPromotion) {
		Optional<TestPlanEntity> source = testPlanRepository.findByApplicationIdAndStageIdAndIdAndRetiredFalse(
				testPlanPromotion.applicationId(), testPlanPromotion.sourceStageId(), testPlanPromotion.testPlanId());
		if (source.isEmpty()) {
			return Optional.empty();
		}
		Optional<TestPlanEntity> copy = promotionService.copyToStage(source.get(), testPlanPromotion.targetStageId());
		if (copy.isEmpty()) {
			return Optional.empty();
		}
		TestPlanEntity stored = testPlanRepository.save(copy.get());
		promotionService.resolveCopiedNextActions(source.get(), stored);
		actionRepository.saveAll(stored.getTestSteps());
		return Optional.of(testPlanEntityMapper.mapEntityToPersistedTestPlan(stored));
	}

	@Override
	@Transactional(readOnly = true)
	public Optional<TestPlan> getTestPlan(TestPlanReference testPlanReference) {
		Optional<TestPlanEntity> entity = testPlanRepository.findByApplicationIdAndStageIdAndId(
				testPlanReference.applicationId(), testPlanReference.stageId(), testPlanReference.testPlanId());
		return entity.map(testPlanEntityMapper::fromEntity);
	}

	@Override
	@Transactional(readOnly = true)
	public Optional<PersistedTestPlan> getPersistedTestPlan(PersistedTestPlanDetails persistedTestPlanDetails) {
		Optional<TestPlanEntity> entity = persistedTestPlanDetails.includeRetired()
				? testPlanRepository.findByApplicationIdAndStageIdAndId(persistedTestPlanDetails.applicationId(),
						persistedTestPlanDetails.stageId(), persistedTestPlanDetails.testPlanId())
				: testPlanRepository.findByApplicationIdAndStageIdAndIdAndRetiredFalse(
						persistedTestPlanDetails.applicationId(), persistedTestPlanDetails.stageId(),
						persistedTestPlanDetails.testPlanId());
		return entity.map(testPlanEntityMapper::mapEntityToPersistedTestPlan);
	}

	@Override
	public PagedResult<PersistedTestPlan> getTestPlans(TestPlanPage testPlanPage) {
		Pageable pageable = PageRequest.of(testPlanPage.page(), testPlanPage.size());
		Page<TestPlanEntity> testPlans = testPlanPage.includeRetired()
				? testPlanRepository.findByApplicationIdAndStageId(testPlanPage.applicationId(), testPlanPage.stageId(),
						pageable)
				: testPlanRepository.findByApplicationIdAndStageIdAndRetiredFalse(testPlanPage.applicationId(),
						testPlanPage.stageId(), pageable);
		return SpringPageMapper.toPagedResult(testPlans.map(testPlanEntityMapper::mapEntityToPersistedTestPlan));
	}

	@Override
	public boolean hasActions(Long testPlanId) {
		return actionRepository.existsByTestPlanId(testPlanId);
	}

	@Override
	public Set<Long> findTestPlanIdsWithActions(TestPlanActions testPlanActions) {
		if (testPlanActions.testPlanIds().isEmpty()) {
			return Set.of();
		}
		return actionRepository.findTestPlanIdsWithActions(testPlanActions.applicationId(), testPlanActions.stageId(),
				testPlanActions.testPlanIds());
	}

	@Override
	public boolean existsByLabel(TestPlanLabel testPlanLabel) {
		return testPlanRepository.existsByApplicationIdAndStageIdAndLabel(testPlanLabel.applicationId(),
				testPlanLabel.stageId(), testPlanLabel.label());
	}

	@Override
	@Transactional(readOnly = true)
	public Optional<TestPlan> getExecutableTestPlan(Long testPlanId) {
		Optional<TestPlanEntity> entity = testPlanRepository.findById(testPlanId);
		return entity.map(testPlan -> testPlanEntityMapper.fromEntity(testPlan,
				valueResolutionService::resolveTestDataReference));
	}

}
