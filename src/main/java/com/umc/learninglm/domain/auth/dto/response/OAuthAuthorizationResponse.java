package com.umc.learninglm.domain.auth.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "OAuth 인증 URL 응답")
public record OAuthAuthorizationResponse(
		@Schema(example = "/api/auth/oauth2/authorization/google") String authorizationUrl
) {
}
