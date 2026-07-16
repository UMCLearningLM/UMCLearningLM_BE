package com.umc.learninglm.domain.auth.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "현재 사용자 정보 응답")
public record MeResponse(
		@Schema(example = "1") Long userId,
		@Schema(example = "user@example.com") String email,
		@Schema(example = "홍길동") String nickname,
		@Schema(example = "LOCAL", allowableValues = {"LOCAL", "SOCIAL"}) String loginType,
		@Schema(example = "GOOGLE", nullable = true) String provider
) {
}
