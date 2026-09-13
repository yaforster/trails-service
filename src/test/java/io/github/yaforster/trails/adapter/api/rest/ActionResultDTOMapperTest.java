package io.github.yaforster.trails.adapter.api.rest;

import io.github.yaforster.trails.adapter.api.rest.actionresult.mapper.ActionResultDTOMapper;
import io.github.yaforster.trails.adapter.api.rest.actionresult.model.ActionResultModel;
import io.github.yaforster.trails.adapter.api.rest.model.ActionResultDTO;
import io.github.yaforster.trails.core.persisted.ActionResultType;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;
import org.springframework.hateoas.Link;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ActionResultDTOMapperTest {

	private final ActionResultDTOMapper mapper = Mappers.getMapper(ActionResultDTOMapper.class);

	@Test
	void mapsActionResultModelToDto() {
		ActionResultModel model = new ActionResultModel(1L, 2L, "label", "message", ActionResultType.SUCCESS, "ex");
		model.add(Link.of("/action-results/1", "self"));

		ActionResultDTO dto = mapper.toDTO(model);

		assertEquals(1L, dto.getId());
		assertEquals(2L, dto.getActionID());
		assertEquals("label", dto.getLabel());
		assertEquals("message", dto.getResultMessage());
		assertEquals(ActionResultDTO.ResultTypeEnum.SUCCESS, dto.getResultType());
		assertEquals("ex", dto.getExceptionMessageFromAction());
		assertEquals("/action-results/1", dto.getLinks().get("self").getHref());
	}

}
