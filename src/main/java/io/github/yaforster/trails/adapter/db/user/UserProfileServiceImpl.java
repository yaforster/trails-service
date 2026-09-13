package io.github.yaforster.trails.adapter.db.user;

import io.github.yaforster.trails.adapter.db.shared.SpringPageMapper;
import io.github.yaforster.trails.app.services.UserProfileService;
import io.github.yaforster.trails.core.BinaryContent;
import io.github.yaforster.trails.core.PagedResult;
import io.github.yaforster.trails.core.data.TrailsScreenshotFile;
import io.github.yaforster.trails.core.user.UserProfile;
import io.github.yaforster.trails.core.user.UserProfilePicture;
import io.github.yaforster.trails.core.user.UserProfilePictureResource;
import io.github.yaforster.trails.core.user.UserProfileUpdate;
import lombok.AllArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.Optional;
import java.util.UUID;

@AllArgsConstructor
@Service
@ConditionalOnProperty(name = "service.api.security.oauth2.enabled", havingValue = "true")
public class UserProfileServiceImpl implements UserProfileService {

	private final UserProfileRepository repository;

	@Override
	@Transactional
	public UserProfile getOrCreateProfile(UUID userId) {
		UserProfileEntity entity = getOrCreateEntity(userId);
		return toDomain(entity);
	}

	@Override
	@Transactional
	public UserProfile updateProfile(UserProfileChange profileChange) {
		UserProfileEntity entity = getOrCreateEntity(profileChange.userId());
		entity.setFirstName(profileChange.update().firstName().orElse(null));
		entity.setLastName(profileChange.update().lastName().orElse(null));
		entity.setEmail(profileChange.update().email().orElse(null));
		entity.setPhoneNumber(profileChange.update().phoneNumber().orElse(null));
		return toDomain(repository.save(entity));
	}

	@Override
	@Transactional(readOnly = true)
	public Optional<UserProfilePicture> getProfilePicture(UUID userId) {
		return repository.findById(userId)
			.filter(entity -> entity.getProfilePictureContent() != null)
			.map(entity -> new UserProfilePicture(new BinaryContent(entity.getProfilePictureContent()),
					entity.getProfilePictureFileName(), entity.getProfilePictureContentType(),
					entity.getProfilePictureUpdatedAt()));
	}

	@Override
	@Transactional
	public UserProfilePictureResource updateProfilePicture(UserProfilePictureChange pictureChange) {
		UserProfileEntity entity = getOrCreateEntity(pictureChange.userId());
		OffsetDateTime updatedAt = OffsetDateTime.now();
		String contentType = pictureChange.file().imageType().contentType();
		entity.setProfilePictureFileName(pictureChange.file().filename());
		entity.setProfilePictureContentType(contentType);
		entity.setProfilePictureContent(pictureChange.file().content());
		entity.setProfilePictureUpdatedAt(updatedAt);
		repository.save(entity);
		return new UserProfilePictureResource(contentType, updatedAt);
	}

	@Override
	@Transactional
	public void deleteProfilePicture(UUID userId) {
		UserProfileEntity entity = getOrCreateEntity(userId);
		entity.setProfilePictureFileName(null);
		entity.setProfilePictureContentType(null);
		entity.setProfilePictureContent(null);
		entity.setProfilePictureUpdatedAt(null);
		repository.save(entity);
	}

	@Override
	@Transactional
	public void deleteUserData(UUID userId) {
		repository.findById(userId).ifPresent(repository::delete);
	}

	@Override
	@Transactional(readOnly = true)
	public PagedResult<UserProfile> listUsers(UserPage userPage) {
		Pageable pageable = PageRequest.of(userPage.page(), userPage.size());
		return SpringPageMapper.toPagedResult(repository.findAll(pageable).map(this::toDomain));
	}

	private UserProfileEntity getOrCreateEntity(UUID userId) {
		return repository.findById(userId).orElseGet(() -> repository.save(new UserProfileEntity().setUserId(userId)));
	}

	private UserProfile toDomain(UserProfileEntity entity) {
		return new UserProfile(entity.getUserId(), entity.getFirstName(), entity.getLastName(), entity.getEmail(),
				entity.getPhoneNumber(), entity.getProfilePictureContent() != null);
	}

}
