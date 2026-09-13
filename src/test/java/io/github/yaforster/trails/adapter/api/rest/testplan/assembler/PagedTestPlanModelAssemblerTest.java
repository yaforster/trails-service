package io.github.yaforster.trails.adapter.api.rest.testplan.assembler;

import io.github.yaforster.trails.adapter.api.ResourceAuthorization;
import io.github.yaforster.trails.adapter.api.rest.testplan.mapper.TestPlanModel;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.web.PagedResourcesAssembler;
import org.springframework.hateoas.PagedModel;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

class PagedTestPlanModelAssemblerTest {

	private final PagedResourcesAssembler<PersistedTestPlanContext> pagedAssembler = mock(
			PagedResourcesAssembler.class);

	private final TestPlanListItemModelAssembler listItemAssembler = mock(TestPlanListItemModelAssembler.class);

	private final ResourceAuthorization resourceAuthorization = mock(ResourceAuthorization.class);

	private final PagedTestPlanModelAssembler assembler = new PagedTestPlanModelAssembler(pagedAssembler,
			listItemAssembler, resourceAuthorization);

	@Test
	void toModel_shouldAddAllPagingLinks_forMiddlePage() {
		when(resourceAuthorization.canManageResources()).thenReturn(true);
		Page<PersistedTestPlanContext> contexts = new PageImpl<>(List.of(), PageRequest.of(2, 10), 50);
		PagedModel<TestPlanModel> model = PagedModel.empty();
		when(pagedAssembler.toModel(contexts, listItemAssembler)).thenReturn(model);
		PagedTestPlanContext context = new PagedTestPlanContext(11L, 22L, contexts);

		PagedModel<TestPlanModel> result = assembler.toModel(context);

		verify(pagedAssembler).toModel(contexts, listItemAssembler);
		assertTrue(result.getLink("self").isPresent());
		assertTrue(result.getLink("first").isPresent());
		assertTrue(result.getLink("last").isPresent());
		assertTrue(result.getLink("prev").isPresent());
		assertTrue(result.getLink("next").isPresent());
		assertTrue(result.getLink("create").isPresent());
		assertTrue(result.getRequiredLink("self").getHref().contains("page=2"));
		assertTrue(result.getRequiredLink("prev").getHref().contains("page=1"));
		assertTrue(result.getRequiredLink("next").getHref().contains("page=3"));
	}

	@Test
	void toModel_shouldNotAddCreateLink_whenUserCannotManageResources() {
		when(resourceAuthorization.canManageResources()).thenReturn(false);
		Page<PersistedTestPlanContext> contexts = new PageImpl<>(List.of(), PageRequest.of(0, 20), 1);
		PagedModel<TestPlanModel> model = PagedModel.empty();
		when(pagedAssembler.toModel(contexts, listItemAssembler)).thenReturn(model);
		PagedTestPlanContext context = new PagedTestPlanContext(1L, 2L, contexts);

		PagedModel<TestPlanModel> result = assembler.toModel(context);

		assertFalse(result.getLink("create").isPresent());
	}

	@Test
	void toModel_shouldNotAddPrev_onFirstPage() {
		Page<PersistedTestPlanContext> contexts = new PageImpl<>(List.of(), PageRequest.of(0, 20), 45);
		PagedModel<TestPlanModel> model = PagedModel.empty();
		when(pagedAssembler.toModel(contexts, listItemAssembler)).thenReturn(model);
		PagedTestPlanContext context = new PagedTestPlanContext(1L, 2L, contexts);

		PagedModel<TestPlanModel> result = assembler.toModel(context);

		assertTrue(result.getLink("self").isPresent());
		assertTrue(result.getLink("first").isPresent());
		assertTrue(result.getLink("last").isPresent());
		assertFalse(result.getLink("prev").isPresent());
		assertTrue(result.getLink("next").isPresent());
		assertTrue(result.getRequiredLink("first").getHref().contains("page=0"));
	}

	@Test
	void toModel_shouldNotAddNext_onLastPage() {
		Page<PersistedTestPlanContext> contexts = new PageImpl<>(List.of(), PageRequest.of(2, 20), 60);
		PagedModel<TestPlanModel> model = PagedModel.empty();
		when(pagedAssembler.toModel(contexts, listItemAssembler)).thenReturn(model);
		PagedTestPlanContext context = new PagedTestPlanContext(7L, 8L, contexts);

		PagedModel<TestPlanModel> result = assembler.toModel(context);

		assertTrue(result.getLink("self").isPresent());
		assertTrue(result.getLink("first").isPresent());
		assertTrue(result.getLink("last").isPresent());
		assertTrue(result.getLink("prev").isPresent());
		assertFalse(result.getLink("next").isPresent());
		assertTrue(result.getRequiredLink("last").getHref().contains("page=2"));
	}

}
