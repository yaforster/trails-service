package io.github.yaforster.trails.adapter.db.deployment;

import io.github.yaforster.trails.core.definition.DeploymentDefinition;
import io.github.yaforster.trails.core.persisted.PersistedDeployment;
import org.springframework.stereotype.Component;

import java.sql.Timestamp;

@Component
public class DeploymentEntityMapper {

	public DeploymentEntity toEntity(Long applicationId, Long stageId, DeploymentDefinition definition) {
		return new DeploymentEntity().setApplicationId(applicationId)
			.setStageId(stageId)
			.setVersion(definition.version())
			.setDeployedAt(Timestamp.from(definition.deployedAt()));
	}

	public PersistedDeployment toPersisted(DeploymentEntity entity) {
		return new PersistedDeployment(entity.getId(), entity.getApplicationId(), entity.getStageId(),
				entity.getVersion(), entity.getDeployedAt().toInstant());
	}

}
