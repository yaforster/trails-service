package io.github.yaforster.trails.adapter.db.result.action;

import io.github.yaforster.trails.app.services.ActionResultPersistence;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@AllArgsConstructor
@Service
public class ActionResultPersistenceServiceImpl implements ActionResultPersistence {

	private final ResultRepository resultRepository;

	private final ResultScreenshotRepository resultScreenshotRepository;

	private final ResultScreenshotFactory resultScreenshotFactory;

	private final ResultEntityMapper resultEntityMapper;

	@Override
	@Transactional
	public void store(ActionResult actionResult) {
		ResultEntity storedResult = resultRepository.save(resultEntityMapper.toEntity(actionResult.testPathResultId(),
				actionResult.executionOrder(), actionResult.result()));
		resultScreenshotFactory.fromBase64(storedResult.getId(), actionResult.result().getBase64Screenshot())
			.ifPresent(resultScreenshotRepository::save);
	}

}
