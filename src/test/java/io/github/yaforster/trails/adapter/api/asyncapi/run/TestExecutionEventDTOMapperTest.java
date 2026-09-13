package io.github.yaforster.trails.adapter.api.asyncapi.run;

import io.github.yaforster.trails.adapter.api.asyncapi.model.LinkAsyncDTO;
import io.github.yaforster.trails.adapter.api.asyncapi.model.TestExecutionStartedEventAsyncDTO;
import io.github.yaforster.trails.adapter.api.asyncapi.run.mapper.TestExecutionEventDTOMapper;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.Links;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class TestExecutionEventDTOMapperTest {

	private final TestExecutionEventDTOMapper mapper = Mappers.getMapper(TestExecutionEventDTOMapper.class);

	@Test
	void toStartedDTO_shouldMapAllFieldsCorrectly() {
		UUID executionId = UUID.randomUUID();
		TestExecutionStartedEventAsyncDTO.Status status = TestExecutionStartedEventAsyncDTO.Status.RUNNING;
		String message = "Test execution has started";
		Links links = Links.of(Link.of("http://example.com/test", "test"));

		TestExecutionStartedEventAsyncDTO result = mapper.toStartedDTO(executionId, status, message, links);

		assertEquals(executionId.toString(), result.getExecutionId());
		assertEquals(status, result.getStatus());
		assertEquals(message, result.getMessage());
		assertEquals(1, result.getLinks().size());
		assertEquals("http://example.com/test", result.getLinks().get("test").getHref());
	}

	@Test
	void toStartedDTO_shouldHandleEmptyLinks() {
		UUID executionId = UUID.randomUUID();
		TestExecutionStartedEventAsyncDTO.Status status = TestExecutionStartedEventAsyncDTO.Status.RUNNING;
		String message = "Test execution has started";
		Links links = Links.NONE;

		TestExecutionStartedEventAsyncDTO result = mapper.toStartedDTO(executionId, status, message, links);

		assertEquals(executionId.toString(), result.getExecutionId());
		assertEquals(status, result.getStatus());
		assertEquals(message, result.getMessage());
		assertEquals(0, result.getLinks().size());
	}

	@Test
	void toStartedDTO_shouldHandleNullLinks() {
		UUID executionId = UUID.randomUUID();
		TestExecutionStartedEventAsyncDTO.Status status = TestExecutionStartedEventAsyncDTO.Status.RUNNING;
		String message = "Test execution has started";

		TestExecutionStartedEventAsyncDTO result = mapper.toStartedDTO(executionId, status, message, null);

		assertEquals(executionId.toString(), result.getExecutionId());
		assertEquals(status, result.getStatus());
		assertEquals(message, result.getMessage());
		assertEquals(0, result.getLinks().size());
	}

	@Test
	void toStartedDTO_shouldHandleNullExecutionId() {
		TestExecutionStartedEventAsyncDTO.Status status = TestExecutionStartedEventAsyncDTO.Status.RUNNING;
		String message = "Test execution has started";
		Links links = Links.NONE;

		TestExecutionStartedEventAsyncDTO result = mapper.toStartedDTO(null, status, message, links);

		assertNull(result.getExecutionId());
		assertEquals(status, result.getStatus());
		assertEquals(message, result.getMessage());
		assertEquals(0, result.getLinks().size());
	}

	@Test
	void toStartedDTO_shouldHandleNullStatus() {
		UUID executionId = UUID.randomUUID();
		String message = "Test execution has started";
		Links links = Links.NONE;

		TestExecutionStartedEventAsyncDTO result = mapper.toStartedDTO(executionId, null, message, links);

		assertEquals(executionId.toString(), result.getExecutionId());
		assertNull(result.getStatus());
		assertEquals(message, result.getMessage());
		assertEquals(0, result.getLinks().size());
	}

	@Test
	void toStartedDTO_shouldHandleNullMessage() {
		UUID executionId = UUID.randomUUID();
		TestExecutionStartedEventAsyncDTO.Status status = TestExecutionStartedEventAsyncDTO.Status.RUNNING;
		Links links = Links.NONE;

		TestExecutionStartedEventAsyncDTO result = mapper.toStartedDTO(executionId, status, null, links);

		assertEquals(executionId.toString(), result.getExecutionId());
		assertEquals(status, result.getStatus());
		assertNull(result.getMessage());
		assertEquals(0, result.getLinks().size());
	}

	@Test
	void mapLink_shouldReturnNull_if_Link_is_Null() {
		LinkAsyncDTO link = mapper.map((Link) null);
		assertNull(link);
	}

}