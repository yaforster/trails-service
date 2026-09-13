package io.github.yaforster.trails.adapter.db.testplan.mapper.detail.field;

import io.github.yaforster.trails.adapter.db.testplan.entity.ActionEntity;
import io.github.yaforster.trails.adapter.db.testplan.entity.details.DownloadedFileCheckDetailsEntity;
import io.github.yaforster.trails.adapter.db.testplan.mapper.PositionEntityMapper;
import io.github.yaforster.trails.core.test.action.download.CheckDownloadedFileAction;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DownloadedFileCheckDetailsMapper {

	private final PositionEntityMapper positionMapper;

	public DownloadedFileCheckDetailsEntity toEntity(CheckDownloadedFileAction action) {
		return new DownloadedFileCheckDetailsEntity(action.getFileName());
	}

	public CheckDownloadedFileAction fromEntity(ActionEntity entity, DownloadedFileCheckDetailsEntity details) {
		return CheckDownloadedFileAction.builder()
			.actionID(entity.getId())
			.label(entity.getLabel())
			.nextActions(entity.getNextActions())
			.fileName(details.getFileName())
			.position(positionMapper.fromEntity(entity.getPosition()))
			.build();
	}

}
