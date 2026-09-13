package io.github.yaforster.trails.adapter.api.rest.action.mapper;

import io.github.yaforster.trails.adapter.api.rest.common.mapper.PositionMapper;
import io.github.yaforster.trails.adapter.api.rest.common.mapper.ValueComputationInstructionMapper;
import io.github.yaforster.trails.adapter.api.rest.model.ActionDefinitionDTO;
import io.github.yaforster.trails.adapter.api.rest.model.ActionDetailsTypeDTO;
import io.github.yaforster.trails.adapter.api.rest.model.ResizeViewportDetailsDTO;
import io.github.yaforster.trails.app.services.ElementDatabaseService;
import io.github.yaforster.trails.core.test.Position;
import io.github.yaforster.trails.core.test.action.browser.ResizeViewportAction;
import io.github.yaforster.trails.core.test.action.browser.viewport.ViewportDimensions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

class ResizeViewportActionDetailsMapperTest {

	private final ElementDatabaseService elementDatabaseService = Mockito.mock(ElementDatabaseService.class);

	private final PositionMapper positionMapper = Mockito.mock(PositionMapper.class);

	private final ResizeViewportActionDetailsMapper mapper = new ResizeViewportActionDetailsMapper();

	private final ActionDetailsMappingContext context = new ActionDetailsMappingContext(elementDatabaseService,
			positionMapper, Mockito.mock(ValueComputationInstructionMapper.class), 1L, 2L);

	@Test
	void toDTO_mapsDimensionsWithoutElementLookup() {
		ResizeViewportAction action = ResizeViewportAction.builder()
			.viewportDimensions(new ViewportDimensions(800, 600))
			.build();

		ResizeViewportDetailsDTO dto = (ResizeViewportDetailsDTO) mapper.toDTO(action, context);

		assertEquals(ActionDetailsTypeDTO.RESIZE_VIEWPORT, dto.getDetailsType());
		assertEquals(800, dto.getViewportWidth());
		assertEquals(600, dto.getViewportHeight());
		verifyNoInteractions(elementDatabaseService);
	}

	@Test
	void fromDTO_mapsDimensionsAndCommonActionFieldsWithoutElementLookup() {
		Position position = new Position(BigDecimal.ONE, BigDecimal.TEN);
		ResizeViewportDetailsDTO details = new ResizeViewportDetailsDTO(ActionDetailsTypeDTO.RESIZE_VIEWPORT, 800, 600);
		ActionDefinitionDTO definition = new ActionDefinitionDTO();
		definition.setReferenceID(7L);
		definition.setLabel("resize");
		definition.setNextActions(List.of(8L));
		when(positionMapper.fromDTO(null)).thenReturn(position);

		ResizeViewportAction action = (ResizeViewportAction) mapper.fromDTO(definition, details, context);

		assertEquals(new ViewportDimensions(800, 600), action.getViewportDimensions());
		assertEquals(7L, action.getActionID());
		assertEquals("resize", action.getLabel());
		assertEquals(List.of(8L), action.getNextActions());
		assertEquals(position, action.getPosition());
		verifyNoInteractions(elementDatabaseService);
	}

}
