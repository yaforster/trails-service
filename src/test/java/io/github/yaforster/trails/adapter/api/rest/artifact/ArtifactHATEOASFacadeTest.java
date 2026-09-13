package io.github.yaforster.trails.adapter.api.rest.artifact;

import io.github.yaforster.trails.core.PagedResult;

import io.github.yaforster.trails.adapter.api.rest.artifact.assembler.ArtifactModelAssembler;
import io.github.yaforster.trails.adapter.api.rest.artifact.assembler.PersistedArtifactContext;
import io.github.yaforster.trails.adapter.api.rest.artifact.mapper.PagedArtifactMapper;
import io.github.yaforster.trails.adapter.api.rest.artifact.model.ArtifactModel;
import io.github.yaforster.trails.adapter.api.rest.model.PagedArtifactDTO;
import io.github.yaforster.trails.core.ArtifactType;
import io.github.yaforster.trails.core.persisted.PersistedArtifact;
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

class ArtifactHATEOASFacadeTest {

	private final ArtifactModelAssembler assembler = mock(ArtifactModelAssembler.class);

	private final PagedResourcesAssembler<PersistedArtifactContext> pagedAssembler = mock(
			PagedResourcesAssembler.class);

	private final PagedArtifactMapper pagedArtifactMapper = mock(PagedArtifactMapper.class);

	private final ArtifactHATEOASFacade facade = new ArtifactHATEOASFacade(assembler, pagedAssembler,
			pagedArtifactMapper);

	@Test
	@SuppressWarnings("unchecked")
	void toPagedDTO_shouldMapContextsAndAddPagingLinks() {
		Long applicationId = 1L;
		Long stageId = 2L;
		Long testRunId = 3L;
		Long testSetResultId = 4L;
		Long pathResultId = 77L;
		PersistedArtifact first = new PersistedArtifact(9L, pathResultId, ArtifactType.SCREENSHOT, "a.png", "image/png",
				10L, "k1");
		PersistedArtifact second = new PersistedArtifact(10L, pathResultId, ArtifactType.FILE, "a.log", "text/plain",
				20L, "k2");
		PagedResult<PersistedArtifact> pagedFiles = new PagedResult<>(List.of(first, second), 1, 20, 80);

		PagedModel<ArtifactModel> model = PagedModel.of(
				List.of(new ArtifactModel(9L, ArtifactModel.Type.SCREENSHOT, "a.png")),
				new PagedModel.PageMetadata(20, 1, 80, 4));
		PagedArtifactDTO dto = new PagedArtifactDTO();
		when(pagedAssembler.toModel(any(Page.class), same(assembler))).thenReturn(model);
		when(pagedArtifactMapper.toPagedDto(any(PagedModel.class))).thenReturn(dto);

		PagedArtifactDTO result = facade.toPagedDTOInHierarchy(applicationId, stageId, testRunId, testSetResultId,
				pathResultId, pagedFiles);

		ArgumentCaptor<Page<PersistedArtifactContext>> pageCaptor = (ArgumentCaptor<Page<PersistedArtifactContext>>) (ArgumentCaptor<?>) ArgumentCaptor
			.forClass(Page.class);
		verify(pagedAssembler).toModel(pageCaptor.capture(), same(assembler));
		List<PersistedArtifactContext> contexts = pageCaptor.getValue().getContent();
		assertEquals(2, contexts.size());
		assertEquals(applicationId, contexts.getFirst().applicationId());
		assertEquals(stageId, contexts.getFirst().stageId());
		assertEquals(testRunId, contexts.getFirst().testRunId());
		assertEquals(testSetResultId, contexts.getFirst().testSetResultId());
		assertEquals(pathResultId, contexts.getFirst().pathResultId());
		assertSame(first, contexts.getFirst().persistedArtifact());
		assertSame(second, contexts.get(1).persistedArtifact());

		ArgumentCaptor<PagedModel<ArtifactModel>> modelCaptor = (ArgumentCaptor<PagedModel<ArtifactModel>>) (ArgumentCaptor<?>) ArgumentCaptor
			.forClass(PagedModel.class);
		verify(pagedArtifactMapper).toPagedDto(modelCaptor.capture());
		PagedModel<ArtifactModel> captured = modelCaptor.getValue();
		assertTrue(captured.getLink("self").isPresent());
		assertTrue(captured.getLink("first").isPresent());
		assertTrue(captured.getLink("last").isPresent());
		assertTrue(captured.getLink("prev").isPresent());
		assertTrue(captured.getLink("next").isPresent());
		assertTrue(
				captured.getRequiredLink("self").getHref().contains("/testruns/1/2/3/browsers/4/paths/77/artifacts"));
		assertTrue(captured.getRequiredLink("self").getHref().contains("page=1"));
		assertTrue(captured.getRequiredLink("self").getHref().contains("size=20"));
		assertSame(dto, result);
	}

	@Test
	@SuppressWarnings("unchecked")
	void toPagedDTO_shouldOmitPrevAndNextForSinglePage() {
		Long applicationId = 1L;
		Long stageId = 2L;
		Long testRunId = 3L;
		Long testSetResultId = 4L;
		Long pathResultId = 77L;
		PersistedArtifact artifact = new PersistedArtifact(9L, pathResultId, ArtifactType.FILE, "a.log", "text/plain",
				20L, "k1");
		PagedResult<PersistedArtifact> pagedFiles = new PagedResult<>(List.of(artifact), 0, 20, 1);

		PagedModel<ArtifactModel> model = PagedModel.of(List.of(new ArtifactModel(9L, ArtifactModel.Type.LOG, "a.log")),
				new PagedModel.PageMetadata(20, 0, 1, 1));
		when(pagedAssembler.toModel(any(Page.class), same(assembler))).thenReturn(model);
		when(pagedArtifactMapper.toPagedDto(any(PagedModel.class))).thenReturn(new PagedArtifactDTO());

		facade.toPagedDTOInHierarchy(applicationId, stageId, testRunId, testSetResultId, pathResultId, pagedFiles);

		ArgumentCaptor<PagedModel<ArtifactModel>> modelCaptor = (ArgumentCaptor<PagedModel<ArtifactModel>>) (ArgumentCaptor<?>) ArgumentCaptor
			.forClass(PagedModel.class);
		verify(pagedArtifactMapper).toPagedDto(modelCaptor.capture());
		PagedModel<ArtifactModel> captured = modelCaptor.getValue();
		assertFalse(captured.getLink("prev").isPresent());
		assertFalse(captured.getLink("next").isPresent());
		assertTrue(captured.getRequiredLink("last").getHref().contains("page=0"));
	}

}
