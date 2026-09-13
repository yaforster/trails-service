package io.github.yaforster.trails.adapter.api.rest.testdata;

import io.github.yaforster.trails.adapter.api.ResourceAuthorization;
import io.github.yaforster.trails.adapter.api.rest.deletion.DatabaseDeletionResultMapper;
import io.github.yaforster.trails.adapter.api.rest.deletion.DatabaseDeletionResultModelMapper;
import io.github.yaforster.trails.adapter.api.rest.hateoas.HATEOASFacade;
import io.github.yaforster.trails.adapter.api.rest.hateoas.HATEOASMapper;
import io.github.yaforster.trails.adapter.api.rest.model.*;
import io.github.yaforster.trails.core.data.TestDataEntry;
import io.github.yaforster.trails.core.data.TestDataSet;
import io.github.yaforster.trails.core.PagedResult;
import io.github.yaforster.trails.core.definition.TestDataSetDefinition;
import io.github.yaforster.trails.core.deletion.DatabaseDeletionResult;
import org.springframework.data.domain.Page;
import org.springframework.hateoas.Links;
import org.springframework.stereotype.Component;

import java.net.URI;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;

import static io.github.yaforster.trails.adapter.api.rest.hateoas.HATEOASLinks.*;
import static org.springframework.hateoas.server.core.DummyInvocationUtils.methodOn;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;

@Component
public class TestDataHATEOASFacade extends HATEOASFacade implements HATEOASMapper {

	private final ResourceAuthorization resourceAuthorization;

	public TestDataHATEOASFacade(DatabaseDeletionResultMapper databaseDeletionResultMapper,
			DatabaseDeletionResultModelMapper databaseDeletionResultModelMapper,
			ResourceAuthorization resourceAuthorization) {
		super(databaseDeletionResultMapper, databaseDeletionResultModelMapper);
		this.resourceAuthorization = resourceAuthorization;
	}

	public TestDataSetDefinition toDomain(TestDataSetDefinitionDTO dto) {
		return new TestDataSetDefinition(dto.getLabel(),
				dto.getValues().stream().map(value -> new TestDataEntry(value.getKey(), value.getValue())).toList());
	}

	public TestDataSetDTO toDTO(TestDataSet testDataSet) {
		TestDataSetDTO dto = new TestDataSetDTO();
		dto.setId(testDataSet.id());
		dto.setLabel(testDataSet.label());
		dto.setCreationDate(OffsetDateTime.ofInstant(testDataSet.creationDate(), ZoneOffset.UTC));
		dto.setRetired(testDataSet.retired());
		dto.setValues(testDataSet.values().stream().map(this::toDTO).toList());
		List<org.springframework.hateoas.Link> links = new java.util.ArrayList<>();
		links.add(linkTo(methodOn(TestDataController.class).getTestData(testDataSet.id(), testDataSet.retired()))
			.withSelfRel());
		if (resourceAuthorization.canAccessTestData()) {
			links.add(put(linkTo(methodOn(TestDataController.class).updateTestData(testDataSet.id(), null))
				.withRel("update")));
		}
		links.add(linkTo(methodOn(TestDataController.class).listTestData(0, 20, false)).withRel("collection"));
		if (resourceAuthorization.canAccessTestData() && testDataSet.retired()) {
			links.add(post(
					linkTo(methodOn(TestDataController.class).restoreTestData(testDataSet.id())).withRel("restore")));
		}
		else if (resourceAuthorization.canAccessTestData()) {
			links.add(delete(
					linkTo(methodOn(TestDataController.class).deleteTestData(testDataSet.id())).withRel("delete")));
		}
		dto.setLinks(mapLinks(Links.of(links)));
		return dto;
	}

	public PagedTestDataSetDTO toPagedDTO(PagedResult<TestDataSet> page, boolean includeRetired) {
		PagedTestDataSetDTO dto = new PagedTestDataSetDTO();
		dto.setPage(page.page());
		dto.setSize(page.size());
		dto.setTotalElements((int) page.totalItems());
		dto.setTotalPages(page.totalPages());
		dto.setItems(page.items().stream().map(this::toDTO).toList());
		dto.setLinks(mapLinks(pagingLinks(page, includeRetired)));
		return dto;
	}

	public URI location(TestDataSet testDataSet) {
		return URI.create("/api/test-data/" + testDataSet.id());
	}

	public DatabaseDeletionResultDTO toDTO(DatabaseDeletionResult deletionResult) {
		return toDeletionDTO(deletionResult, deletionNotFoundModel -> deletionNotFoundModel
			.add(linkTo(methodOn(TestDataController.class).listTestData(0, 20, false)).withRel("collection")));
	}

	private TestDataEntryDTO toDTO(TestDataEntry value) {
		TestDataEntryDTO dto = new TestDataEntryDTO();
		dto.setKey(value.key());
		dto.setValue(value.value());
		return dto;
	}

	private Links pagingLinks(PagedResult<TestDataSet> page, boolean includeRetired) {
		int currentPage = page.page();
		int size = page.size();
		int lastPage = Math.max(page.totalPages() - 1, 0);
		List<org.springframework.hateoas.Link> links = new java.util.ArrayList<>();
		links.add(linkTo(methodOn(TestDataController.class).listTestData(currentPage, size, includeRetired))
			.withSelfRel());
		if (resourceAuthorization.canAccessTestData()) {
			links.add(put(linkTo(methodOn(TestDataController.class).createNewTestData(null)).withRel("create")));
		}
		links.add(linkTo(methodOn(TestDataController.class).listTestData(0, size, includeRetired)).withRel("first"));
		links.add(linkTo(methodOn(TestDataController.class).listTestData(lastPage, size, includeRetired))
			.withRel("last"));
		if (currentPage > 0) {
			links.add(linkTo(methodOn(TestDataController.class).listTestData(currentPage - 1, size, includeRetired))
				.withRel("prev"));
		}
		if (currentPage < lastPage) {
			links.add(linkTo(methodOn(TestDataController.class).listTestData(currentPage + 1, size, includeRetired))
				.withRel("next"));
		}
		return Links.of(links);
	}

}
