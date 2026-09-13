package io.github.yaforster.trails.adapter.api.rest.user;

import io.github.yaforster.trails.core.PagedResult;

import io.github.yaforster.trails.adapter.api.rest.model.PagedUserProfileDTO;
import io.github.yaforster.trails.adapter.api.rest.model.ProfilePictureResourceDTO;
import io.github.yaforster.trails.adapter.api.rest.model.UserProfileDTO;
import io.github.yaforster.trails.adapter.api.rest.model.UserProfileUpdateDTO;
import io.github.yaforster.trails.core.TrailsTest;
import io.github.yaforster.trails.core.user.UserProfile;
import io.github.yaforster.trails.core.user.UserProfilePictureResource;
import io.github.yaforster.trails.core.user.UserProfileUpdate;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class UserHATEOASFacadeTest extends TrailsTest {

	private final UserHATEOASFacade facade = new UserHATEOASFacade();

	@Test
	void toDTO_shouldMapProfileAndPictureLinks() {
		UUID userId = UUID.randomUUID();
		UserProfile profile = new UserProfile(userId, "Ada", "Lovelace", "ada@example.org", "+491701234567", true);

		UserProfileDTO result = facade.toDTO(profile);

		assertEquals(userId, result.getUserId());
		assertNotNull(result.getLinks().getGetProfilePicture());
	}

	@Test
	void toDTO_shouldOmitPictureLinks_whenNoPictureExists() {
		UserProfile profile = new UserProfile(UUID.randomUUID(), null, null, null, null, false);

		UserProfileDTO result = facade.toDTO(profile);

		assertNull(result.getLinks().getGetProfilePicture());
	}

	@Test
	void toDTO_shouldMapPictureResource() {
		OffsetDateTime updatedAt = OffsetDateTime.now();

		ProfilePictureResourceDTO result = facade.toDTO(new UserProfilePictureResource("image/jpeg", updatedAt));

		assertEquals("image/jpeg", result.getContentType());
	}

	@Test
	void toDomain_shouldMapExplicitNullValue() {
		UserProfileUpdateDTO dto = new UserProfileUpdateDTO().firstName(null)
			.lastName("Lovelace")
			.email("ada@example.org")
			.phoneNumber("+491701234567");

		UserProfileUpdate result = facade.toDomain(dto);

		assertEquals(Optional.empty(), result.firstName());
	}

	@Test
	void toPagedDTO_shouldIncludeUserSubject() {
		UUID userId = UUID.randomUUID();
		UserProfile profile = new UserProfile(userId, "Ada", "Lovelace", "ada@example.org", null, false);
		PagedResult<UserProfile> profiles = new PagedResult<>(List.of(profile), 0, 20, 1);

		PagedUserProfileDTO result = facade.toPagedDTO(profiles);

		assertEquals(userId, result.getItems().getFirst().getUserId());
	}

	@Test
	void toPagedDTO_shouldAddDeleteUserDataLink() {
		UUID userId = UUID.randomUUID();
		UserProfile profile = new UserProfile(userId, null, null, null, null, false);
		PagedResult<UserProfile> profiles = new PagedResult<>(List.of(profile), 0, 20, 1);

		PagedUserProfileDTO result = facade.toPagedDTO(profiles);

		assertTrue(result.getItems()
			.getFirst()
			.getLinks()
			.getDeleteUserData()
			.getHref()
			.endsWith("/user/profile/" + userId));
	}

	@Test
	void toPagedDTO_shouldAddPagingMetadata() {
		UserProfile profile = new UserProfile(UUID.randomUUID(), null, null, null, null, false);
		PagedResult<UserProfile> profiles = new PagedResult<>(List.of(profile), 1, 20, 80);

		PagedUserProfileDTO result = facade.toPagedDTO(profiles);

		assertEquals(4, result.getTotalPages());
	}

}
