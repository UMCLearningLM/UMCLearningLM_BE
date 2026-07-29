package com.umc.learninglm.domain.auth.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "인증 성공 및 사용자 토큰 응답")
public record AuthTokenResponse(
		@Schema(example = "1") Long userId,
		@Schema(example = "user@example.com") String email,
		@Schema(example = "홍길동") String nickname,
		@Schema(example = "eyJhbGciOiJIUzM4NCJ9.access.signature") String accessToken,
		@Schema(example = "eyJhbGciOiJIUzM4NCJ9.refresh.signature") String refreshToken
) {
}
