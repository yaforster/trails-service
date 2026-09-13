package io.github.yaforster.trails.adapter.api.rest.testpath;

import io.github.yaforster.trails.adapter.api.rest.model.PagedTestPathResultDTO;
import io.github.yaforster.trails.adapter.api.rest.model.TestPathResultDTO;
import io.github.yaforster.trails.adapter.api.rest.common.PagedResultPageAdapter;
import io.github.yaforster.trails.adapter.api.rest.testpath.assembler.PersistedTestPathResultContext;
import io.github.yaforster.trails.adapter.api.rest.testpath.assembler.TestPathResultModelAssembler;
import io.github.yaforster.trails.adapter.api.rest.testpath.mapper.PagedTestPathResultMapper;
import io.github.yaforster.trails.adapter.api.rest.testpath.model.TestPathResultModel;
import io.github.yaforster.trails.adapter.api.rest.testset.TestSetController;
import io.github.yaforster.trails.core.persisted.PersistedTestPathResult;
import io.github.yaforster.trails.core.PagedResult;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.web.PagedResourcesAssembler;
import org.springframework.hateoas.PagedModel;
import org.springframework.stereotype.Component;

import static org.springframework.hateoas.server.core.DummyInvocationUtils.methodOn;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;

@Component
@AllArgsConstructor
public class TestPathHATEOASFacade {

	private final TestPathResultModelAssembler assembler;

	private final PagedResourcesAssembler<PersistedTestPathResultContext> pagedAssembler;

	private final PagedTestPathResultMapper pagedTestPathResultMapper;

	public TestPathResultDTO toDTOInHierarchy(Long applicationId, Long stageId, Long testRunId,
			PersistedTestPathResult entity) {
		PersistedTestPathResultContext context = PersistedTestPathResultContext.of(applicationId, stageId, testRunId,
				entity.testSetResultId(), entity);
		TestPathResultModel model = assembler.toModel(context);
		return pagedTestPathResultMapper.toTestPathResultDto(model);
	}

	public PagedTestPathResultDTO toPagedDTOInHierarchy(Long applicationId, Long stageId, Long testRunId,
			Long testSetResultId, PagedResult<PersistedTestPathResult> pagedResults) {
		Page<PersistedTestPathResultContext> contexts = PagedResultPageAdapter
			.toSpringPage(pagedResults.map(entity -> PersistedTestPathResultContext.of(applicationId, stageId,
					testRunId, testSetResultId, entity)));

		PagedModel<TestPathResultModel> model = pagedAssembler.toModel(contexts, assembler);
		addHierarchyPagingLinks(model, applicationId, stageId, testRunId, testSetResultId, pagedResults.page(),
				pagedResults.size(), pagedResults.totalPages());

		return pagedTestPathResultMapper.toPagedDto(model);
	}

	private void addHierarchyPagingLinks(PagedModel<?> model, Long applicationId, Long stageId, Long testRunId,
			Long testSetResultId, int page, int size, int totalPages) {
		int lastPage = Math.max(totalPages - 1, 0);

		model.add(linkTo(methodOn(TestPathController.class).listPathResultsInTestSet(applicationId, stageId, testRunId,
				testSetResultId, page, size))
			.withSelfRel());

		model.add(linkTo(methodOn(TestSetController.class).getBrowserResultInTestRun(testRunId, applicationId, stageId,
				testSetResultId))
			.withRel("testSetResult"));

		model.add(linkTo(methodOn(TestPathController.class).listPathResultsInTestSet(applicationId, stageId, testRunId,
				testSetResultId, 0, size))
			.withRel("first"));

		model.add(linkTo(methodOn(TestPathController.class).listPathResultsInTestSet(applicationId, stageId, testRunId,
				testSetResultId, lastPage, size))
			.withRel("last"));

		if (page > 0) {
			model.add(linkTo(methodOn(TestPathController.class).listPathResultsInTestSet(applicationId, stageId,
					testRunId, testSetResultId, page - 1, size))
				.withRel("prev"));
		}

		if (page < lastPage) {
			model.add(linkTo(methodOn(TestPathController.class).listPathResultsInTestSet(applicationId, stageId,
					testRunId, testSetResultId, page + 1, size))
				.withRel("next"));
		}
	}

}
