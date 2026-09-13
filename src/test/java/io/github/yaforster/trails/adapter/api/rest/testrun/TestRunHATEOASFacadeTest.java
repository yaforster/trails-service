package io.github.yaforster.trails.adapter.api.rest.testrun;

import io.github.yaforster.trails.core.PagedResult;

import io.github.yaforster.trails.adapter.api.rest.model.PagedTestRunDTO;
import io.github.yaforster.trails.adapter.api.rest.model.PagedTestRunHistoryDTO;
import io.github.yaforster.trails.adapter.api.rest.model.PersistedTestRunResultDTO;
import io.github.yaforster.trails.adapter.api.rest.model.TestRunStatisticsDTO;
import io.github.yaforster.trails.adapter.api.rest.testrun.assembler.TestRunResultAssembler;
import io.github.yaforster.trails.adapter.api.rest.testrun.assembler.TestRunResultContext;
import io.github.yaforster.trails.adapter.api.rest.testrun.assembler.TestRunStatisticsAssembler;
import io.github.yaforster.trails.adapter.api.rest.testrun.assembler.TestRunStatisticsContext;
import io.github.yaforster.trails.adapter.api.rest.testrun.mapper.PagedTestRunResultMapper;
import io.github.yaforster.trails.adapter.api.rest.testrun.mapper.TestRunStatisticsMapper;
import io.github.yaforster.trails.adapter.api.rest.testrun.model.TestRunResultModel;
import io.github.yaforster.trails.adapter.api.rest.testrun.model.TestRunStatisticsModel;
import io.github.yaforster.trails.core.data.ResultIndicator;
import io.github.yaforster.trails.core.persisted.PersistedTestRunHistory;
import io.github.yaforster.trails.core.persisted.PersistedTestRunResult;
import io.github.yaforster.trails.core.persisted.PersistedTestRunStatistics;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.web.PagedResourcesAssembler;
import org.springframework.hateoas.PagedModel;

import java.sql.Timestamp;
import java.time.OffsetDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.same;
import static org.mockito.Mockito.*;

class TestRunHATEOASFacadeTest {

	private final TestRunResultAssembler assembler = mock(TestRunResultAssembler.class);

	private final PagedResourcesAssembler<TestRunResultContext> pagedAssembler = mock(PagedResourcesAssembler.class);

	private final PagedTestRunResultMapper mapper = mock(PagedTestRunResultMapper.class);

	private final TestRunStatisticsAssembler statisticsAssembler = mock(TestRunStatisticsAssembler.class);

	private final TestRunStatisticsMapper statisticsMapper = mock(TestRunStatisticsMapper.class);

	private final TestRunHATEOASFacade facade = new TestRunHATEOASFacade(assembler, pagedAssembler, mapper,
			statisticsAssembler, statisticsMapper);

	private static PersistedTestRunResult persisted(Long id, Long applicationId, Long stageId) {
		return new PersistedTestRunResult(id, applicationId, stageId, 4L, Timestamp.valueOf("2026-01-01 00:00:00"),
				ResultIndicator.SUCCESS, "label");
	}

	private static TestRunResultContext context(Long resultId, Long applicationId, Long stageId) {
		return new TestRunResultContext(applicationId, stageId, resultId, OffsetDateTime.parse("2026-01-01T00:00:00Z"),
				ResultIndicator.SUCCESS, "label");
	}

	private static TestRunResultModel model(TestRunResultContext context) {
		return new TestRunResultModel(context.resultId(), context.timestamp(), context.indicator(),
				context.applicationId(), context.stageId(), context.label());
	}

	@Test
	void toDTO_shouldDelegateAssemblerAndMapper() {
		PersistedTestRunResult persisted = persisted(9L, 1L, 2L);
		TestRunResultContext context = context(9L, 1L, 2L);
		TestRunResultModel model = model(context);
		PersistedTestRunResultDTO dto = new PersistedTestRunResultDTO().id(9L);

		when(assembler.toContext(persisted)).thenReturn(context);
		when(assembler.toModel(context)).thenReturn(model);
		when(mapper.toPersistedTestRunResultDto(model)).thenReturn(dto);

		PersistedTestRunResultDTO response = facade.toDTO(persisted);

		assertSame(dto, response);
		verify(assembler).toContext(persisted);
		verify(assembler).toModel(context);
		verify(mapper).toPersistedTestRunResultDto(model);
	}

