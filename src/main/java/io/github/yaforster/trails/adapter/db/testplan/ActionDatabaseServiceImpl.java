package io.github.yaforster.trails.adapter.db.testplan;

import io.github.yaforster.trails.adapter.db.shared.SpringPageMapper;
import io.github.yaforster.trails.adapter.db.testplan.entity.ActionEntity;
import io.github.yaforster.trails.adapter.db.testplan.mapper.PersistedActionMapper;
import io.github.yaforster.trails.adapter.db.testplan.repo.ActionRepository;
import io.github.yaforster.trails.adapter.db.testplan.repo.TestPlanRepository;
import io.github.yaforster.trails.app.services.ActionQueryService;
import io.github.yaforster.trails.core.PagedResult;
import io.github.yaforster.trails.core.persisted.PersistedAction;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.Optional;

@AllArgsConstructor
@Service
public class ActionDatabaseServiceImpl implements ActionQueryService {

	private final TestPlanRepository testPlanRepository;

	private final ActionRepository actionRepository;

	private final PersistedActionMapper persistedActionMapper;

	@Override
	public Optional<PagedResult<PersistedAction>> listActions(ActionPage actionPage) {
		boolean testPlanExists = testPlanRepository
			.findByApplicationIdAndStageIdAndIdAndRetiredFalse(actionPage.applicationId(), actionPage.stageId(),
					actionPage.testPlanId())
			.isPresent();
		if (!testPlanExists) {
			return Optional.empty();
		}
		Pageable pageable = PageRequest.of(actionPage.page(), actionPage.size());
		Page<ActionEntity> entities = actionRepository.findByTestPlanIdOrderByIdAsc(actionPage.testPlanId(), pageable);
		return Optional.of(SpringPageMapper.toPagedResult(entities.map(persistedActionMapper::toPersisted)));
	}

}
