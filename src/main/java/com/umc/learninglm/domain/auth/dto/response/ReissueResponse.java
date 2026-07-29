package com.umc.learninglm.domain.auth.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "토큰 재발급 응답")
public record ReissueResponse(
		@Schema(example = "eyJhbGciOiJIUzM4NCJ9.access.signature") String accessToken,
		@Schema(example = "eyJhbGciOiJIUzM4NCJ9.refresh.signature") String refreshToken
) {
}
