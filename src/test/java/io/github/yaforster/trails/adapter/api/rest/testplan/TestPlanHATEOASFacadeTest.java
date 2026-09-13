package io.github.yaforster.trails.adapter.api.rest.testplan;

import io.github.yaforster.trails.core.PagedResult;

import io.github.yaforster.trails.adapter.api.rest.action.mapper.ActionMapper;
import io.github.yaforster.trails.adapter.api.rest.deletion.DatabaseDeletionResultMapper;
import io.github.yaforster.trails.adapter.api.rest.deletion.DatabaseDeletionResultModelMapper;
import io.github.yaforster.trails.adapter.api.rest.model.*;
import io.github.yaforster.trails.adapter.api.rest.testplan.assembler.PagedTestPlanContext;
import io.github.yaforster.trails.adapter.api.rest.testplan.assembler.PagedTestPlanModelAssembler;
import io.github.yaforster.trails.adapter.api.rest.testplan.assembler.PersistedTestPlanContext;
import io.github.yaforster.trails.adapter.api.rest.testplan.assembler.TestPlanModelAssembler;
import io.github.yaforster.trails.adapter.api.rest.testplan.mapper.PagedTestPlanMapper;
import io.github.yaforster.trails.adapter.api.rest.testplan.mapper.PersistedTestPlanDTOMapper;
import io.github.yaforster.trails.adapter.api.rest.testplan.mapper.TestPlanMapper;
import io.github.yaforster.trails.adapter.api.rest.testplan.mapper.TestPlanModel;
import io.github.yaforster.trails.adapter.api.rest.testplan.model.PersistedTestPlanModel;
import io.github.yaforster.trails.app.services.TestPlanDatabaseService;
import io.github.yaforster.trails.core.data.TestPlan;
import io.github.yaforster.trails.core.definition.TestPlanDefinition;
import io.github.yaforster.trails.core.deletion.DatabaseDeletionResult;
import io.github.yaforster.trails.core.deletion.DeletionNotFound;
import io.github.yaforster.trails.core.persisted.PersistedTestPlan;
import io.github.yaforster.trails.core.test.Action;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.hateoas.PagedModel;

