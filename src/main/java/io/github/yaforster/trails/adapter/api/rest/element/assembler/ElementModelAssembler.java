package io.github.yaforster.trails.adapter.api.rest.element.assembler;

import io.github.yaforster.trails.adapter.api.ResourceAuthorization;
import io.github.yaforster.trails.adapter.api.rest.element.ElementController;
import io.github.yaforster.trails.adapter.api.rest.element.model.ElementModel;
import io.github.yaforster.trails.core.persisted.PersistedElement;
import jakarta.annotation.Nonnull;
import lombok.AllArgsConstructor;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.server.RepresentationModelAssembler;
import org.springframework.stereotype.Component;

import static io.github.yaforster.trails.adapter.api.rest.hateoas.HATEOASLinks.*;
import static org.springframework.hateoas.server.core.DummyInvocationUtils.methodOn;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;

@Component
@AllArgsConstructor
public class ElementModelAssembler implements RepresentationModelAssembler<PersistedElementContext, ElementModel> {

	private final ResourceAuthorization resourceAuthorization;

	@Override
	public @Nonnull ElementModel toModel(@Nonnull PersistedElementContext elementContext) {
		PersistedElement persistedElement = elementContext.persistedElement();
		ElementModel model = new ElementModel(persistedElement.id(), persistedElement.type(), persistedElement.label(),
				persistedElement.locator(), persistedElement.locatorType(), persistedElement.retired());
		model.add(linkTo(methodOn(ElementController.class).listElements(elementContext.applicationID(),
				elementContext.stageID(), 0, 20, false))
			.withRel("collection"));

		if (elementContext.screenshotID().isPresent()) {
			model.add(linkTo(methodOn(ElementController.class).getElementScreenshot(elementContext.applicationID(),
					elementContext.stageID(), persistedElement.id()))
				.withRel("screenshot"));
		}

		model.add(linkTo(methodOn(ElementController.class).getElement(elementContext.applicationID(),
				elementContext.stageID(), persistedElement.id(), false))
			.withSelfRel());

		if (resourceAuthorization.canManageResources() && !persistedElement.retired()) {
			model.add(put(linkTo(methodOn(ElementController.class).updateElement(elementContext.applicationID(),
					elementContext.stageID(), persistedElement.id(), null))
				.withRel("update")));
			model.add(
					put(linkTo(methodOn(ElementController.class).uploadElementScreenshot(elementContext.applicationID(),
							elementContext.stageID(), persistedElement.id(), null))
						.withRel("uploadScreenshot")));
		}

		if (resourceAuthorization.canManageResources() && elementContext.screenshotID().isPresent()) {
			model.add(delete(
					linkTo(methodOn(ElementController.class).deleteElementScreenshot(elementContext.applicationID(),
							elementContext.stageID(), persistedElement.id()))
						.withRel("deleteScreenshot")));
		}

		if (resourceAuthorization.canManageResources() && persistedElement.retired()) {
			model.add(post(linkTo(methodOn(ElementController.class).restoreElement(elementContext.applicationID(),
					elementContext.stageID(), persistedElement.id()))
				.withRel("restore")));
		}
		else if (resourceAuthorization.canManageResources()) {
			model.add(delete(linkTo(methodOn(ElementController.class).deleteElement(elementContext.applicationID(),
					elementContext.stageID(), persistedElement.id()))
				.withRel("delete")));
			model.add(promoteLink(elementContext, persistedElement));
		}

		return model;
	}

	private Link promoteLink(PersistedElementContext elementContext, PersistedElement persistedElement) {
		String href = linkTo(methodOn(ElementController.class).getElement(elementContext.applicationID(),
				elementContext.stageID(), persistedElement.id(), false))
			.toUriComponentsBuilder()
			.replaceQuery(null)
			.path("/promote")
			.build()
			.toUriString() + "{?targetStageId}";
		return post(Link.of(href).withRel("promote"));
	}

}
