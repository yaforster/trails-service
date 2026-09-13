package io.github.yaforster.trails.adapter.api.rest.element.model;

import io.github.yaforster.trails.core.data.ElementType;
import io.github.yaforster.trails.core.test.action.element.LocatorType;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.hateoas.RepresentationModel;

@AllArgsConstructor
@Getter
public class ElementModel extends RepresentationModel<ElementModel> {

	private Long id;

	private ElementType type;

	private String label;

	private String locatorString;

	private LocatorType locatorType;

	private boolean retired;

	public ElementModel(Long id, ElementType type, String label, String locatorString, LocatorType locatorType) {
		this(id, type, label, locatorString, locatorType, false);
	}

}
