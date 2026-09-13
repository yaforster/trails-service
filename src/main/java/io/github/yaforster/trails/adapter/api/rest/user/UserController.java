package io.github.yaforster.trails.adapter.api.rest.user;

import io.github.yaforster.trails.adapter.api.NotAllowedException;
import io.github.yaforster.trails.adapter.api.rest.common.mapper.ScreenshotFileRequestMapper;
import io.github.yaforster.trails.adapter.api.rest.model.PagedUserProfileDTO;
import io.github.yaforster.trails.adapter.api.rest.model.ProfilePictureResourceDTO;
import io.github.yaforster.trails.adapter.api.rest.model.UserProfileDTO;
import io.github.yaforster.trails.adapter.api.rest.model.UserProfileUpdateDTO;
import io.github.yaforster.trails.app.services.UserProfileService;
import io.github.yaforster.trails.core.PagedResult;
import io.github.yaforster.trails.core.data.TrailsScreenshotFile;
import io.github.yaforster.trails.core.user.UserProfile;
import io.github.yaforster.trails.core.user.UserProfilePictureResource;
import io.github.yaforster.trails.core.user.UserProfileUpdate;
import lombok.AllArgsConstructor;
import org.openapitools.api.UserApi;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

@AllArgsConstructor
@RestController
@ConditionalOnProperty(name = "service.api.security.oauth2.enabled", havingValue = "true")
public class UserController implements UserApi {

	private final UserProfileService userProfileService;

	private final UserHATEOASFacade hateoasFacade;

	private final ScreenshotFileRequestMapper screenshotFileRequestMapper;

	private final UserControllerValidator validator;

	@Override
	@PreAuthorize("@resourceAuthorization.canManageUsers(authentication)")
	public ResponseEntity<PagedUserProfileDTO> listUsers(Integer page, Integer size) {
		validator.validateListUsers(page, size);
		PagedResult<UserProfile> users = userProfileService.listUsers(new UserProfileService.UserPage(page, size));
		return ResponseEntity.ok(hateoasFacade.toPagedDTO(users));
	}

	@Override
	public ResponseEntity<UserProfileDTO> getProfile() {
		UserProfile profile = userProfileService.getOrCreateProfile(getCurrentUserId());
		return ResponseEntity.ok(hateoasFacade.toDTO(profile));
	}

	@Override
	public ResponseEntity<Resource> getProfilePicture() {
		return userProfileService.getProfilePicture(getCurrentUserId())
			.map(profilePicture -> ResponseEntity.ok()
				.contentType(MediaType.parseMediaType(profilePicture.contentType()))
				.contentLength(profilePicture.size())
				.header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + profilePicture.fileName() + "\"")
				.<Resource>body(new ByteArrayResource(profilePicture.content().bytes())))
			.orElse(ResponseEntity.notFound().build());
	}

	@Override
	public ResponseEntity<UserProfileDTO> updateProfile(UserProfileUpdateDTO userProfileUpdateDTO) {
		validator.validateUpdateProfile(userProfileUpdateDTO);
		UserProfileUpdate update = hateoasFacade.toDomain(userProfileUpdateDTO);
		UserProfile profile = userProfileService
			.updateProfile(new UserProfileService.UserProfileChange(getCurrentUserId(), update));
		return ResponseEntity.ok(hateoasFacade.toDTO(profile));
	}

	@Override
	public ResponseEntity<ProfilePictureResourceDTO> updateProfilePicture(MultipartFile file) {
		validator.validateUpdateProfilePicture(file);
		TrailsScreenshotFile profilePicture = screenshotFileRequestMapper.from(file);
		UserProfilePictureResource resource = userProfileService
			.updateProfilePicture(new UserProfileService.UserProfilePictureChange(getCurrentUserId(), profilePicture));
		return ResponseEntity.ok(hateoasFacade.toDTO(resource));
	}

	@Override
	public ResponseEntity<Void> deleteProfilePicture() {
		userProfileService.deleteProfilePicture(getCurrentUserId());
		return ResponseEntity.noContent().build();
	}

	@Override
	@PreAuthorize("@resourceAuthorization.canManageUsers(authentication)")
	public ResponseEntity<Void> deleteUserData(UUID userId) {
		userProfileService.deleteUserData(userId);
		return ResponseEntity.noContent().build();
	}

	private UUID getCurrentUserId() {
		try {
			return UUID.fromString(getCurrentJWT().getSubject());
		}
		catch (IllegalArgumentException exception) {
			throw new NotAllowedException("JWT subject is not a valid user id.");
		}
	}

	private Jwt getCurrentJWT() {
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		if (authentication == null || !(authentication.getPrincipal() instanceof Jwt jwt)) {
			throw new NotAllowedException("User is not authenticated or does not have JWT token.");
		}
		return jwt;
	}

}
