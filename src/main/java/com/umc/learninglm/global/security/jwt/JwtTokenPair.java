package com.umc.learninglm.global.security.jwt;

import java.time.LocalDateTime;

public record JwtTokenPair(
		String accessToken,
		String refreshToken,
		LocalDateTime accessTokenExpiresAt,
		LocalDateTime refreshTokenExpiresAt
) {
}
