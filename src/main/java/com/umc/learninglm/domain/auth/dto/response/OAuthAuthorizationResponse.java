package com.umc.learninglm.domain.auth.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "OAuth 인증 URL 응답")
public record OAuthAuthorizationResponse(
		@Schema(example = "https://accounts.google.com/o/oauth2/v2/auth") String authorizationUrl
) {
}
