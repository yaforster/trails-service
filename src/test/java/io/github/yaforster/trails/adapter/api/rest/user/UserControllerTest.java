package io.github.yaforster.trails.adapter.api.rest.user;

import io.github.yaforster.trails.core.PagedResult;

import io.github.yaforster.trails.adapter.api.rest.model.ProfilePictureResourceDTO;
import io.github.yaforster.trails.adapter.api.rest.common.mapper.ScreenshotFileRequestMapper;
import io.github.yaforster.trails.adapter.api.rest.model.PagedUserProfileDTO;
import io.github.yaforster.trails.adapter.api.rest.model.UserProfileDTO;
import io.github.yaforster.trails.adapter.api.rest.model.UserProfileUpdateDTO;
import io.github.yaforster.trails.app.services.UserProfileService;
import io.github.yaforster.trails.core.TrailsTest;
import io.github.yaforster.trails.core.data.TrailsScreenshotFile;
import io.github.yaforster.trails.core.user.UserProfile;
import io.github.yaforster.trails.core.user.UserProfilePictureResource;
import io.github.yaforster.trails.core.user.UserProfileUpdate;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

import java.lang.reflect.Method;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class UserControllerTest extends TrailsTest {

	private final UserProfileService userProfileService = mock(UserProfileService.class);

	private final UserHATEOASFacade facade = mock(UserHATEOASFacade.class);

	private final ScreenshotFileRequestMapper screenshotFileRequestMapper = mock(ScreenshotFileRequestMapper.class);

	private final UserControllerValidator validator = mock(UserControllerValidator.class);

	private final UserController controller = new UserController(userProfileService, facade,
			screenshotFileRequestMapper, validator);

	@Test
	void getProfile_shouldUseJwtSubjectAsUserId() {
		UUID userId = UUID.randomUUID();
		UserProfile profile = new UserProfile(userId, null, null, null, null, false);
		UserProfileDTO dto = new UserProfileDTO().userId(userId);
		authenticate(userId);
		when(userProfileService.getOrCreateProfile(userId)).thenReturn(profile);
		when(facade.toDTO(profile)).thenReturn(dto);

		try {
			ResponseEntity<UserProfileDTO> response = controller.getProfile();

			assertEquals(HttpStatus.OK, response.getStatusCode());
			assertSame(dto, response.getBody());
		}
		finally {
			SecurityContextHolder.clearContext();
		}
	}

	@Test
	void updateProfile_shouldValidateMapAndStoreUpdate() {
		UUID userId = UUID.randomUUID();
		UserProfileUpdateDTO dto = new UserProfileUpdateDTO().firstName("Ada");
		UserProfileUpdate update = new UserProfileUpdate(Optional.of("Ada"), Optional.empty(), Optional.empty(),
				Optional.empty());
		UserProfile profile = new UserProfile(userId, "Ada", null, null, null, false);
		authenticate(userId);
		when(facade.toDomain(dto)).thenReturn(update);
		when(userProfileService.updateProfile(new UserProfileService.UserProfileChange(userId, update)))
			.thenReturn(profile);
		when(facade.toDTO(profile)).thenReturn(new UserProfileDTO().userId(userId));

		try {
			ResponseEntity<UserProfileDTO> response = controller.updateProfile(dto);

			assertEquals(HttpStatus.OK, response.getStatusCode());
			verify(validator).validateUpdateProfile(dto);
		}
		finally {
			SecurityContextHolder.clearContext();
		}
	}

	@Test
	void updateProfilePicture_shouldValidateAndStorePicture() {
		UUID userId = UUID.randomUUID();
		MockMultipartFile file = new MockMultipartFile("file", "avatar.jpg", "image/jpeg", jpegBytes());
		TrailsScreenshotFile screenshotFile = screenshotFile("avatar.jpg", jpegBytes());
		UserProfilePictureResource resource = new UserProfilePictureResource("image/jpeg", OffsetDateTime.now());
		authenticate(userId);
		when(screenshotFileRequestMapper.from(file)).thenReturn(screenshotFile);
		when(userProfileService
			.updateProfilePicture(new UserProfileService.UserProfilePictureChange(userId, screenshotFile)))
			.thenReturn(resource);
		when(facade.toDTO(resource)).thenReturn(new ProfilePictureResourceDTO());

		try {
			ResponseEntity<ProfilePictureResourceDTO> response = controller.updateProfilePicture(file);

			assertEquals(HttpStatus.OK, response.getStatusCode());
			verify(validator).validateUpdateProfilePicture(file);
		}
		finally {
			SecurityContextHolder.clearContext();
		}
	}

	@Test
	void deleteProfilePicture_shouldReturnNoContent() {
		UUID userId = UUID.randomUUID();
		authenticate(userId);

		try {
			ResponseEntity<Void> response = controller.deleteProfilePicture();

			assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
			verify(userProfileService).deleteProfilePicture(userId);
		}
		finally {
			SecurityContextHolder.clearContext();
		}
	}

	@Test
	void deleteUserData_shouldReturnNoContent() {
		UUID userId = UUID.randomUUID();

		ResponseEntity<Void> response = controller.deleteUserData(userId);

		assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
	}

	@Test
	void deleteUserData_shouldDeleteRequestedUserData() {
		UUID userId = UUID.randomUUID();

		controller.deleteUserData(userId);

		verify(userProfileService).deleteUserData(userId);
	}

	@Test
	void deleteUserData_shouldRequireConfiguredAdminAuthority() throws NoSuchMethodException {
		Method method = UserController.class.getMethod("deleteUserData", UUID.class);
		PreAuthorize preAuthorize = method.getAnnotation(PreAuthorize.class);

		assertEquals("@resourceAuthorization.canManageUsers(authentication)", preAuthorize.value());
	}

	@Test
	void listUsers_shouldReturnPagedUsers() {
		UserProfile profile = new UserProfile(UUID.randomUUID(), null, null, null, null, false);
		PagedResult<UserProfile> users = new PagedResult<>(List.of(profile), 1, 20, 40);
		PagedUserProfileDTO dto = new PagedUserProfileDTO();
		when(userProfileService.listUsers(new UserProfileService.UserPage(1, 20))).thenReturn(users);
		when(facade.toPagedDTO(users)).thenReturn(dto);

		ResponseEntity<PagedUserProfileDTO> response = controller.listUsers(1, 20);

		assertSame(dto, response.getBody());
	}

	@Test
	void listUsers_shouldValidatePageRequest() {
		PagedResult<UserProfile> users = new PagedResult<>(List.of(), 0, 20, 0);
		when(userProfileService.listUsers(new UserProfileService.UserPage(0, 20))).thenReturn(users);

		controller.listUsers(0, 20);

		verify(validator).validateListUsers(0, 20);
	}

	@Test
	void listUsers_shouldRequireConfiguredAdminAuthority() throws NoSuchMethodException {
		Method method = UserController.class.getMethod("listUsers", Integer.class, Integer.class);
		PreAuthorize preAuthorize = method.getAnnotation(PreAuthorize.class);

		assertEquals("@resourceAuthorization.canManageUsers(authentication)", preAuthorize.value());
	}

	private void authenticate(UUID userId) {
		Jwt jwt = Jwt.withTokenValue("token").header("alg", "none").subject(userId.toString()).build();
		SecurityContextHolder.getContext().setAuthentication(new JwtAuthenticationToken(jwt));
	}

}
