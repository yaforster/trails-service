package io.github.yaforster.trails.adapter.api.rest.user;

import io.github.yaforster.trails.adapter.api.APIRequestValidationException;
import io.github.yaforster.trails.adapter.api.rest.model.UserProfileUpdateDTO;
import io.github.yaforster.trails.core.TrailsTest;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.junit.jupiter.api.Assertions.assertThrows;

class UserControllerValidatorTest extends TrailsTest {

	private final UserControllerValidator validator = new UserControllerValidator();

	@Test
	void validateUpdateProfile_shouldThrow_whenDtoIsNull() {
		assertThrows(APIRequestValidationException.class, () -> validator.validateUpdateProfile(null));
	}

	@Test
	void validateUpdateProfile_shouldThrow_whenEmailIsInvalid() {
		UserProfileUpdateDTO dto = fullUpdate().email("invalid");

		assertThrows(APIRequestValidationException.class, () -> validator.validateUpdateProfile(dto));
	}

	@Test
	void validateUpdateProfile_shouldAcceptNullProfileFields() {
		UserProfileUpdateDTO dto = new UserProfileUpdateDTO().firstName(null)
			.lastName("Lovelace")
			.email("ada@example.org")
			.phoneNumber(null);

		assertThatCode(() -> validator.validateUpdateProfile(dto)).doesNotThrowAnyException();
	}

	@Test
	void validateUpdateProfile_shouldAcceptValidUpdate() {
		UserProfileUpdateDTO dto = fullUpdate();

		assertThatCode(() -> validator.validateUpdateProfile(dto)).doesNotThrowAnyException();
	}

	@Test
	void validateUpdateProfilePicture_shouldThrow_whenFileIsNull() {
		assertThrows(APIRequestValidationException.class, () -> validator.validateUpdateProfilePicture(null));
	}

	@Test
	void validateUpdateProfilePicture_shouldAcceptFile() {
		MockMultipartFile file = new MockMultipartFile("file", "avatar.jpg", "image/jpeg", jpegBytes());

		assertThatCode(() -> validator.validateUpdateProfilePicture(file)).doesNotThrowAnyException();
	}

	private UserProfileUpdateDTO fullUpdate() {
		return new UserProfileUpdateDTO().firstName("Ada")
			.lastName("Lovelace")
			.email("ada@example.org")
			.phoneNumber("+491701234567");
	}

}
