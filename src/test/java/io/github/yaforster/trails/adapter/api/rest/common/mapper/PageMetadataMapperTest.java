package io.github.yaforster.trails.adapter.api.rest.common.mapper;

import io.github.yaforster.trails.adapter.api.rest.common.mapper.PageMetadataMapper;
import io.github.yaforster.trails.adapter.api.rest.model.PageMetadataDTO;
import io.github.yaforster.trails.core.TrailsTest;
import org.instancio.InstancioApi;
import org.junit.jupiter.api.Test;
import org.springframework.hateoas.PagedModel;

import static org.instancio.Select.field;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class PageMetadataMapperTest extends TrailsTest {

	private final PageMetadataMapper mapper = new PageMetadataMapper();

	@Test
	void mapModel_ShouldReturnWhenModelIsNull() {
		getInstancioOf(PageMetadataDTO.class).stream().limit(TEST_REPETITIONS).forEach(dto -> {
			Integer initialPage = dto.getPage();
			Integer initialSize = dto.getSize();
			Integer initialTotalElements = dto.getTotalElements();
			Integer initialTotalPages = dto.getTotalPages();

			mapper.map((PagedModel<?>) null, dto);

			assertEquals(initialPage, dto.getPage());
			assertEquals(initialSize, dto.getSize());
			assertEquals(initialTotalElements, dto.getTotalElements());
			assertEquals(initialTotalPages, dto.getTotalPages());
		});
	}

	@Test
	void mapModel_ShouldReturnWhenDtoIsNull() {
		@SuppressWarnings("unchecked")
		PagedModel<Object> model = mock(PagedModel.class);
		PagedModel.PageMetadata metadata = randomMetadata();
		when(model.getMetadata()).thenReturn(metadata);

		assertDoesNotThrow(() -> mapper.map(model, null));
	}

	@Test
	void mapModel_ShouldReturnWhenMetadataIsNull() {
		@SuppressWarnings("unchecked")
		PagedModel<Object> model = mock(PagedModel.class);
		when(model.getMetadata()).thenReturn(null);

		nullablePageMetadataDto().stream().limit(TEST_REPETITIONS).forEach(dto -> {
			Integer initialPage = dto.getPage();
			Integer initialSize = dto.getSize();
			Integer initialTotalElements = dto.getTotalElements();
			Integer initialTotalPages = dto.getTotalPages();

			mapper.map(model, dto);

			assertEquals(initialPage, dto.getPage());
			assertEquals(initialSize, dto.getSize());
			assertEquals(initialTotalElements, dto.getTotalElements());
			assertEquals(initialTotalPages, dto.getTotalPages());
		});
	}

	@Test
	void mapModel_ShouldMapMetadataToDto() {
		@SuppressWarnings("unchecked")
		PagedModel<Object> model = mock(PagedModel.class);

		getInstancioOf(Integer.class).stream().limit(TEST_REPETITIONS).forEach(ignored -> {
			PagedModel.PageMetadata metadata = randomMetadata();
			when(model.getMetadata()).thenReturn(metadata);

			PageMetadataDTO dto = nullablePageMetadataDto().create();

			mapper.map(model, dto);

			assertEquals((int) metadata.getNumber(), dto.getPage());
			assertEquals((int) metadata.getSize(), dto.getSize());
			assertEquals((int) metadata.getTotalElements(), dto.getTotalElements());
			assertEquals((int) metadata.getTotalPages(), dto.getTotalPages());
		});
	}

	@Test
	void mapMetadata_ShouldReturnWhenMetadataIsNull() {
		nullablePageMetadataDto().stream().limit(TEST_REPETITIONS).forEach(dto -> {
			Integer initialPage = dto.getPage();
			Integer initialSize = dto.getSize();
			Integer initialTotalElements = dto.getTotalElements();
			Integer initialTotalPages = dto.getTotalPages();

			mapper.map((PagedModel.PageMetadata) null, dto);

			assertEquals(initialPage, dto.getPage());
			assertEquals(initialSize, dto.getSize());
			assertEquals(initialTotalElements, dto.getTotalElements());
			assertEquals(initialTotalPages, dto.getTotalPages());
		});
	}

	@Test
	void mapMetadata_ShouldReturnWhenDtoIsNull() {
		getInstancioOf(Integer.class).stream()
			.limit(TEST_REPETITIONS)
			.forEach(ignored -> assertDoesNotThrow(() -> mapper.map(randomMetadata(), null)));
	}

	@Test
	void mapMetadata_ShouldMapAllFields() {
		getInstancioOf(Integer.class).stream().limit(TEST_REPETITIONS).forEach(ignored -> {
			PagedModel.PageMetadata metadata = randomMetadata();
			PageMetadataDTO dto = nullablePageMetadataDto().create();

			mapper.map(metadata, dto);

			assertEquals((int) metadata.getNumber(), dto.getPage());
			assertEquals((int) metadata.getSize(), dto.getSize());
			assertEquals((int) metadata.getTotalElements(), dto.getTotalElements());
			assertEquals((int) metadata.getTotalPages(), dto.getTotalPages());
		});
	}

	private PagedModel.PageMetadata randomMetadata() {
		int size = getInstancioOf(Integer.class).create();
		int number = getInstancioOf(Integer.class).create();
		int totalElements = getInstancioOf(Integer.class).create();
		int totalPages = getInstancioOf(Integer.class).create();
		return new PagedModel.PageMetadata(size, number, totalElements, totalPages);
	}

	private InstancioApi<PageMetadataDTO> nullablePageMetadataDto() {
		return getInstancioOf(PageMetadataDTO.class).withNullable(field(PageMetadataDTO::getPage))
			.withNullable(field(PageMetadataDTO::getSize))
			.withNullable(field(PageMetadataDTO::getTotalElements))
			.withNullable(field(PageMetadataDTO::getTotalPages));
	}

}