import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class TestPlanHATEOASFacadeTest {

	private final ActionMapper actionMapper = mock(ActionMapper.class);

	private final PersistedTestPlanDTOMapper persistedTestPlanMapper = mock(PersistedTestPlanDTOMapper.class);

	private final TestPlanModelAssembler assembler = mock(TestPlanModelAssembler.class);

	private final PagedTestPlanModelAssembler pagedModelAssembler = mock(PagedTestPlanModelAssembler.class);

	private final DatabaseDeletionResultMapper databaseDeletionResultMapper = new DatabaseDeletionResultMapper();

	private final DatabaseDeletionResultModelMapper databaseDeletionResultModelMapper = new DatabaseDeletionResultModelMapper();

	private final TestPlanMapper testPlanMapper = mock(TestPlanMapper.class);

	private final TestPlanDatabaseService testPlanDatabaseService = mock(TestPlanDatabaseService.class);

	private final PagedTestPlanMapper pagedTestPlanMapper = mock(PagedTestPlanMapper.class);

	private final TestPlanHATEOASFacade facade = new TestPlanHATEOASFacade(actionMapper, persistedTestPlanMapper,
			assembler, pagedModelAssembler, databaseDeletionResultMapper, databaseDeletionResultModelMapper,
			testPlanMapper, testPlanDatabaseService, pagedTestPlanMapper);

	@Test
	void fromDTO_shouldMapActionsAndCreateDefinition() {
		ActionDefinitionDTO actionDefinitionOne = new ActionDefinitionDTO().label("first");
		ActionDefinitionDTO actionDefinitionTwo = new ActionDefinitionDTO().label("second");
		Action actionOne = mock(Action.class);
		Action actionTwo = mock(Action.class);
		TestPlanDefinitionDTO dto = new TestPlanDefinitionDTO().label("plan")
			.testSteps(List.of(actionDefinitionOne, actionDefinitionTwo));
		when(actionMapper.fromDefinitionDTO(actionDefinitionOne)).thenReturn(actionOne);
		when(actionMapper.fromDefinitionDTO(actionDefinitionTwo)).thenReturn(actionTwo);

		TestPlanDefinition result = facade.fromDTO(1L, 2L, dto);

		assertEquals(1L, result.applicationID());
		assertEquals(2L, result.stageID());
		assertEquals("plan", result.label());
		assertEquals(List.of(actionOne, actionTwo), result.actions());
	}

	@Test
	void toDTO_shouldBuildContextAndDelegateForPersistedPlan() {
		PersistedTestPlan persisted = new PersistedTestPlan(9L, 1L, 2L, "plan");
		PersistedTestPlanModel model = new PersistedTestPlanModel(9L, 1L, 2L, "plan");
		PersistedTestPlanDTO dto = new PersistedTestPlanDTO().id(9L);
		when(assembler.toModel(any(PersistedTestPlanContext.class))).thenReturn(model);
		when(persistedTestPlanMapper.toDTO(model)).thenReturn(dto);

		PersistedTestPlanDTO result = facade.toDTO(1L, 2L, persisted, true);

		ArgumentCaptor<PersistedTestPlanContext> contextCaptor = ArgumentCaptor
			.forClass(PersistedTestPlanContext.class);
		verify(assembler).toModel(contextCaptor.capture());
		PersistedTestPlanContext captured = contextCaptor.getValue();
		assertEquals(1L, captured.applicationId());
		assertEquals(2L, captured.stageId());
		assertSame(persisted, captured.persistedTestPlan());
		assertTrue(captured.hasTestSteps());
		assertSame(dto, result);
	}

	@Test
	void toDTO_shouldMapDeletionNotFoundToDto() {
		Long applicationId = 1L;
		Long stageId = 2L;
		DatabaseDeletionResult deletionResult = new DeletionNotFound(9L);

		DatabaseDeletionResultDTO result = facade.toDTO(applicationId, stageId, deletionResult);

		assertEquals("No database entry with the given values was found.", result.getMessage());
		assertFalse(result.getDeletionResult());
		assertEquals(9L, result.getDeletionRequestedForID());
	}

	@Test
	void mapToTestPlanDTO_shouldDelegateAssemblerAndMapper() {
		TestPlan plan = new TestPlan(9L, 1L, 2L, "plan", List.of());
		TestPlanModel model = new TestPlanModel(9L, "plan");
		TestPlanDTO dto = new TestPlanDTO().id(9L);
		when(assembler.toModel(plan)).thenReturn(model);
		when(testPlanMapper.toDTO(model)).thenReturn(dto);

		TestPlanDTO result = facade.mapToTestPlanDTO(plan);

		assertSame(dto, result);
		verify(assembler).toModel(plan);
		verify(testPlanMapper).toDTO(model);
	}

	@Test
	void mapToPagedTestPlanDTO_shouldBuildContextsAndDelegate() {
		Long applicationId = 1L;
		Long stageId = 2L;
		PersistedTestPlan first = new PersistedTestPlan(9L, 11L, 12L, "one");
		PersistedTestPlan second = new PersistedTestPlan(10L, 13L, 14L, "two");
		PagedResult<PersistedTestPlan> page = new PagedResult<>(List.of(first, second), 0, 20, 2);
		when(testPlanDatabaseService.findTestPlanIdsWithActions(
				new TestPlanDatabaseService.TestPlanActions(applicationId, stageId, List.of(9L, 10L))))
			.thenReturn(Set.of(9L));

		PagedModel<TestPlanModel> pagedModel = PagedModel.of(List.of(new TestPlanModel(9L, "one")),
				new PagedModel.PageMetadata(20, 0, 2, 1));
		PagedTestPlansDTO dto = new PagedTestPlansDTO();
		when(pagedModelAssembler.toModel(any(PagedTestPlanContext.class))).thenReturn(pagedModel);
		when(pagedTestPlanMapper.toPagedDto(pagedModel)).thenReturn(dto);

		PagedTestPlansDTO result = facade.mapToPagedTestPlanDTO(applicationId, stageId, page);

		ArgumentCaptor<PagedTestPlanContext> contextCaptor = ArgumentCaptor.forClass(PagedTestPlanContext.class);
		verify(pagedModelAssembler).toModel(contextCaptor.capture());
		PagedTestPlanContext captured = contextCaptor.getValue();
		assertEquals(applicationId, captured.applicationId());
		assertEquals(stageId, captured.stageId());
		assertEquals(2, captured.contexts().getContent().size());
		PersistedTestPlanContext firstContext = captured.contexts().getContent().getFirst();
		PersistedTestPlanContext secondContext = captured.contexts().getContent().get(1);
		assertSame(first, firstContext.persistedTestPlan());
		assertSame(second, secondContext.persistedTestPlan());
		assertTrue(firstContext.hasTestSteps());
		assertFalse(secondContext.hasTestSteps());
		verify(testPlanDatabaseService).findTestPlanIdsWithActions(
				new TestPlanDatabaseService.TestPlanActions(applicationId, stageId, List.of(9L, 10L)));
		verify(testPlanDatabaseService, never()).hasActions(anyLong());
		assertSame(dto, result);
	}

}
