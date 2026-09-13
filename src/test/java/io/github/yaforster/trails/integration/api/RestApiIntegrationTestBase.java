package io.github.yaforster.trails.integration.api;

import io.github.yaforster.trails.core.TrailsTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockMultipartHttpServletRequestBuilder;
import org.springframework.test.web.servlet.result.JsonPathResultMatchers;
import org.springframework.web.multipart.MultipartFile;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@AutoConfigureMockMvc
@TestPropertySource(properties = {
		"spring.datasource.url=jdbc:h2:mem:trails-it;MODE=MySQL;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE",
		"spring.datasource.driver-class-name=org.h2.Driver", "spring.datasource.username=sa",
		"spring.datasource.password=", "spring.jpa.hibernate.ddl-auto=none",
		"service.api.error-handling.add-stacktrace-to-response=false" })
abstract class RestApiIntegrationTestBase extends TrailsTest {

	protected static final int DEFAULT_PAGE = 0;

	protected static final int DEFAULT_SIZE = 20;

	private static final String API_PREFIX = "/api";

	@Autowired
	protected MockMvc mockMvc;

	private static String apiPath(String path) {
		if (path == null || path.isBlank()) {
			return API_PREFIX;
		}
		if (path.startsWith(API_PREFIX + "/") || path.equals(API_PREFIX)) {
			return path;
		}
		if (path.startsWith("/")) {
			return API_PREFIX + path;
		}
		return API_PREFIX + "/" + path;
	}

	private static void expectOptionalPageLink(ResultActions actions, String linkPath, Integer page) throws Exception {
		JsonPathResultMatchers linkMatcher = jsonPath(linkPath);
		if (page == null) {
			actions.andExpect(linkMatcher.doesNotExist());
			return;
		}
		actions.andExpect(jsonPath(linkPath + ".href", containsString("page=" + page)));
	}

	protected ResultActions performGet(String pathAndQuery) throws Exception {
		return mockMvc.perform(get(apiPath(pathAndQuery)).accept(MediaType.APPLICATION_JSON));
	}

	protected ResultActions performGet(String pathAndQuery, MediaType accept) throws Exception {
		return mockMvc.perform(get(apiPath(pathAndQuery)).accept(accept));
	}

	protected ResultActions performPutJson(String path, String jsonBody) throws Exception {
		return performPutJson(path, jsonBody, MediaType.APPLICATION_JSON);
	}

	protected ResultActions performPutJson(String path, String jsonBody, MediaType accept) throws Exception {
		return mockMvc
			.perform(put(apiPath(path)).contentType(MediaType.APPLICATION_JSON).accept(accept).content(jsonBody));
	}

	protected ResultActions performPostJson(String path, String jsonBody) throws Exception {
		return mockMvc.perform(post(apiPath(path)).contentType(MediaType.APPLICATION_JSON)
			.accept(MediaType.APPLICATION_JSON)
			.content(jsonBody));
	}

	protected ResultActions performDelete(String path) throws Exception {
		return mockMvc.perform(delete(apiPath(path)).accept(MediaType.APPLICATION_JSON));
	}

	protected ResultActions performPutMultipart(String path, MultipartFile file) throws Exception {
		return performPutMultipart(path, file, MediaType.APPLICATION_JSON);
	}

	protected ResultActions performPutMultipart(String path, MultipartFile file, MediaType accept) throws Exception {
		MockMultipartFile requestFile = new MockMultipartFile("file", file.getOriginalFilename(), file.getContentType(),
				file.getBytes());
		MockMultipartHttpServletRequestBuilder builder = multipart(apiPath(path)).file(requestFile);
		return mockMvc.perform(builder.with(request -> {
			request.setMethod("PUT");
			return request;
		}).accept(accept));
	}

	protected ResultActions expectPage(ResultActions actions, int page, int size, int totalElements, int totalPages)
			throws Exception {
		return actions.andExpect(jsonPath("$.page").value(page))
			.andExpect(jsonPath("$.size").value(size))
			.andExpect(jsonPath("$.totalElements").value(totalElements))
			.andExpect(jsonPath("$.totalPages").value(totalPages));
	}

	protected ResultActions expectDefaultSelfPagingParams(ResultActions actions) throws Exception {
		return actions.andExpect(jsonPath("$._links.self.href", containsString("page=" + DEFAULT_PAGE)))
			.andExpect(jsonPath("$._links.self.href", containsString("size=" + DEFAULT_SIZE)));
	}

	protected ResultActions expectPagingLinks(ResultActions actions, Integer firstPage, Integer lastPage,
			Integer prevPage, Integer nextPage) throws Exception {
		if (firstPage != null) {
			actions.andExpect(jsonPath("$._links.first.href", containsString("page=" + firstPage)));
		}
		if (lastPage != null) {
			actions.andExpect(jsonPath("$._links.last.href", containsString("page=" + lastPage)));
		}
		expectOptionalPageLink(actions, "$._links.prev", prevPage);
		expectOptionalPageLink(actions, "$._links.next", nextPage);
		return actions;
	}

	protected ResultActions expectEmptyItems(ResultActions actions) throws Exception {
		return actions.andExpect(jsonPath("$.items").doesNotExist());
	}

}
