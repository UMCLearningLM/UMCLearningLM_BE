package com.umc.learninglm.domain.auth.dto.request;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;

@Schema(description = "프로필 수정 요청. 변경하지 않을 필드는 생략합니다.")
public record ProfileUpdateRequest(
		@Schema(example = "new-user@example.com", nullable = true)
		@Email(message = "이메일 형식이 올바르지 않습니다.")
		@Size(min = 1, max = 255, message = "이메일 형식이 올바르지 않습니다.")
		String email,

		@Schema(description = "비밀번호 변경 시 필수", example = "CurrentPassword123!", nullable = true)
		@Size(min = 1, message = "현재 비밀번호를 입력해주세요.")
		String currentPassword,

		@Schema(description = "변경할 새 비밀번호", example = "NewPassword123!", nullable = true)
		@Size(min = 1, message = "새 비밀번호를 입력해주세요.")
		String newPassword,

		@Schema(example = "새닉네임", nullable = true)
		@Size(min = 1, max = 50, message = "닉네임 형식이 올바르지 않습니다.")
		String nickname
) {

	@JsonIgnore
	@Schema(hidden = true)
	@AssertTrue(message = "수정할 정보가 없습니다.")
	public boolean isUpdateFieldPresent() {
		return email != null || newPassword != null || nickname != null;
	}

	@JsonIgnore
	@Schema(hidden = true)
	@AssertTrue(message = "현재 비밀번호를 입력해주세요.")
	public boolean isCurrentPasswordPresentForPasswordChange() {
		return newPassword == null || (currentPassword != null && !currentPassword.isBlank());
	}

	@JsonIgnore
	@Schema(hidden = true)
	@AssertTrue(message = "새 비밀번호를 입력해주세요.")
	public boolean isNewPasswordPresentWithCurrentPassword() {
		return currentPassword == null || (newPassword != null && !newPassword.isBlank());
	}
}
