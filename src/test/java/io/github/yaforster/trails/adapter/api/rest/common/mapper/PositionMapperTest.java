package io.github.yaforster.trails.adapter.api.rest.common.mapper;

import io.github.yaforster.trails.adapter.api.rest.common.mapper.PositionMapper;
import io.github.yaforster.trails.adapter.api.rest.model.PositionDTO;
import io.github.yaforster.trails.core.test.Position;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

class PositionMapperTest {

	private final PositionMapper mapper = new PositionMapperImpl();

	@Test
	void toDTO_ShouldMapPositionToDto() {
		Position position = new Position(BigDecimal.valueOf(12.5), BigDecimal.valueOf(99.1));

		PositionDTO result = mapper.toDTO(position);

		assertNotNull(result);
		assertEquals(position.xCoordinatePixels(), result.getxCoordinate());
		assertEquals(position.yCoordinatePixels(), result.getyCoordinate());
	}

	@Test
	void toDTO_ShouldReturnNullForNullInput() {
		assertNull(mapper.toDTO(null));
	}

	@Test
	void toDTO_ShouldKeepNullCoordinateValues() {
		Position position = new Position(null, BigDecimal.valueOf(42));

		PositionDTO result = mapper.toDTO(position);

		assertNotNull(result);
		assertNull(result.getxCoordinate());
		assertEquals(BigDecimal.valueOf(42), result.getyCoordinate());
	}

	@Test
	void fromDTO_ShouldMapDtoToPosition() {
		PositionDTO dto = new PositionDTO();
		dto.setxCoordinate(BigDecimal.valueOf(10.25));
		dto.setyCoordinate(BigDecimal.valueOf(20.5));

		Position result = mapper.fromDTO(dto);

		assertNotNull(result);
		assertEquals(dto.getxCoordinate(), result.xCoordinatePixels());
		assertEquals(dto.getyCoordinate(), result.yCoordinatePixels());
	}

	@Test
	void fromDTO_ShouldReturnNullForNullInput() {
		assertNull(mapper.fromDTO(null));
	}

	@Test
	void fromDTO_ShouldKeepNullCoordinateValues() {
		PositionDTO dto = new PositionDTO();
		dto.setxCoordinate(BigDecimal.valueOf(77));
		dto.setyCoordinate(null);

		Position result = mapper.fromDTO(dto);

		assertNotNull(result);
		assertEquals(BigDecimal.valueOf(77), result.xCoordinatePixels());
		assertNull(result.yCoordinatePixels());
	}

}
