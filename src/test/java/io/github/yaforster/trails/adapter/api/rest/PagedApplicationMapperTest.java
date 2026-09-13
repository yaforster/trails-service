package io.github.yaforster.trails.adapter.api.rest;

import io.github.yaforster.trails.adapter.api.rest.common.mapper.PageMetadataMapper;
import io.github.yaforster.trails.adapter.api.rest.application.mapper.PagedApplicationMapper;
import io.github.yaforster.trails.adapter.api.rest.application.model.ApplicationModel;
import io.github.yaforster.trails.adapter.api.rest.model.PagedApplicationDTO;
import org.junit.jupiter.api.Test;
import org.springframework.hateoas.Link;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class PagedApplicationMapperTest {

	@Test
	void mapsPagedApplicationModelToDto() {
		ApplicationModel model = new ApplicationModel(9L, "app");
		model.add(Link.of("/applications/9", "self"));

		PagedApplicationMapper mapper = new PagedApplicationMapper(new PageMetadataMapper());
		PagedApplicationDTO dto = mapper.toPagedDto(RestMapperTestSupport.paged(List.of(model)));

		assertEquals(1, dto.getItems().size());
		assertEquals(9L, dto.getItems().getFirst().getId());
		assertEquals("app", dto.getItems().getFirst().getLabel());
		assertEquals("/applications/9", dto.getItems().getFirst().getLinks().get("self").getHref());
		assertEquals("/page/self", dto.getLinks().get("self").getHref());
	}

}
