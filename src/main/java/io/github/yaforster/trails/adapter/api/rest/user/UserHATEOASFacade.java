package io.github.yaforster.trails.adapter.api.rest.user;

import io.github.yaforster.trails.adapter.api.rest.model.*;
import io.github.yaforster.trails.core.user.UserProfile;
import io.github.yaforster.trails.core.PagedResult;
import io.github.yaforster.trails.core.user.UserProfilePictureResource;
import io.github.yaforster.trails.core.user.UserProfileUpdate;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.springframework.hateoas.server.core.DummyInvocationUtils.methodOn;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;

@Component
@ConditionalOnProperty(name = "service.api.security.oauth2.enabled", havingValue = "true")
public class UserHATEOASFacade {

	public UserProfileDTO toDTO(UserProfile profile) {
		return new UserProfileDTO().userId(profile.userId())
			.firstName(profile.firstName())
			.lastName(profile.lastName())
			.email(profile.email())
			.phoneNumber(profile.phoneNumber())
			.links(profileLinks(profile.hasProfilePicture()));
	}

	public PagedUserProfileDTO toPagedDTO(PagedResult<UserProfile> profiles) {
		return new PagedUserProfileDTO().page(profiles.page())
			.size(profiles.size())
			.totalElements((int) profiles.totalItems())
			.totalPages(profiles.totalPages())
			.items(profiles.items().stream().map(this::toAdminDTO).toList())
			.links(pagingLinks(profiles.page(), profiles.size(), profiles.totalPages()));
	}

	public UserProfileUpdate toDomain(UserProfileUpdateDTO dto) {
		return new UserProfileUpdate(value(dto.getFirstName()), value(dto.getLastName()), value(dto.getEmail()),
				value(dto.getPhoneNumber()));
	}

	public ProfilePictureResourceDTO toDTO(UserProfilePictureResource resource) {
		return new ProfilePictureResourceDTO().href(linkTo(methodOn(UserController.class).getProfilePicture()).toUri())
			.contentType(resource.contentType())
			.updatedAt(resource.updatedAt());
	}

	private ProfileLinksDTO profileLinks(boolean hasProfilePicture) {
		ProfileLinksDTO links = new ProfileLinksDTO()
			.self(link(linkTo(methodOn(UserController.class).getProfile()).toUri().toString(), HttpMethod.GET))
			.update(link(linkTo(methodOn(UserController.class).updateProfile(null)).toUri().toString(), HttpMethod.PUT))
			.uploadProfilePicture(
					link(linkTo(methodOn(UserController.class).updateProfilePicture(null)).toUri().toString(),
							HttpMethod.PUT));
		if (hasProfilePicture) {
			links.getProfilePicture(link(linkTo(methodOn(UserController.class).getProfilePicture()).toUri().toString(),
					HttpMethod.GET));
			links.deleteProfilePicture(
					link(linkTo(methodOn(UserController.class).deleteProfilePicture()).toUri().toString(),
							HttpMethod.DELETE));
		}
		return links;
	}

	private UserProfileDTO toAdminDTO(UserProfile profile) {
		return new UserProfileDTO().userId(profile.userId())
			.firstName(profile.firstName())
			.lastName(profile.lastName())
			.email(profile.email())
			.phoneNumber(profile.phoneNumber())
			.links(adminProfileLinks(profile.userId()));
	}

	private ProfileLinksDTO adminProfileLinks(UUID userId) {
		LinkDTO deleteLink = link(linkTo(methodOn(UserController.class).deleteUserData(userId)).toUri().toString(),
				HttpMethod.DELETE);
		return new ProfileLinksDTO().self(deleteLink).deleteUserData(deleteLink);
	}

	private Map<String, LinkDTO> pagingLinks(int page, int size, int totalPages) {
		int lastPage = Math.max(totalPages - 1, 0);
		Map<String, LinkDTO> links = new java.util.LinkedHashMap<>();
		links.put("self",
				link(linkTo(methodOn(UserController.class).listUsers(page, size)).toUri().toString(), HttpMethod.GET));
		links.put("first",
				link(linkTo(methodOn(UserController.class).listUsers(0, size)).toUri().toString(), HttpMethod.GET));
		links.put("last", link(linkTo(methodOn(UserController.class).listUsers(lastPage, size)).toUri().toString(),
				HttpMethod.GET));
		if (page > 0) {
			links.put("prev", link(linkTo(methodOn(UserController.class).listUsers(page - 1, size)).toUri().toString(),
					HttpMethod.GET));
		}
		if (page < lastPage) {
			links.put("next", link(linkTo(methodOn(UserController.class).listUsers(page + 1, size)).toUri().toString(),
					HttpMethod.GET));
		}
		return links;
	}

	private LinkDTO link(String href, HttpMethod method) {
		return new LinkDTO().href(href).method(method.name()).templated(false);
	}

	private Optional<String> value(String value) {
		return Optional.ofNullable(value);
	}

}
