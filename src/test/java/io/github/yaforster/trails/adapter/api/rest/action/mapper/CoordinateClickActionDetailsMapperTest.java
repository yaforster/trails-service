package io.github.yaforster.trails.adapter.api.rest.action.mapper;

import io.github.yaforster.trails.adapter.api.rest.common.mapper.PositionMapper;
import io.github.yaforster.trails.adapter.api.rest.common.mapper.ValueComputationInstructionMapper;
import io.github.yaforster.trails.adapter.api.rest.model.ActionDefinitionDTO;
import io.github.yaforster.trails.adapter.api.rest.model.ActionDetailsTypeDTO;
import io.github.yaforster.trails.adapter.api.rest.model.CoordinateClickDetailsDTO;
import io.github.yaforster.trails.app.services.ElementDatabaseService;
import io.github.yaforster.trails.core.test.Position;
import io.github.yaforster.trails.core.test.action.browser.CoordinateClickAction;
import io.github.yaforster.trails.core.test.action.browser.ViewportCoordinates;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

class CoordinateClickActionDetailsMapperTest {

	private final ElementDatabaseService elementDatabaseService = Mockito.mock(ElementDatabaseService.class);

	private final PositionMapper positionMapper = Mockito.mock(PositionMapper.class);

	private final CoordinateClickActionDetailsMapper mapper = new CoordinateClickActionDetailsMapper();

	private final ActionDetailsMappingContext context = new ActionDetailsMappingContext(elementDatabaseService,
			positionMapper, Mockito.mock(ValueComputationInstructionMapper.class), 1L, 2L);

	@Test
	void toDTO_mapsCoordinatesWithoutElementLookup() {
		CoordinateClickAction action = CoordinateClickAction.builder()
			.actionID(7L)
			.label("coordinate")
			.viewportCoordinates(new ViewportCoordinates(12, 34))
			.build();

		CoordinateClickDetailsDTO dto = (CoordinateClickDetailsDTO) mapper.toDTO(action, context);

		assertEquals(ActionDetailsTypeDTO.COORDINATE_CLICK, dto.getDetailsType());
		assertEquals(12, dto.getxCoordinate());
		assertEquals(34, dto.getyCoordinate());
		verifyNoInteractions(elementDatabaseService);
	}

	@Test
	void fromDTO_mapsCoordinatesAndCommonActionFieldsWithoutElementLookup() {
		Position position = new Position(BigDecimal.ONE, BigDecimal.TEN);
		CoordinateClickDetailsDTO details = new CoordinateClickDetailsDTO(ActionDetailsTypeDTO.COORDINATE_CLICK, 12,
				34);
		ActionDefinitionDTO definition = new ActionDefinitionDTO();
		definition.setReferenceID(7L);
		definition.setLabel("coordinate");
		definition.setNextActions(List.of(8L));
		when(positionMapper.fromDTO(null)).thenReturn(position);

		CoordinateClickAction action = (CoordinateClickAction) mapper.fromDTO(definition, details, context);

		assertEquals(new ViewportCoordinates(12, 34), action.getViewportCoordinates());
		assertEquals(7L, action.getActionID());
		assertEquals("coordinate", action.getLabel());
		assertEquals(List.of(8L), action.getNextActions());
		assertEquals(position, action.getPosition());
		verifyNoInteractions(elementDatabaseService);
	}

}
