package io.github.yaforster.trails.adapter.db.result.testpath;

import io.github.yaforster.trails.app.services.TestPathResultPersistence;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@AllArgsConstructor
@Service
public class TestPathResultPersistenceServiceImpl implements TestPathResultPersistence {

	private final TestPathResultRepository testPathResultRepository;

	@Override
	@Transactional
	public Long store(NamedTestPath testPath) {
		return testPathResultRepository
			.save(new TestPathResultEntity().setTestSetResultId(testPath.testSetResultId())
				.setPathLabel(testPath.pathLabel()))
			.getId();
	}

}
