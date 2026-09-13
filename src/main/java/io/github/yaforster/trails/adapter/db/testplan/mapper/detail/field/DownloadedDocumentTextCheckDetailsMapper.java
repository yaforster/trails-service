package io.github.yaforster.trails.adapter.db.testplan.mapper.detail.field;

import io.github.yaforster.trails.adapter.db.testplan.entity.ActionEntity;
import io.github.yaforster.trails.adapter.db.testplan.entity.details.DownloadedDocumentTextCheckDetailsEntity;
import io.github.yaforster.trails.adapter.db.testplan.mapper.PositionEntityMapper;
import io.github.yaforster.trails.core.test.action.document.CheckDownloadedDocumentTextAction;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DownloadedDocumentTextCheckDetailsMapper {

	private final PositionEntityMapper positionMapper;

	public DownloadedDocumentTextCheckDetailsEntity toEntity(CheckDownloadedDocumentTextAction action) {
		return new DownloadedDocumentTextCheckDetailsEntity(action.getFileName(), action.getExpectedText(),
				action.isCaseSensitive());
	}

	public CheckDownloadedDocumentTextAction fromEntity(ActionEntity entity,
			DownloadedDocumentTextCheckDetailsEntity details) {
		return CheckDownloadedDocumentTextAction.builder()
			.actionID(entity.getId())
			.label(entity.getLabel())
			.nextActions(entity.getNextActions())
			.fileName(details.getFileName())
			.expectedText(details.getExpectedText())
			.caseSensitive(Boolean.TRUE.equals(details.getCaseSensitive()))
			.position(positionMapper.fromEntity(entity.getPosition()))
			.build();
	}

}
