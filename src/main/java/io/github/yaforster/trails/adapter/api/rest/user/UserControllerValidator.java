package io.github.yaforster.trails.adapter.api.rest.user;

import io.github.yaforster.trails.adapter.api.rest.model.UserProfileUpdateDTO;
import io.github.yaforster.trails.adapter.api.rest.validation.ValidationContext;
import io.github.yaforster.trails.adapter.api.rest.validation.PaginationValidator;
import io.github.yaforster.trails.core.ValidationViolation;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.util.regex.Pattern;

@Component
@ConditionalOnProperty(name = "service.api.security.oauth2.enabled", havingValue = "true")
public class UserControllerValidator {

	private static final Pattern EMAIL_PATTERN = Pattern.compile("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$");

	public void validateUpdateProfile(UserProfileUpdateDTO dto) {
		ValidationContext validation = ValidationContext.begin();
		if (dto == null) {
			validation.addViolation(
					new ValidationViolation("USER_PROFILE_UPDATE_NULL", "User profile update must not be null.", "/"));
		}
		if (dto != null) {
			if (isFirstNameTooLong(dto)) {
				validation.addViolation(new ValidationViolation("USER_FIRST_NAME_TOO_LONG",
						"First name must not be longer than 100 characters.", "/firstName"));
			}
			if (isLastNameTooLong(dto)) {
				validation.addViolation(new ValidationViolation("USER_LAST_NAME_TOO_LONG",
						"Last name must not be longer than 100 characters.", "/lastName"));
			}
			if (isEmailTooLong(dto)) {
				validation.addViolation(new ValidationViolation("USER_EMAIL_TOO_LONG",
						"Email must not be longer than 255 characters.", "/email"));
			}
			if (isEmailInvalid(dto)) {
				validation.addViolation(new ValidationViolation("USER_EMAIL_INVALID",
						"Email must be a valid email address.", "/email"));
			}
			if (isPhoneNumberTooLong(dto)) {
				validation.addViolation(new ValidationViolation("USER_PHONE_NUMBER_TOO_LONG",
						"Phone number must not be longer than 50 characters.", "/phoneNumber"));
			}
		}
		validation.rejectIfErrors();
	}

	public void validateUpdateProfilePicture(MultipartFile file) {
		ValidationContext validation = ValidationContext.begin();
		if (file == null) {
			validation.addViolation(new ValidationViolation("USER_PROFILE_PICTURE_FILE_NULL",
					"User profile picture file must not be null.", "/file"));
		}
		validation.rejectIfErrors();
	}

	public void validateListUsers(Integer page, Integer size) {
		ValidationContext validation = ValidationContext.begin();
		PaginationValidator.validate(validation, page, size, "USER");
		validation.rejectIfErrors();
	}

	private boolean isFirstNameTooLong(UserProfileUpdateDTO dto) {
		return isTooLong(dto.getFirstName(), 100);
	}

	private boolean isLastNameTooLong(UserProfileUpdateDTO dto) {
		return isTooLong(dto.getLastName(), 100);
	}

	private boolean isEmailTooLong(UserProfileUpdateDTO dto) {
		return isTooLong(dto.getEmail(), 255);
	}

	private boolean isPhoneNumberTooLong(UserProfileUpdateDTO dto) {
		return isTooLong(dto.getPhoneNumber(), 50);
	}

	private boolean isEmailInvalid(UserProfileUpdateDTO dto) {
		String email = dto.getEmail();
		if (email == null) {
			return false;
		}
		return !email.isBlank() && !EMAIL_PATTERN.matcher(email).matches();
	}

	private boolean isTooLong(String value, int maxLength) {
		return value != null && value.length() > maxLength;
	}

}
