package io.github.yaforster.trails.adapter.api.rest;

import io.github.yaforster.trails.adapter.api.rest.model.BrowserDTO;
import io.github.yaforster.trails.adapter.api.rest.model.TestSetResultDTO;
import io.github.yaforster.trails.adapter.api.rest.testset.mapper.TestSetResultDTOMapper;
import io.github.yaforster.trails.adapter.api.rest.testset.model.TestCaseResultModel;
import io.github.yaforster.trails.adapter.api.rest.testset.model.TestSetResultModel;
import io.github.yaforster.trails.core.test.Browser;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;
import org.springframework.hateoas.Link;

import java.time.OffsetDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class TestSetResultDTOMapperTest {

	private final TestSetResultDTOMapper mapper = Mappers.getMapper(TestSetResultDTOMapper.class);

	@Test
	void mapsTestSetResultModelToDto() {
		OffsetDateTime timestamp = OffsetDateTime.parse("2026-01-01T10:15:30Z");
		TestCaseResultModel caseResult = new TestCaseResultModel(timestamp, Browser.EDGE);
		TestSetResultModel model = new TestSetResultModel(9L, 123, Browser.CHROME, "plan", Optional.of(caseResult));
		model.add(Link.of("/test-sets/9", "self"));

		TestSetResultDTO dto = mapper.toDTO(model);

		assertEquals(9L, dto.getId());
		assertEquals(123, dto.getTotalRunTime());
		assertEquals(BrowserDTO.CHROME, dto.getBrowserToRunIn());
		assertEquals("plan", dto.getTestPlanLabel());
		assertNotNull(dto.getTestCaseResult());
		assertEquals(timestamp, dto.getTestCaseResult().getTimestamp());
		assertEquals(BrowserDTO.EDGE, dto.getTestCaseResult().getTestedInBrowser());
		assertTrue(dto.getTestCaseResult().getLinks().isEmpty());
		assertEquals("/test-sets/9", dto.getLinks().get("self").getHref());
		assertNull(mapper.map(Optional.empty()));
	}

}
