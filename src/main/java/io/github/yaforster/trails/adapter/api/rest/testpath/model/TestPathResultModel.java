package io.github.yaforster.trails.adapter.api.rest.testpath.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.hateoas.RepresentationModel;

@AllArgsConstructor
@Getter
public class TestPathResultModel extends RepresentationModel<TestPathResultModel> {

	private final Long id;

}
