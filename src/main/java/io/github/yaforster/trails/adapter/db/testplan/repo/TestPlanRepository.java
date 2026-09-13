package io.github.yaforster.trails.adapter.db.testplan.repo;

import io.github.yaforster.trails.adapter.db.testplan.entity.TestPlanEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface TestPlanRepository extends JpaRepository<TestPlanEntity, Long> {

	Optional<TestPlanEntity> findByApplicationIdAndStageIdAndId(Long appId, Long stageId, Long id);

	Optional<TestPlanEntity> findByApplicationIdAndStageIdAndIdAndRetiredFalse(Long appId, Long stageId, Long id);

	Page<TestPlanEntity> findByApplicationIdAndStageId(Long applicationId, Long stageId, Pageable pageable);

	Page<TestPlanEntity> findByApplicationIdAndStageIdAndRetiredFalse(Long applicationId, Long stageId,
			Pageable pageable);

	boolean existsByApplicationIdAndStageId(Long applicationId, Long stageId);

	boolean existsByApplicationIdAndStageIdAndRetiredFalse(Long applicationId, Long stageId);

	boolean existsByApplicationIdAndStageIdAndLabel(Long applicationId, Long stageId, String label);

}
