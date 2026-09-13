package io.github.yaforster.trails.adapter.db.deployment;

import io.github.yaforster.trails.adapter.db.shared.SpringPageMapper;
import io.github.yaforster.trails.app.services.DeploymentDatabaseService;
import io.github.yaforster.trails.app.services.StageDatabaseService;
import io.github.yaforster.trails.core.PagedResult;
import io.github.yaforster.trails.core.definition.DeploymentDefinition;
import io.github.yaforster.trails.core.persisted.PersistedDeployment;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.Optional;

@AllArgsConstructor
@Service
public class DeploymentDatabaseServiceImpl implements DeploymentDatabaseService {

	private final DeploymentRepository deploymentRepository;

	private final StageDatabaseService stageDatabaseService;

	private final DeploymentEntityMapper mapper;

	@Override
	public Optional<PersistedDeployment> storeDeployment(StageDeployment stageDeployment) {
		if (stageDatabaseService
			.getStage(new StageDatabaseService.StageDetails(stageDeployment.applicationId(), stageDeployment.stageId(),
					true))
			.isEmpty()) {
			return Optional.empty();
		}

		DeploymentEntity stored = deploymentRepository.save(mapper.toEntity(stageDeployment.applicationId(),
				stageDeployment.stageId(), stageDeployment.definition()));
		return Optional.of(mapper.toPersisted(stored));
	}

	@Override
	public Optional<PersistedDeployment> getDeployment(DeploymentDetails deploymentDetails) {
		return deploymentRepository
			.findByApplicationIdAndStageIdAndId(deploymentDetails.applicationId(), deploymentDetails.stageId(),
					deploymentDetails.deploymentId())
			.map(mapper::toPersisted);
	}

	@Override
	public Optional<PagedResult<PersistedDeployment>> listDeployments(StageDeploymentPage deploymentPage) {
		if (stageDatabaseService
			.getStage(new StageDatabaseService.StageDetails(deploymentPage.applicationId(), deploymentPage.stageId(),
					true))
			.isEmpty()) {
			return Optional.empty();
		}

		Pageable pageable = PageRequest.of(deploymentPage.page(), deploymentPage.size());
		return Optional.of(SpringPageMapper.toPagedResult(deploymentRepository
			.findByApplicationIdAndStageIdOrderByDeployedAtAscIdAsc(deploymentPage.applicationId(),
					deploymentPage.stageId(), pageable)
			.map(mapper::toPersisted)));
	}

}
