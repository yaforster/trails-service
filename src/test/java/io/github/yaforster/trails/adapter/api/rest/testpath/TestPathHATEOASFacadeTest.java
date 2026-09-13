package io.github.yaforster.trails.adapter.api.rest.testpath;

import io.github.yaforster.trails.core.PagedResult;

import io.github.yaforster.trails.adapter.api.rest.model.PagedTestPathResultDTO;
import io.github.yaforster.trails.adapter.api.rest.model.TestPathResultDTO;
import io.github.yaforster.trails.adapter.api.rest.testpath.assembler.PersistedTestPathResultContext;
import io.github.yaforster.trails.adapter.api.rest.testpath.assembler.TestPathResultModelAssembler;
import io.github.yaforster.trails.adapter.api.rest.testpath.mapper.PagedTestPathResultMapper;
import io.github.yaforster.trails.adapter.api.rest.testpath.model.TestPathResultModel;
import io.github.yaforster.trails.core.persisted.PersistedTestPathResult;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.web.PagedResourcesAssembler;
import org.springframework.hateoas.PagedModel;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.same;
import static org.mockito.Mockito.*;

class TestPathHATEOASFacadeTest {

	private final TestPathResultModelAssembler assembler = mock(TestPathResultModelAssembler.class);

	private final PagedResourcesAssembler<PersistedTestPathResultContext> pagedAssembler = mock(
			PagedResourcesAssembler.class);

	private final PagedTestPathResultMapper mapper = mock(PagedTestPathResultMapper.class);

	private final TestPathHATEOASFacade facade = new TestPathHATEOASFacade(assembler, pagedAssembler, mapper);

	@Test
	void toDTOInHierarchy_shouldBuildContextFromEntityAndDelegate() {
		PersistedTestPathResult entity = new PersistedTestPathResult(11L, 9L, "path");
		TestPathResultModel model = new TestPathResultModel(11L);
		TestPathResultDTO dto = new TestPathResultDTO().id(11L);
		when(assembler.toModel(any(PersistedTestPathResultContext.class))).thenReturn(model);
		when(mapper.toTestPathResultDto(model)).thenReturn(dto);

		TestPathResultDTO response = facade.toDTOInHierarchy(1L, 2L, 3L, entity);

		ArgumentCaptor<PersistedTestPathResultContext> contextCaptor = ArgumentCaptor
			.forClass(PersistedTestPathResultContext.class);
		verify(assembler).toModel(contextCaptor.capture());
		PersistedTestPathResultContext captured = contextCaptor.getValue();
		assertEquals(1L, captured.applicationId());
		assertEquals(2L, captured.stageId());
		assertEquals(3L, captured.testRunId());
		assertEquals(9L, captured.testSetResultId());
		assertSame(entity, captured.persistedTestPathResult());
		assertSame(dto, response);
	}

	@Test
	@SuppressWarnings("unchecked")
	void toPagedDTOInHierarchy_shouldMapContextsAndAddPagingLinks() {
		Long applicationId = 1L;
		Long stageId = 2L;
		Long testRunId = 3L;
		Long testSetResultId = 9L;
		PagedResult<PersistedTestPathResult> pagedResults = new PagedResult<>(
				List.of(new PersistedTestPathResult(11L, testSetResultId, "path")), 1, 20, 80);
		PagedModel<TestPathResultModel> model = PagedModel.of(List.of(new TestPathResultModel(11L)),
				new PagedModel.PageMetadata(20, 1, 80, 4));
		PagedTestPathResultDTO dto = new PagedTestPathResultDTO();

		when(pagedAssembler.toModel(any(Page.class), same(assembler))).thenReturn(model);
		when(mapper.toPagedDto(any(PagedModel.class))).thenReturn(dto);

		PagedTestPathResultDTO response = facade.toPagedDTOInHierarchy(applicationId, stageId, testRunId,
				testSetResultId, pagedResults);

		ArgumentCaptor<Page<PersistedTestPathResultContext>> pageCaptor = (ArgumentCaptor<Page<PersistedTestPathResultContext>>) (ArgumentCaptor<?>) ArgumentCaptor
			.forClass(Page.class);
		verify(pagedAssembler).toModel(pageCaptor.capture(), same(assembler));
		PersistedTestPathResultContext mappedContext = pageCaptor.getValue().getContent().getFirst();
		assertEquals(applicationId, mappedContext.applicationId());
		assertEquals(stageId, mappedContext.stageId());
		assertEquals(testRunId, mappedContext.testRunId());
		assertEquals(testSetResultId, mappedContext.testSetResultId());

		ArgumentCaptor<PagedModel<TestPathResultModel>> modelCaptor = (ArgumentCaptor<PagedModel<TestPathResultModel>>) (ArgumentCaptor<?>) ArgumentCaptor
			.forClass(PagedModel.class);
		verify(mapper).toPagedDto(modelCaptor.capture());
		PagedModel<TestPathResultModel> capturedModel = modelCaptor.getValue();
		assertTrue(capturedModel.getRequiredLink("self").getHref().contains("/testruns/1/2/3/browsers/9/paths"));
		assertTrue(capturedModel.getRequiredLink("self").getHref().contains("page=1"));
		assertTrue(capturedModel.getRequiredLink("self").getHref().contains("size=20"));
		assertSame(dto, response);
	}

	@Test
	@SuppressWarnings("unchecked")
	void toPagedDTOInHierarchy_shouldNotAddPrevAndNextForSinglePageResults() {
		PagedResult<PersistedTestPathResult> pagedResults = new PagedResult<>(
				List.of(new PersistedTestPathResult(11L, 9L, "path")), 0, 20, 1);
		PagedModel<TestPathResultModel> model = PagedModel.of(List.of(new TestPathResultModel(11L)),
				new PagedModel.PageMetadata(20, 0, 1, 1));

		when(pagedAssembler.toModel(any(Page.class), same(assembler))).thenReturn(model);
		when(mapper.toPagedDto(any(PagedModel.class))).thenReturn(new PagedTestPathResultDTO());

		facade.toPagedDTOInHierarchy(1L, 2L, 3L, 9L, pagedResults);

		ArgumentCaptor<PagedModel<TestPathResultModel>> modelCaptor = (ArgumentCaptor<PagedModel<TestPathResultModel>>) (ArgumentCaptor<?>) ArgumentCaptor
			.forClass(PagedModel.class);
		verify(mapper).toPagedDto(modelCaptor.capture());
		PagedModel<TestPathResultModel> captured = modelCaptor.getValue();
		assertFalse(captured.getLink("prev").isPresent());
		assertFalse(captured.getLink("next").isPresent());
		assertTrue(captured.getRequiredLink("last").getHref().contains("page=0"));
	}

}
