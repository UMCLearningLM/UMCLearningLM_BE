package com.umc.learninglm.domain.auth.dto.request;

import com.umc.learninglm.domain.auth.validation.AuthValidationPatterns;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

@Schema(description = "비밀번호 재설정 요청")
public record PasswordResetRequest(
		@Schema(example = "NewPassword123!")
		@NotBlank(message = "새 비밀번호를 입력해주세요.")
		@Pattern(regexp = AuthValidationPatterns.PASSWORD, message = "비밀번호 형식이 올바르지 않습니다.")
		String newPassword
) {
}
