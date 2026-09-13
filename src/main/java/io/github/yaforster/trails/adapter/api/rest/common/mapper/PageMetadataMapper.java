package io.github.yaforster.trails.adapter.api.rest.common.mapper;

import io.github.yaforster.trails.adapter.api.rest.model.PageMetadataDTO;
import org.springframework.hateoas.PagedModel;
import org.springframework.stereotype.Component;

@Component
public class PageMetadataMapper {

	public void map(PagedModel<?> model, PageMetadataDTO dto) {
		if (model == null || dto == null) {
			return;
		}

		map(model.getMetadata(), dto);
	}

	public void map(PagedModel.PageMetadata metadata, PageMetadataDTO dto) {
		if (metadata == null || dto == null) {
			return;
		}

		dto.setPage((int) metadata.getNumber());
		dto.setSize((int) metadata.getSize());
		dto.setTotalElements((int) metadata.getTotalElements());
		dto.setTotalPages((int) metadata.getTotalPages());
	}

}
