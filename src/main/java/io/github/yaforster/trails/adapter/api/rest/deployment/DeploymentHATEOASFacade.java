package io.github.yaforster.trails.adapter.api.rest.deployment;

import io.github.yaforster.trails.adapter.api.rest.hateoas.HATEOASMapper;
import io.github.yaforster.trails.adapter.api.rest.model.DeploymentDTO;
import io.github.yaforster.trails.adapter.api.rest.model.DeploymentDefinitionDTO;
import io.github.yaforster.trails.adapter.api.rest.model.LinkDTO;
import io.github.yaforster.trails.adapter.api.rest.model.PagedDeploymentDTO;
import io.github.yaforster.trails.core.definition.DeploymentDefinition;
import io.github.yaforster.trails.core.PagedResult;
import io.github.yaforster.trails.core.persisted.PersistedDeployment;
import org.springframework.data.domain.Page;
import org.springframework.hateoas.Links;
import org.springframework.stereotype.Component;

import java.net.URI;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.LinkedHashMap;
import java.util.Map;

import static org.springframework.hateoas.server.core.DummyInvocationUtils.methodOn;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;

@Component
public class DeploymentHATEOASFacade implements HATEOASMapper {

	public DeploymentDefinition toDomain(DeploymentDefinitionDTO dto) {
		return new DeploymentDefinition(dto.getVersion().trim(), dto.getDeployedAt().toInstant());
	}

	public DeploymentDTO toDTO(PersistedDeployment deployment) {
		return new DeploymentDTO().id(deployment.id())
			.applicationId(deployment.applicationId())
			.stageId(deployment.stageId())
			.version(deployment.version())
			.deployedAt(OffsetDateTime.ofInstant(deployment.deployedAt(), ZoneOffset.UTC))
			.links(mapLinks(Links.of(
					linkTo(methodOn(DeploymentController.class).getDeployment(deployment.applicationId(),
							deployment.stageId(), deployment.id()))
						.withSelfRel(),
					linkTo(methodOn(DeploymentController.class).listDeployments(deployment.applicationId(),
							deployment.stageId(), 0, 20))
						.withRel("collection"))));
	}

	public PagedDeploymentDTO toPagedDTO(Long applicationId, Long stageId, PagedResult<PersistedDeployment> page) {
		return new PagedDeploymentDTO().page(page.page())
			.size(page.size())
			.totalElements((int) page.totalItems())
			.totalPages(page.totalPages())
			.items(page.items().stream().map(this::toDTO).toList())
			.links(pagingLinks(applicationId, stageId, page));
	}

	public URI location(PersistedDeployment deployment) {
		return URI.create("/api/applications/%d/stages/%d/deployments/%d".formatted(deployment.applicationId(),
				deployment.stageId(), deployment.id()));
	}

	private Map<String, LinkDTO> pagingLinks(Long applicationId, Long stageId, PagedResult<PersistedDeployment> page) {
		int currentPage = page.page();
		int size = page.size();
		int lastPage = Math.max(page.totalPages() - 1, 0);
		Map<String, LinkDTO> links = new LinkedHashMap<>();
		links.put("self", toDTO(
				linkTo(methodOn(DeploymentController.class).listDeployments(applicationId, stageId, currentPage, size))
					.withSelfRel()));
		links.put("first",
				toDTO(linkTo(methodOn(DeploymentController.class).listDeployments(applicationId, stageId, 0, size))
					.withRel("first")));
		links.put("last", toDTO(
				linkTo(methodOn(DeploymentController.class).listDeployments(applicationId, stageId, lastPage, size))
					.withRel("last")));
		if (currentPage > 0) {
			links.put("prev", toDTO(linkTo(
					methodOn(DeploymentController.class).listDeployments(applicationId, stageId, currentPage - 1, size))
				.withRel("prev")));
		}
		if (currentPage < lastPage) {
			links.put("next", toDTO(linkTo(
					methodOn(DeploymentController.class).listDeployments(applicationId, stageId, currentPage + 1, size))
				.withRel("next")));
		}
		links.put("create",
				toDTO(linkTo(methodOn(DeploymentController.class).reportDeployment(applicationId, stageId, null))
					.withRel("create")));
		return links;
	}

}
