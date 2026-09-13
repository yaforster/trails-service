package io.github.yaforster.trails.adapter.db.result.testset;

import io.github.yaforster.trails.app.services.TestSetResultPersistence;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@AllArgsConstructor
@Service
public class TestSetResultPersistenceServiceImpl implements TestSetResultPersistence {

	private final TestSetResultRepository testSetResultRepository;

	private final TestSetResultEntityMapper testSetResultEntityMapper;

	@Override
	@Transactional
	public Long store(TestSetResultToStore testSetResult) {
		return testSetResultRepository
			.save(testSetResultEntityMapper.toEntity(testSetResult.testRunId(), testSetResult.testSetResult()))
			.getId();
	}

}
