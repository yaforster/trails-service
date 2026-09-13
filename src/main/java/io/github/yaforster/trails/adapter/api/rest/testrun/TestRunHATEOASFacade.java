package io.github.yaforster.trails.adapter.api.rest.testrun;

import io.github.yaforster.trails.adapter.api.rest.model.*;
import io.github.yaforster.trails.adapter.api.rest.testplan.TestPlanController;
import io.github.yaforster.trails.adapter.api.rest.testrun.assembler.TestRunResultAssembler;
import io.github.yaforster.trails.adapter.api.rest.testrun.assembler.TestRunResultContext;
import io.github.yaforster.trails.adapter.api.rest.testrun.assembler.TestRunStatisticsAssembler;
import io.github.yaforster.trails.adapter.api.rest.testrun.assembler.TestRunStatisticsContext;
import io.github.yaforster.trails.adapter.api.rest.testrun.mapper.PagedTestRunResultMapper;
import io.github.yaforster.trails.adapter.api.rest.testrun.mapper.TestRunStatisticsMapper;
import io.github.yaforster.trails.adapter.api.rest.testrun.model.TestRunResultModel;
import io.github.yaforster.trails.adapter.api.rest.testrun.model.TestRunStatisticsModel;
import io.github.yaforster.trails.adapter.api.rest.common.PagedResultPageAdapter;
import io.github.yaforster.trails.core.PagedResult;
import io.github.yaforster.trails.core.persisted.PersistedTestRunHistory;
import io.github.yaforster.trails.core.persisted.PersistedTestRunResult;
import io.github.yaforster.trails.core.persisted.PersistedTestRunStatistics;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.web.PagedResourcesAssembler;
import org.springframework.hateoas.PagedModel;
import org.springframework.stereotype.Component;

import java.sql.Timestamp;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.LinkedHashMap;
import java.util.Map;

import static org.springframework.hateoas.server.core.DummyInvocationUtils.methodOn;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;

@Component
@AllArgsConstructor
public class TestRunHATEOASFacade implements io.github.yaforster.trails.adapter.api.rest.hateoas.HATEOASMapper {

	private final TestRunResultAssembler assembler;

	private final PagedResourcesAssembler<TestRunResultContext> pagedAssembler;

	private final PagedTestRunResultMapper pagedTestRunResultMapper;

	private final TestRunStatisticsAssembler statisticsAssembler;

	private final TestRunStatisticsMapper testRunStatisticsMapper;

	public PersistedTestRunResultDTO toDTO(PersistedTestRunResult result) {
		TestRunResultContext context = assembler.toContext(result);
		TestRunResultModel model = assembler.toModel(context);
		return pagedTestRunResultMapper.toPersistedTestRunResultDto(model);
	}

	public PagedTestRunDTO toPagedDTO(Long applicationId, Long stageId,
			PagedResult<PersistedTestRunResult> pagedTestRuns) {
		Page<TestRunResultContext> contexts = PagedResultPageAdapter
			.toSpringPage(pagedTestRuns.map(assembler::toContext));

		PagedModel<TestRunResultModel> model = pagedAssembler.toModel(contexts, assembler);
		addPagingLinks(model, applicationId, stageId, pagedTestRuns.page(), pagedTestRuns.size(),
				pagedTestRuns.totalPages());

		return pagedTestRunResultMapper.toPagedDto(model);
	}

	public TestRunStatisticsDTO toStatisticsDTO(PersistedTestRunStatistics statistics) {
		TestRunStatisticsContext context = statisticsAssembler.toContext(statistics);
		TestRunStatisticsModel model = statisticsAssembler.toModel(context);
		return testRunStatisticsMapper.toDTO(model);
	}

