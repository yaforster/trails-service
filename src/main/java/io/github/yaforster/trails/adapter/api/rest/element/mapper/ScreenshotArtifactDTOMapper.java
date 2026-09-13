package io.github.yaforster.trails.adapter.api.rest.element.mapper;

import io.github.yaforster.trails.adapter.api.rest.element.ElementController;
import io.github.yaforster.trails.adapter.api.rest.model.ArtifactDTO;
import io.github.yaforster.trails.adapter.api.rest.model.LinkDTO;
import io.github.yaforster.trails.core.data.TrailsScreenshotFile;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;

import static org.springframework.hateoas.server.core.DummyInvocationUtils.methodOn;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;

@Component
public class ScreenshotArtifactDTOMapper {

	public ArtifactDTO toDTO(Long applicationId, Long stageId, Long elementId, Long screenshotId,
			TrailsScreenshotFile file) {
		ArtifactDTO artifactDTO = new ArtifactDTO().id(screenshotId)
			.type(ArtifactDTO.TypeEnum.SCREENSHOT)
			.filename(file.filename());
		artifactDTO
			.putLinksItem("self", new LinkDTO()
				.href(linkTo(methodOn(ElementController.class).getElementScreenshot(applicationId, stageId, elementId))
					.toUri()
					.toString())
				.method(HttpMethod.GET.name())
				.templated(false));
		return artifactDTO;
	}

}
