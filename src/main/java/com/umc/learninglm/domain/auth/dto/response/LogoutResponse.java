package com.umc.learninglm.domain.auth.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "로그아웃 결과")
public record LogoutResponse(
		@Schema(example = "true") boolean loggedOut
) {
}
