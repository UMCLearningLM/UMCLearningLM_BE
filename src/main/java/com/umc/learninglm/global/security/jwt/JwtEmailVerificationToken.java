package com.umc.learninglm.global.security.jwt;

import java.time.LocalDateTime;

public record JwtEmailVerificationToken(
		String token,
		LocalDateTime expiresAt
) {
}
