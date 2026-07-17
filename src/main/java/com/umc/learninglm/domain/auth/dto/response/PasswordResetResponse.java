package com.umc.learninglm.domain.auth.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "비밀번호 재설정 응답")
public record PasswordResetResponse(
		@Schema(example = "true") boolean passwordReset,
		@Schema(example = "true") boolean loggedOut
) {
}
