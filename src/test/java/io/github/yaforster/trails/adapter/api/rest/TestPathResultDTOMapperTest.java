package io.github.yaforster.trails.adapter.api.rest;

import io.github.yaforster.trails.adapter.api.rest.model.TestPathResultDTO;
import io.github.yaforster.trails.adapter.api.rest.testpath.mapper.TestPathResultDTOMapper;
import io.github.yaforster.trails.adapter.api.rest.testpath.model.TestPathResultModel;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;
import org.springframework.hateoas.Link;

import static org.junit.jupiter.api.Assertions.assertEquals;

class TestPathResultDTOMapperTest {

	private final TestPathResultDTOMapper mapper = Mappers.getMapper(TestPathResultDTOMapper.class);

	@Test
	void mapsTestPathResultModelToDto() {
		TestPathResultModel model = new TestPathResultModel(44L);
		model.add(Link.of("/test-path-results/44", "self"));

		TestPathResultDTO dto = mapper.toDTO(model);

		assertEquals(44L, dto.getId());
		assertEquals("/test-path-results/44", dto.getLinks().get("self").getHref());
	}

}
