package io.github.yaforster.trails.adapter.api.rest.testset;

import io.github.yaforster.trails.core.PagedResult;

import io.github.yaforster.trails.adapter.api.rest.model.PagedTestSetResultDTO;
import io.github.yaforster.trails.adapter.api.rest.model.TestSetResultDTO;
import io.github.yaforster.trails.adapter.api.rest.testset.assembler.PersistedTestSetResultContext;
import io.github.yaforster.trails.adapter.api.rest.testset.assembler.TestSetResultModelAssembler;
import io.github.yaforster.trails.adapter.api.rest.testset.mapper.PagedTestSetResultMapper;
import io.github.yaforster.trails.adapter.api.rest.testset.model.TestSetResultModel;
import io.github.yaforster.trails.core.persisted.PersistedTestSetResult;
import io.github.yaforster.trails.core.test.Browser;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.web.PagedResourcesAssembler;
import org.springframework.hateoas.PagedModel;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.same;
import static org.mockito.Mockito.*;

class TestSetHATEOASFacadeTest {

	private final TestSetResultModelAssembler assembler = mock(TestSetResultModelAssembler.class);

	private final PagedResourcesAssembler<PersistedTestSetResultContext> pagedAssembler = mock(
			PagedResourcesAssembler.class);

	private final PagedTestSetResultMapper mapper = mock(PagedTestSetResultMapper.class);

	private final TestSetHATEOASFacade facade = new TestSetHATEOASFacade(assembler, pagedAssembler, mapper);

	private static PersistedTestSetResult persisted(Long id, Long testRunId, Long applicationId, Long stageId) {
		return new PersistedTestSetResult(id, testRunId, applicationId, stageId, 42, Browser.CHROME, "plan",
				OffsetDateTime.parse("2026-01-01T00:00:00Z"), Browser.FIREFOX);
	}

	@Test
	void toDTO_shouldBuildContextFromLookupAndDelegateToAssemblerAndMapper() {
		PersistedTestSetResult lookup = persisted(50L, 9L, 1L, 2L);
		TestSetResultModel model = new TestSetResultModel(50L, 42, Browser.CHROME, "plan", Optional.empty());
		TestSetResultDTO dto = new TestSetResultDTO().id(50L);

		when(assembler.toModel(any(PersistedTestSetResultContext.class))).thenReturn(model);
		when(mapper.toTestSetResultDto(model)).thenReturn(dto);

		TestSetResultDTO response = facade.toDTO(lookup);

		ArgumentCaptor<PersistedTestSetResultContext> contextCaptor = ArgumentCaptor
			.forClass(PersistedTestSetResultContext.class);
		verify(assembler).toModel(contextCaptor.capture());
		PersistedTestSetResultContext captured = contextCaptor.getValue();
		assertEquals(1L, captured.applicationId());
		assertEquals(2L, captured.stageId());
		assertEquals(9L, captured.testRunId());
		assertSame(lookup, captured.entity());
		assertSame(dto, response);
	}

	@Test
	@SuppressWarnings("unchecked")
	void toPagedDTO_shouldMapContextsAndAddPagingLinks() {
		Long applicationId = 1L;
		Long stageId = 2L;
		Long testRunId = 9L;
		PagedResult<PersistedTestSetResult> pagedResults = new PagedResult<>(List.of(persisted(100L, 99L, 88L, 77L)), 1,
				20, 80);
		PagedModel<TestSetResultModel> model = PagedModel.of(
				List.of(new TestSetResultModel(100L, 42, Browser.CHROME, "plan", Optional.empty())),
				new PagedModel.PageMetadata(20, 1, 80, 4));
		PagedTestSetResultDTO dto = new PagedTestSetResultDTO();

		when(pagedAssembler.toModel(any(Page.class), same(assembler))).thenReturn(model);
		when(mapper.toPagedDto(any(PagedModel.class))).thenReturn(dto);

		PagedTestSetResultDTO response = facade.toPagedDTO(applicationId, stageId, testRunId, pagedResults);

		ArgumentCaptor<Page<PersistedTestSetResultContext>> pageCaptor = (ArgumentCaptor<Page<PersistedTestSetResultContext>>) (ArgumentCaptor<?>) ArgumentCaptor
			.forClass(Page.class);
		verify(pagedAssembler).toModel(pageCaptor.capture(), same(assembler));
		PersistedTestSetResultContext mappedContext = pageCaptor.getValue().getContent().getFirst();
		assertEquals(applicationId, mappedContext.applicationId());
		assertEquals(stageId, mappedContext.stageId());
		assertEquals(testRunId, mappedContext.testRunId());
		assertEquals(100L, mappedContext.entity().id());

		ArgumentCaptor<PagedModel<TestSetResultModel>> modelCaptor = (ArgumentCaptor<PagedModel<TestSetResultModel>>) (ArgumentCaptor<?>) ArgumentCaptor
			.forClass(PagedModel.class);
		verify(mapper).toPagedDto(modelCaptor.capture());
		PagedModel<TestSetResultModel> capturedModel = modelCaptor.getValue();
		assertTrue(capturedModel.getLink("self").isPresent());
		assertTrue(capturedModel.getLink("testRun").isPresent());
		assertTrue(capturedModel.getLink("first").isPresent());
		assertTrue(capturedModel.getLink("last").isPresent());
		assertTrue(capturedModel.getLink("prev").isPresent());
		assertTrue(capturedModel.getLink("next").isPresent());
		assertTrue(capturedModel.getRequiredLink("self").getHref().contains("/testruns/1/2/9/browsers"));
		assertTrue(capturedModel.getRequiredLink("self").getHref().contains("page=1"));
		assertTrue(capturedModel.getRequiredLink("self").getHref().contains("size=20"));
		assertSame(dto, response);
	}

	@Test
	@SuppressWarnings("unchecked")
	void toPagedDTO_shouldNotAddPrevAndNextForSinglePageResults() {
		PagedResult<PersistedTestSetResult> pagedResults = new PagedResult<>(List.of(persisted(100L, 9L, 1L, 2L)), 0,
				20, 1);
		PagedModel<TestSetResultModel> model = PagedModel.of(
				List.of(new TestSetResultModel(100L, 42, Browser.CHROME, "plan", Optional.empty())),
				new PagedModel.PageMetadata(20, 0, 1, 1));

		when(pagedAssembler.toModel(any(Page.class), same(assembler))).thenReturn(model);
		when(mapper.toPagedDto(any(PagedModel.class))).thenReturn(new PagedTestSetResultDTO());

		facade.toPagedDTO(1L, 2L, 9L, pagedResults);

		ArgumentCaptor<PagedModel<TestSetResultModel>> modelCaptor = (ArgumentCaptor<PagedModel<TestSetResultModel>>) (ArgumentCaptor<?>) ArgumentCaptor
			.forClass(PagedModel.class);
		verify(mapper).toPagedDto(modelCaptor.capture());
		PagedModel<TestSetResultModel> captured = modelCaptor.getValue();
		assertFalse(captured.getLink("prev").isPresent());
		assertFalse(captured.getLink("next").isPresent());
		assertTrue(captured.getRequiredLink("last").getHref().contains("page=0"));
	}

}