	@Test
	@SuppressWarnings("unchecked")
	void toPagedDTO_shouldMapPageContextsAndAddLinks() {
		Long applicationId = 1L;
		Long stageId = 2L;
		PersistedTestRunResult persisted = persisted(9L, applicationId, stageId);
		TestRunResultContext context = context(9L, applicationId, stageId);
		PagedResult<PersistedTestRunResult> pagedResults = new PagedResult<>(List.of(persisted), 1, 20, 80);
		PagedModel<TestRunResultModel> model = PagedModel.of(List.of(model(context)),
				new PagedModel.PageMetadata(20, 1, 80, 4));
		PagedTestRunDTO dto = new PagedTestRunDTO();

		when(assembler.toContext(persisted)).thenReturn(context);
		when(pagedAssembler.toModel(any(Page.class), same(assembler))).thenReturn(model);
		when(mapper.toPagedDto(any(PagedModel.class))).thenReturn(dto);

		PagedTestRunDTO response = facade.toPagedDTO(applicationId, stageId, pagedResults);

		verify(assembler).toContext(persisted);
		ArgumentCaptor<PagedModel<TestRunResultModel>> modelCaptor = (ArgumentCaptor<PagedModel<TestRunResultModel>>) (ArgumentCaptor<?>) ArgumentCaptor
			.forClass(PagedModel.class);
		verify(mapper).toPagedDto(modelCaptor.capture());
		PagedModel<TestRunResultModel> captured = modelCaptor.getValue();
		assertTrue(captured.getLink("self").isPresent());
		assertTrue(captured.getLink("first").isPresent());
		assertTrue(captured.getLink("last").isPresent());
		assertTrue(captured.getLink("prev").isPresent());
		assertTrue(captured.getLink("next").isPresent());
		assertTrue(captured.getRequiredLink("self").getHref().contains("/applications/1/stages/2/testruns"));
		assertTrue(captured.getRequiredLink("self").getHref().contains("page=1"));
		assertTrue(captured.getRequiredLink("self").getHref().contains("size=20"));
		assertSame(dto, response);
	}

	@Test
	@SuppressWarnings("unchecked")
	void toPagedDTO_shouldOmitPrevAndNextOnSinglePage() {
		PersistedTestRunResult persisted = persisted(9L, 1L, 2L);
		TestRunResultContext context = context(9L, 1L, 2L);
		PagedResult<PersistedTestRunResult> pagedResults = new PagedResult<>(List.of(persisted), 0, 20, 1);
		PagedModel<TestRunResultModel> model = PagedModel.of(List.of(model(context)),
				new PagedModel.PageMetadata(20, 0, 1, 1));

		when(assembler.toContext(persisted)).thenReturn(context);
		when(pagedAssembler.toModel(any(Page.class), same(assembler))).thenReturn(model);
		when(mapper.toPagedDto(any(PagedModel.class))).thenReturn(new PagedTestRunDTO());

		facade.toPagedDTO(1L, 2L, pagedResults);

		ArgumentCaptor<PagedModel<TestRunResultModel>> modelCaptor = (ArgumentCaptor<PagedModel<TestRunResultModel>>) (ArgumentCaptor<?>) ArgumentCaptor
			.forClass(PagedModel.class);
		verify(mapper).toPagedDto(modelCaptor.capture());
		PagedModel<TestRunResultModel> captured = modelCaptor.getValue();
		assertFalse(captured.getLink("prev").isPresent());
		assertFalse(captured.getLink("next").isPresent());
		assertTrue(captured.getRequiredLink("last").getHref().contains("page=0"));
	}

	@Test
	void toHistoryDTO_shouldExposePageMetadataAndPageLinks() {
		PersistedTestRunResult persisted = persisted(9L, 1L, 2L);
		PagedResult<PersistedTestRunResult> page = new PagedResult<>(List.of(persisted), 1, 25, 80);
		PersistedTestRunHistory history = new PersistedTestRunHistory(1L, 2L, 4L, 80L, 60L, 15L, 5L, page);

		PagedTestRunHistoryDTO response = facade.toHistoryDTO(history);

		assertEquals(1, response.getPage());
		assertEquals(25, response.getSize());
		assertEquals(80, response.getTotalElements());
		assertEquals(4, response.getTotalPages());
		assertEquals(1, response.getItems().size());
		assertEquals(9L, response.getItems().getFirst().getId());
		assertEquals(80L, response.getTotalRuns());
		assertEquals(60L, response.getSuccessfulRuns());
		assertEquals(15L, response.getPartialSuccessRuns());
		assertEquals(5L, response.getFailedRuns());
		assertTrue(response.getLinks().get("self").getHref().contains("page=1"));
		assertTrue(response.getLinks().get("self").getHref().contains("size=25"));
		assertTrue(response.getLinks().get("first").getHref().contains("page=0"));
		assertTrue(response.getLinks().get("last").getHref().contains("page=3"));
		assertTrue(response.getLinks().get("prev").getHref().contains("page=0"));
		assertTrue(response.getLinks().get("next").getHref().contains("page=2"));
	}

	@Test
	void toStatisticsDTO_shouldDelegateAssemblerAndMapper() {
		PersistedTestRunStatistics statistics = new PersistedTestRunStatistics(1L, 2L, 3L, 10L, 4L, 3L, 3L);
		TestRunStatisticsContext context = new TestRunStatisticsContext(1L, 2L, 3L, 10L, 4L, 3L, 3L);
		TestRunStatisticsModel model = new TestRunStatisticsModel(1L, 2L, 3L, 10L, 4L, 3L, 3L);
		TestRunStatisticsDTO dto = new TestRunStatisticsDTO();

		when(statisticsAssembler.toContext(statistics)).thenReturn(context);
		when(statisticsAssembler.toModel(context)).thenReturn(model);
		when(statisticsMapper.toDTO(model)).thenReturn(dto);

		TestRunStatisticsDTO response = facade.toStatisticsDTO(statistics);

		assertSame(dto, response);
		verify(statisticsAssembler).toContext(statistics);
		verify(statisticsAssembler).toModel(context);
		verify(statisticsMapper).toDTO(model);
	}

}
