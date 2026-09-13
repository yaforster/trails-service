package io.github.yaforster.trails.adapter.api.rest;

import io.github.yaforster.trails.adapter.api.rest.common.mapper.PageMetadataMapper;
import io.github.yaforster.trails.adapter.api.rest.element.mapper.PagedElementMapper;
import io.github.yaforster.trails.adapter.api.rest.element.model.ElementModel;
import io.github.yaforster.trails.adapter.api.rest.model.PagedElementDTO;
import io.github.yaforster.trails.core.data.ElementType;
import io.github.yaforster.trails.core.test.action.element.LocatorType;
import org.junit.jupiter.api.Test;
import org.springframework.hateoas.Link;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class PagedElementMapperTest {

	@Test
	void mapsPagedElementModelToDto() {
		ElementModel model = new ElementModel(5L, ElementType.BUTTON, "button", "#btn", LocatorType.CSS);
		model.add(Link.of("/elements/5", "self"));

		PagedElementMapper mapper = new PagedElementMapper(new PageMetadataMapper());
		PagedElementDTO dto = mapper.toPagedDto(RestMapperTestSupport.paged(List.of(model)));

		assertEquals(1, dto.getItems().size());
		assertEquals(5L, dto.getItems().getFirst().getId());
		assertEquals("button", dto.getItems().getFirst().getLabel());
		assertEquals("/elements/5", dto.getItems().getFirst().getLinks().get("self").getHref());
		assertEquals("/page/self", dto.getLinks().get("self").getHref());
	}

}
