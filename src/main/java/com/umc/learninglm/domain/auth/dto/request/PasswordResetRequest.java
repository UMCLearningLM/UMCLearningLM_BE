package com.umc.learninglm.domain.auth.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "비밀번호 재설정 요청")
public record PasswordResetRequest(
		@Schema(example = "NewPassword123!")
		@NotBlank(message = "새 비밀번호를 입력해주세요.")
		String newPassword
) {
}
