package io.github.yaforster.trails.adapter.db.application;

import io.github.yaforster.trails.adapter.db.shared.SpringPageMapper;
import io.github.yaforster.trails.app.services.ApplicationDatabaseService;
import io.github.yaforster.trails.core.PagedResult;
import io.github.yaforster.trails.core.definition.ApplicationDefinition;
import io.github.yaforster.trails.core.deletion.DatabaseDeletionResult;
import io.github.yaforster.trails.core.deletion.DeletionFailure;
import io.github.yaforster.trails.core.deletion.DeletionNotFound;
import io.github.yaforster.trails.core.deletion.DeletionSuccess;
import io.github.yaforster.trails.core.persisted.PersistedApplication;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.Optional;

@AllArgsConstructor
@Service
public class ApplicationDatabaseServiceImpl implements ApplicationDatabaseService {

	private final ApplicationRepository applicationRepository;

	private final ApplicationEntityMapper applicationEntityMapper;

	@Override
	public PersistedApplication storeApplication(ApplicationDefinition applicationDefinition) {
		ApplicationEntity entity = applicationEntityMapper.toEntity(applicationDefinition);
		ApplicationEntity storedApplication = applicationRepository.save(entity);
		return applicationEntityMapper.toPersisted(storedApplication);
	}

	@Override
	public Optional<PersistedApplication> getApplication(ApplicationDetails applicationDetails) {
		Optional<ApplicationEntity> application = applicationDetails.includeRetired()
				? applicationRepository.findById(applicationDetails.id())
				: applicationRepository.findByIdAndRetiredFalse(applicationDetails.id());
		return application.map(applicationEntityMapper::toPersisted);
	}

	@Override
	public boolean existsByLabel(String label) {
		return applicationRepository.existsByLabel(label);
	}

	@Override
	public PagedResult<PersistedApplication> getApplications(ApplicationPage applicationPage) {
		Pageable pageable = PageRequest.of(applicationPage.page(), applicationPage.size());
		Page<ApplicationEntity> applications = applicationPage.includeRetired()
				? applicationRepository.findAll(pageable) : applicationRepository.findAllByRetiredFalse(pageable);
		return SpringPageMapper.toPagedResult(applications.map(applicationEntityMapper::toPersisted));
	}

	@Override
	public DatabaseDeletionResult deleteApplication(Long id) {
		Optional<ApplicationEntity> entity = applicationRepository.findById(id);
		if (entity.isEmpty()) {
			return new DeletionNotFound(id);
		}
		try {
			applicationRepository.save(entity.get().setRetired(true));
			return new DeletionSuccess(id);
		}
		catch (Exception exception) {
			return new DeletionFailure(id, exception);
		}
	}

	@Override
	public DatabaseDeletionResult restoreApplication(Long id) {
		Optional<ApplicationEntity> entity = applicationRepository.findById(id);
		if (entity.isEmpty()) {
			return new DeletionNotFound(id);
		}
		try {
			applicationRepository.save(entity.get().setRetired(false));
			return new DeletionSuccess(id);
		}
		catch (Exception exception) {
			return new DeletionFailure(id, exception);
		}
	}

}
