package io.github.yaforster.trails.adapter.db.result.testpath;

import io.github.yaforster.trails.adapter.db.shared.SpringPageMapper;
import io.github.yaforster.trails.app.services.TestPathResultQueryService;
import io.github.yaforster.trails.app.services.TestSetResultQueryService;
import io.github.yaforster.trails.core.PagedResult;
import io.github.yaforster.trails.core.persisted.PersistedTestPathResult;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.Optional;

@AllArgsConstructor
@Service
public class TestPathResultDatabaseServiceImpl implements TestPathResultQueryService {

	private final TestSetResultQueryService testSetResultQueryService;

	private final TestPathResultRepository testPathResultRepository;

	private final TestPathResultEntityMapper testPathResultEntityMapper;

	@Override
	public Optional<PagedResult<PersistedTestPathResult>> listPathResults(TestSetPathPage testSetPathPage) {
		if (testSetResultQueryService.getTestSetResult(testSetPathPage.testSetResultId()).isEmpty()) {
			return Optional.empty();
		}
		Pageable pageable = PageRequest.of(testSetPathPage.page(), testSetPathPage.size());
		return Optional.of(SpringPageMapper.toPagedResult(
				testPathResultRepository.findByTestSetResultIdOrderByIdAsc(testSetPathPage.testSetResultId(), pageable)
					.map(testPathResultEntityMapper::toPersisted)));
	}

	@Override
	public PagedResult<PersistedTestPathResult> listPathResults(TestSetPaths testSetPaths) {
		if (testSetPaths.testSetResultIds().isEmpty()) {
			return new PagedResult<>(java.util.List.of(), testSetPaths.page(), testSetPaths.size(), 0);
		}
		Pageable pageable = PageRequest.of(testSetPaths.page(), testSetPaths.size());
		return SpringPageMapper.toPagedResult(testPathResultRepository
			.findByTestSetResultIdInOrderByTestSetResultIdAscIdAsc(testSetPaths.testSetResultIds(), pageable)
			.map(testPathResultEntityMapper::toPersisted));
	}

	@Override
	public Optional<PersistedTestPathResult> getPathResult(Long pathResultId) {
		return testPathResultRepository.findById(pathResultId).map(testPathResultEntityMapper::toPersisted);
	}

}
