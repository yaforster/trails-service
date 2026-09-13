package io.github.yaforster.trails.adapter.api.rest.action;

import io.github.yaforster.trails.core.PagedResult;

import io.github.yaforster.trails.adapter.api.rest.action.assembler.ActionModelAssembler;
import io.github.yaforster.trails.adapter.api.rest.action.assembler.PersistedActionContext;
import io.github.yaforster.trails.adapter.api.rest.action.mapper.PagedActionMapper;
import io.github.yaforster.trails.adapter.api.rest.action.model.ActionModel;
import io.github.yaforster.trails.adapter.api.rest.model.PagedActionDTO;
import io.github.yaforster.trails.core.persisted.PersistedAction;
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

class ActionHATEOASFacadeTest {

	private final ActionModelAssembler assembler = mock(ActionModelAssembler.class);

	private final PagedResourcesAssembler<PersistedActionContext> pagedAssembler = mock(PagedResourcesAssembler.class);

	private final PagedActionMapper pagedActionMapper = mock(PagedActionMapper.class);

	private final ActionHATEOASFacade facade = new ActionHATEOASFacade(assembler, pagedAssembler, pagedActionMapper);

	@Test
	@SuppressWarnings("unchecked")
	void toPagedDTO_shouldMapContextsAndAddPagingLinks() {
		Long applicationId = 1L;
		Long stageId = 2L;
		Long testPlanId = 3L;
		PersistedAction first = new PersistedAction(9L, testPlanId, 7L,
				mock(io.github.yaforster.trails.core.test.Action.class));
		PersistedAction second = new PersistedAction(10L, testPlanId, 8L,
				mock(io.github.yaforster.trails.core.test.Action.class));
		PagedResult<PersistedAction> paged = new PagedResult<>(List.of(first, second), 1, 20, 80);

		PagedModel<ActionModel> model = PagedModel.of(List.of(mock(ActionModel.class)),
				new PagedModel.PageMetadata(20, 1, 80, 4));
		PagedActionDTO dto = new PagedActionDTO();
		when(pagedAssembler.toModel(any(Page.class), same(assembler))).thenReturn(model);
		when(pagedActionMapper.toPagedDto(any(PagedModel.class))).thenReturn(dto);

		PagedActionDTO result = facade.toPagedDTO(applicationId, stageId, testPlanId, paged);

		ArgumentCaptor<Page<PersistedActionContext>> pageCaptor = (ArgumentCaptor<Page<PersistedActionContext>>) (ArgumentCaptor<?>) ArgumentCaptor
			.forClass(Page.class);
		verify(pagedAssembler).toModel(pageCaptor.capture(), same(assembler));
		List<PersistedActionContext> contexts = pageCaptor.getValue().getContent();
		assertEquals(2, contexts.size());
		assertEquals(applicationId, contexts.getFirst().applicationId());
		assertEquals(stageId, contexts.getFirst().stageId());
		assertEquals(testPlanId, contexts.getFirst().testPlanId());
		assertSame(first, contexts.getFirst().persistedAction());
		assertEquals(applicationId, contexts.get(1).applicationId());
		assertEquals(stageId, contexts.get(1).stageId());
		assertEquals(testPlanId, contexts.get(1).testPlanId());
		assertSame(second, contexts.get(1).persistedAction());

		ArgumentCaptor<PagedModel<ActionModel>> modelCaptor = (ArgumentCaptor<PagedModel<ActionModel>>) (ArgumentCaptor<?>) ArgumentCaptor
			.forClass(PagedModel.class);
		verify(pagedActionMapper).toPagedDto(modelCaptor.capture());
		PagedModel<ActionModel> captured = modelCaptor.getValue();
		assertTrue(captured.getLink("self").isPresent());
		assertTrue(captured.getLink("testPlan").isPresent());
		assertTrue(captured.getLink("first").isPresent());
		assertTrue(captured.getLink("last").isPresent());
		assertTrue(captured.getLink("prev").isPresent());
		assertTrue(captured.getLink("next").isPresent());
		assertTrue(captured.getRequiredLink("self").getHref().contains("/applications/1/stages/2/testPlans/3/actions"));
		assertTrue(captured.getRequiredLink("self").getHref().contains("page=1"));
		assertTrue(captured.getRequiredLink("self").getHref().contains("size=20"));
		assertSame(dto, result);
	}

	@Test
	@SuppressWarnings("unchecked")
	void toPagedDTO_shouldOmitPrevAndNextForSinglePage() {
		PersistedAction action = new PersistedAction(9L, 3L, 7L,
				mock(io.github.yaforster.trails.core.test.Action.class));
		PagedResult<PersistedAction> paged = new PagedResult<>(List.of(action), 0, 20, 1);
		PagedModel<ActionModel> model = PagedModel.of(List.of(mock(ActionModel.class)),
				new PagedModel.PageMetadata(20, 0, 1, 1));
		when(pagedAssembler.toModel(any(Page.class), same(assembler))).thenReturn(model);
		when(pagedActionMapper.toPagedDto(any(PagedModel.class))).thenReturn(new PagedActionDTO());

		facade.toPagedDTO(1L, 2L, 3L, paged);

		ArgumentCaptor<PagedModel<ActionModel>> modelCaptor = (ArgumentCaptor<PagedModel<ActionModel>>) (ArgumentCaptor<?>) ArgumentCaptor
			.forClass(PagedModel.class);
		verify(pagedActionMapper).toPagedDto(modelCaptor.capture());
		PagedModel<ActionModel> captured = modelCaptor.getValue();
		assertFalse(captured.getLink("prev").isPresent());
		assertFalse(captured.getLink("next").isPresent());
		assertTrue(captured.getRequiredLink("last").getHref().contains("page=0"));
	}

}
