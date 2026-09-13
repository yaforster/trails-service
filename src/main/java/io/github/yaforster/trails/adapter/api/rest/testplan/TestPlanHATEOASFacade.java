package io.github.yaforster.trails.adapter.api.rest.testplan;

import io.github.yaforster.trails.adapter.api.rest.action.mapper.ActionMapper;
import io.github.yaforster.trails.adapter.api.rest.deletion.DatabaseDeletionResultContext;
import io.github.yaforster.trails.adapter.api.rest.deletion.DatabaseDeletionResultMapper;
import io.github.yaforster.trails.adapter.api.rest.deletion.DatabaseDeletionResultModelMapper;
import io.github.yaforster.trails.adapter.api.rest.common.PagedResultPageAdapter;
import io.github.yaforster.trails.adapter.api.rest.hateoas.HATEOASFacade;
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
import io.github.yaforster.trails.core.PagedResult;
import io.github.yaforster.trails.core.data.TestPlan;
import io.github.yaforster.trails.core.definition.TestPlanDefinition;
import io.github.yaforster.trails.core.deletion.DatabaseDeletionResult;
import io.github.yaforster.trails.core.persisted.PersistedTestPlan;
import io.github.yaforster.trails.core.test.Action;
import org.springframework.data.domain.Page;
import org.springframework.hateoas.PagedModel;
import org.springframework.stereotype.Component;

import java.util.List;
import java.math.BigDecimal;
import java.util.Set;
import io.github.yaforster.trails.core.definition.TestPlanGroup;

import static org.springframework.hateoas.server.core.DummyInvocationUtils.methodOn;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;

/**
 * API composition facade for test plan resources. It intentionally aggregates mapping,
 * deletion-result shaping and HATEOAS assembly for the test plan API surface in a single
 * place to keep response behavior uniform.
 */
@Component
@SuppressWarnings("OverlyCoupledClass")
public class TestPlanHATEOASFacade extends HATEOASFacade {

	private final ActionMapper actionMapper;

	private final PersistedTestPlanDTOMapper persistedTestPlanMapper;

	private final TestPlanModelAssembler assembler;

	private final PagedTestPlanModelAssembler pagedModelAssembler;

	private final TestPlanMapper testPlanMapper;

	private final TestPlanDatabaseService testPlanDatabaseService;

	private final PagedTestPlanMapper pagedTestPlanMapper;

	public TestPlanHATEOASFacade(ActionMapper actionMapper, PersistedTestPlanDTOMapper persistedTestPlanMapper,
			TestPlanModelAssembler assembler, PagedTestPlanModelAssembler pagedModelAssembler,
			DatabaseDeletionResultMapper databaseDeletionResultMapper,
			DatabaseDeletionResultModelMapper databaseDeletionResultModelMapper, TestPlanMapper testPlanMapper,
			TestPlanDatabaseService testPlanDatabaseService, PagedTestPlanMapper pagedTestPlanMapper) {
		super(databaseDeletionResultMapper, databaseDeletionResultModelMapper);
		this.actionMapper = actionMapper;
		this.persistedTestPlanMapper = persistedTestPlanMapper;
		this.assembler = assembler;
		this.pagedModelAssembler = pagedModelAssembler;
		this.testPlanMapper = testPlanMapper;
		this.testPlanDatabaseService = testPlanDatabaseService;
		this.pagedTestPlanMapper = pagedTestPlanMapper;
	}

	public TestPlanDefinition fromDTO(Long applicationId, Long stageId, TestPlanDefinitionDTO dto) {
		List<Action> actions = mapToActions(dto.getTestSteps());
		List<TestPlanGroup> groups = dto.getGroups() == null ? List.of()
				: dto.getGroups()
					.stream()
					.map(group -> new TestPlanGroup(group.getLabel(), BigDecimal.valueOf(group.getxCoordinate()),
							BigDecimal.valueOf(group.getyCoordinate()), BigDecimal.valueOf(group.getWidthPixels()),
							BigDecimal.valueOf(group.getHeightPixels()),
							group.getActionReferenceIds() == null ? List.of() : group.getActionReferenceIds()))
					.toList();
		return new TestPlanDefinition(applicationId, stageId, dto.getLabel(), actions, groups);
	}

	private List<Action> mapToActions(List<ActionDefinitionDTO> testSteps) {
		return testSteps.stream().map(actionMapper::fromDefinitionDTO).toList();
	}

	public PersistedTestPlanDTO toDTO(Long applicationId, Long stageId, PersistedTestPlan persistedTestPlan,
			boolean hasActions) {
		PersistedTestPlanContext context = new PersistedTestPlanContext(applicationId, stageId, persistedTestPlan,
				hasActions);
		PersistedTestPlanModel model = assembler.toModel(context);
		return persistedTestPlanMapper.toDTO(model);
	}

	private DatabaseDeletionResultDTO toDTO(DatabaseDeletionResultContext context) {
		DatabaseDeletionResult deletionResult = context.deletionResult();
		Long applicationId = context.applicationId();
		Long stageId = context.stageId();
		return toDeletionDTO(deletionResult,
				deletionNotFoundModel -> deletionNotFoundModel
					.add(linkTo(methodOn(TestPlanController.class).listTestPlans(applicationId, stageId, 0, 20, false))
						.withRel("collection")));
	}

	public DatabaseDeletionResultDTO toDTO(Long applicationId, Long stageId, DatabaseDeletionResult deletionResult) {
		DatabaseDeletionResultContext context = new DatabaseDeletionResultContext(applicationId, stageId,
				deletionResult);
		return toDTO(context);
	}

	public TestPlanDTO mapToTestPlanDTO(TestPlan testPlan) {
		TestPlanModel model = assembler.toModel(testPlan);
		return testPlanMapper.toDTO(model);
	}

	public PagedTestPlansDTO mapToPagedTestPlanDTO(Long applicationId, Long stageId,
			PagedResult<PersistedTestPlan> pagedTestPlans) {
		Set<Long> testPlanIdsWithActions = testPlanDatabaseService
			.findTestPlanIdsWithActions(new TestPlanDatabaseService.TestPlanActions(applicationId, stageId,
					pagedTestPlans.items().stream().map(PersistedTestPlan::id).toList()));
		Page<PersistedTestPlanContext> contexts = PagedResultPageAdapter.toSpringPage(pagedTestPlans.map(testPlan -> {
			boolean hasActions = testPlanIdsWithActions.contains(testPlan.id());
			return new PersistedTestPlanContext(applicationId, stageId, testPlan, hasActions);
		}));
		PagedTestPlanContext context = new PagedTestPlanContext(applicationId, stageId, contexts);
		PagedModel<TestPlanModel> model = pagedModelAssembler.toModel(context);
		return pagedTestPlanMapper.toPagedDto(model);
	}

}
