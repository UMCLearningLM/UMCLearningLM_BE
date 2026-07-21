package com.umc.learninglm.domain.auth.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "토큰 재발급 요청")
public record ReissueRequest(
		@Schema(description = "검증 및 회전할 Refresh Token", example = "eyJhbGciOiJIUzM4NCJ9.refresh.signature")
		@NotBlank(message = "리프레시 토큰이 필요합니다.")
		String refreshToken
) {
}
