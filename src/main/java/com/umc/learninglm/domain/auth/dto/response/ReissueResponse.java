package com.umc.learninglm.domain.auth.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "토큰 재발급 응답")
public record ReissueResponse(
		@Schema(example = "new-access-token") String accessToken,
		@Schema(example = "new-refresh-token") String refreshToken
) {
}
