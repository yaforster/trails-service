package io.github.yaforster.trails.adapter.db.result.testset;

import io.github.yaforster.trails.adapter.db.shared.SpringPageMapper;
import io.github.yaforster.trails.app.services.TestRunResultDatabaseService;
import io.github.yaforster.trails.app.services.TestSetResultQueryService;
import io.github.yaforster.trails.core.PagedResult;
import io.github.yaforster.trails.core.persisted.PersistedTestSetResult;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.Optional;

@AllArgsConstructor
@Service
public class TestSetResultDatabaseServiceImpl implements TestSetResultQueryService {

	private final TestRunResultDatabaseService testRunResultDatabaseService;

	private final TestSetResultRepository testSetResultRepository;

	private final TestSetResultEntityMapper testSetResultEntityMapper;

	@Override
	public Optional<PagedResult<PersistedTestSetResult>> listBrowserResults(
			TestRunBrowserResults testRunBrowserResults) {
		if (testRunResultDatabaseService
			.getTestRunResult(
					new TestRunResultDatabaseService.TestRunResultDetails(testRunBrowserResults.applicationId(),
							testRunBrowserResults.stageId(), testRunBrowserResults.testRunId()))
			.isEmpty()) {
			return Optional.empty();
		}
		Pageable pageable = PageRequest.of(testRunBrowserResults.page(), testRunBrowserResults.size());
		return Optional.of(SpringPageMapper.toPagedResult(
				testSetResultRepository.findByTestRunIdOrderByIdAsc(testRunBrowserResults.testRunId(), pageable)
					.map(entity -> testSetResultEntityMapper.toPersisted(entity, testRunBrowserResults.applicationId(),
							testRunBrowserResults.stageId(), testRunBrowserResults.testRunId()))));
	}

	@Override
	public Optional<PersistedTestSetResult> getTestSetResult(Long testSetResultId) {
		return testSetResultRepository.findById(testSetResultId)
			.flatMap(testSetResult -> testRunResultDatabaseService
				.getTestRunResult(new TestRunResultDatabaseService.TestRunReference(testSetResult.getTestRunId()))
				.map(testRunResult -> testSetResultEntityMapper.toPersisted(testSetResult,
						testRunResult.applicationId(), testRunResult.stageId(), testRunResult.id())));
	}

}