	public PagedTestRunHistoryDTO toHistoryDTO(PersistedTestRunHistory history) {
		PagedResult<PersistedTestRunResult> page = history.items();
		return new PagedTestRunHistoryDTO().page(page.page())
			.size(page.size())
			.totalElements((int) page.totalItems())
			.totalPages(page.totalPages())
			.applicationId(history.applicationId())
			.stageId(history.stageId())
			.testPlanId(history.testPlanId())
			.totalRuns(history.totalRuns())
			.successfulRuns(history.successfulRuns())
			.partialSuccessRuns(history.partialSuccessRuns())
			.failedRuns(history.failedRuns())
			.items(page.items().stream().map(this::toHistoryPointDTO).toList())
			.links(historyLinks(history));
	}

	private TestRunHistoryPointDTO toHistoryPointDTO(PersistedTestRunResult result) {
		return new TestRunHistoryPointDTO().id(result.id())
			.timestamp(toOffsetDateTime(result.timestamp()))
			.indicator(ResultIndicatorDTO.fromValue(result.status().name()))
			.label(result.label());
	}

	private Map<String, LinkDTO> historyLinks(PersistedTestRunHistory history) {
		PagedResult<PersistedTestRunResult> page = history.items();
		int currentPage = page.page();
		int size = page.size();
		int lastPage = Math.max(page.totalPages() - 1, 0);
		Map<String, LinkDTO> links = new LinkedHashMap<>();
		links.put("self", toDTO(linkTo(methodOn(TestRunController.class).getTestRunHistory(history.applicationId(),
				history.stageId(), history.testPlanId(), currentPage, size))
			.withSelfRel()));
		links.put("first", toDTO(linkTo(methodOn(TestRunController.class).getTestRunHistory(history.applicationId(),
				history.stageId(), history.testPlanId(), 0, size))
			.withRel("first")));
		links.put("last", toDTO(linkTo(methodOn(TestRunController.class).getTestRunHistory(history.applicationId(),
				history.stageId(), history.testPlanId(), lastPage, size))
			.withRel("last")));
		if (currentPage > 0) {
			links.put("prev", toDTO(linkTo(methodOn(TestRunController.class).getTestRunHistory(history.applicationId(),
					history.stageId(), history.testPlanId(), currentPage - 1, size))
				.withRel("prev")));
		}
		if (currentPage < lastPage) {
			links.put("next", toDTO(linkTo(methodOn(TestRunController.class).getTestRunHistory(history.applicationId(),
					history.stageId(), history.testPlanId(), currentPage + 1, size))
				.withRel("next")));
		}
		links.put("statistics",
				toDTO(linkTo(methodOn(TestRunController.class).getTestRunStatistics(history.applicationId(),
						history.stageId(), history.testPlanId()))
					.withRel("statistics")));
		links.put("testPlan", toDTO(linkTo(methodOn(TestPlanController.class).getTestPlan(history.applicationId(),
				history.stageId(), history.testPlanId(), false))
			.withRel("testPlan")));
		return links;
	}

	private static OffsetDateTime toOffsetDateTime(Timestamp timestamp) {
		if (timestamp == null) {
			return null;
		}
		return timestamp.toInstant().atOffset(ZoneOffset.UTC);
	}

	private void addPagingLinks(PagedModel<?> model, Long applicationId, Long stageId, int page, int size,
			int totalPages) {
		int lastPage = Math.max(totalPages - 1, 0);

		model.add(linkTo(methodOn(TestRunController.class).listTestRuns(applicationId, stageId, page, size))
			.withSelfRel());

		model.add(linkTo(methodOn(TestRunController.class).listTestRuns(applicationId, stageId, 0, size))
			.withRel("first"));

		model.add(linkTo(methodOn(TestRunController.class).listTestRuns(applicationId, stageId, lastPage, size))
			.withRel("last"));

		if (page > 0) {
			model.add(linkTo(methodOn(TestRunController.class).listTestRuns(applicationId, stageId, page - 1, size))
				.withRel("prev"));
		}

		if (page < lastPage) {
			model.add(linkTo(methodOn(TestRunController.class).listTestRuns(applicationId, stageId, page + 1, size))
				.withRel("next"));
		}
	}

}
