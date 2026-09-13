package io.github.yaforster.trails.adapter.api.rest.testset;

import io.github.yaforster.trails.adapter.api.rest.model.PagedTestSetResultDTO;
import io.github.yaforster.trails.adapter.api.rest.model.TestSetResultDTO;
import io.github.yaforster.trails.adapter.api.rest.common.PagedResultPageAdapter;
import io.github.yaforster.trails.adapter.api.rest.testrun.TestRunController;
import io.github.yaforster.trails.adapter.api.rest.testset.assembler.PersistedTestSetResultContext;
import io.github.yaforster.trails.adapter.api.rest.testset.assembler.TestSetResultModelAssembler;
import io.github.yaforster.trails.adapter.api.rest.testset.mapper.PagedTestSetResultMapper;
import io.github.yaforster.trails.adapter.api.rest.testset.model.TestSetResultModel;
import io.github.yaforster.trails.core.persisted.PersistedTestSetResult;
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
public class TestSetHATEOASFacade {

	private final TestSetResultModelAssembler assembler;

	private final PagedResourcesAssembler<PersistedTestSetResultContext> pagedAssembler;

	private final PagedTestSetResultMapper pagedTestSetResultMapper;

	public TestSetResultDTO toDTO(PersistedTestSetResult lookup) {
		PersistedTestSetResultContext context = new PersistedTestSetResultContext(lookup.applicationId(),
				lookup.stageId(), lookup.testRunId(), lookup);
		TestSetResultModel model = assembler.toModel(context);
		return pagedTestSetResultMapper.toTestSetResultDto(model);
	}

	public PagedTestSetResultDTO toPagedDTO(Long applicationId, Long stageId, Long testRunId,
			PagedResult<PersistedTestSetResult> pagedResults) {
		Page<PersistedTestSetResultContext> contexts = PagedResultPageAdapter.toSpringPage(pagedResults
			.map(entity -> new PersistedTestSetResultContext(applicationId, stageId, testRunId, entity)));

		PagedModel<TestSetResultModel> model = pagedAssembler.toModel(contexts, assembler);
		addPagingLinks(model, applicationId, stageId, testRunId, pagedResults.page(), pagedResults.size(),
				pagedResults.totalPages());

		return pagedTestSetResultMapper.toPagedDto(model);
	}

	private void addPagingLinks(PagedModel<?> model, Long applicationId, Long stageId, Long testRunId, int page,
			int size, int totalPages) {
		int lastPage = Math.max(totalPages - 1, 0);

		model.add(linkTo(
				methodOn(TestSetController.class).listBrowserResults(testRunId, applicationId, stageId, page, size))
			.withSelfRel());

		model.add(linkTo(methodOn(TestRunController.class).getTestRun(testRunId, applicationId, stageId))
			.withRel("testRun"));

		model.add(
				linkTo(methodOn(TestSetController.class).listBrowserResults(testRunId, applicationId, stageId, 0, size))
					.withRel("first"));

		model.add(linkTo(
				methodOn(TestSetController.class).listBrowserResults(testRunId, applicationId, stageId, lastPage, size))
			.withRel("last"));

		if (page > 0) {
			model.add(linkTo(methodOn(TestSetController.class).listBrowserResults(testRunId, applicationId, stageId,
					page - 1, size))
				.withRel("prev"));
		}

		if (page < lastPage) {
			model.add(linkTo(methodOn(TestSetController.class).listBrowserResults(testRunId, applicationId, stageId,
					page + 1, size))
				.withRel("next"));
		}
	}

}
