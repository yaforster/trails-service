package io.github.yaforster.trails.adapter.api.asyncapi.run.mapper;

import io.github.yaforster.trails.adapter.api.asyncapi.model.*;
import io.github.yaforster.trails.app.GlobalMapperConfig;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.Links;

import java.util.Collections;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Mapper(config = GlobalMapperConfig.class)
public interface TestExecutionEventDTOMapper {

	@Mapping(target = "executionId", source = "executionId")
	@Mapping(target = "status", source = "status")
	@Mapping(target = "message", source = "message")
	@Mapping(target = "links", source = "links")
	TestExecutionStartedEventAsyncDTO toStartedDTO(UUID executionId, TestExecutionStartedEventAsyncDTO.Status status,
			String message, Links links);

	@Mapping(target = "executionId", source = "executionId")
	@Mapping(target = "status", source = "status")
	@Mapping(target = "message", source = "message")
	@Mapping(target = "testRunId", source = "testRunId")
	@Mapping(target = "links", source = "links")
	TestExecutionCompletedEventAsyncDTO toCompletedDTO(UUID executionId,
			TestExecutionCompletedEventAsyncDTO.Status status, String message, Long testRunId, Links links);

	@Mapping(target = "executionId", source = "executionId")
	@Mapping(target = "status", source = "status")
	@Mapping(target = "message", source = "message")
	@Mapping(target = "error", source = "error")
	@Mapping(target = "links", source = "links")
	TestExecutionFailedEventAsyncDTO toFailedDTO(UUID executionId, TestExecutionFailedEventAsyncDTO.Status status,
			String message, ErrorAsyncDTO error, Links links);

	default String map(UUID executionId) {
		return executionId == null ? null : executionId.toString();
	}

	default Map<String, LinkAsyncDTO> map(Links links) {
		if (links == null || links.isEmpty()) {
			return Collections.emptyMap();
		}
		return links.stream().collect(Collectors.toMap(link -> link.getRel().value(), this::map));
	}

	default LinkAsyncDTO map(Link link) {
		if (link == null) {
			return null;
		}
		return LinkAsyncDTO.builder().href(link.getHref()).build();
	}

}
