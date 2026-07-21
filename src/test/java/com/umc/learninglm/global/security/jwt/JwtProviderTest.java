package com.umc.learninglm.global.security.jwt;

import com.umc.learninglm.domain.auth.entity.User;
import com.umc.learninglm.domain.auth.enums.UserRole;
import com.umc.learninglm.domain.auth.enums.VerificationPurpose;
import com.umc.learninglm.global.error.CustomException;
import com.umc.learninglm.global.error.ErrorCode;
import io.jsonwebtoken.Claims;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class JwtProviderTest {

	private JwtProvider jwtProvider;
	private User user;

	@BeforeEach
	void setUp() {
		String secret = Base64.getEncoder().encodeToString(
				"test-secret-key-with-at-least-forty-eight-bytes-value".getBytes(StandardCharsets.UTF_8));
		jwtProvider = new JwtProvider(secret, 1_800_000L, 86_400_000L, 604_800_000L, 1_800_000L);
		user = User.createLocal("user@example.com", "password-hash", "홍길동");
	}

	@Test
	void createsAndParsesAccessAndRefreshTokens() {
		JwtTokenPair tokenPair = jwtProvider.createTokenPair(user, true);

		Claims accessClaims = jwtProvider.parseAccessToken(tokenPair.accessToken());
		Claims refreshClaims = jwtProvider.parseRefreshToken(tokenPair.refreshToken());

		assertThat(jwtProvider.getEmail(accessClaims)).isEqualTo("user@example.com");
		assertThat(jwtProvider.getAuthority(accessClaims)).isEqualTo(UserRole.USER);
		assertThat(jwtProvider.isRememberMe(refreshClaims)).isTrue();
		assertThat(tokenPair.refreshTokenExpiresAt()).isAfter(tokenPair.accessTokenExpiresAt());
	}

	@Test
	void createsUniqueTokensForConsecutiveIssuance() {
		JwtTokenPair firstTokenPair = jwtProvider.createTokenPair(user, false);
		JwtTokenPair secondTokenPair = jwtProvider.createTokenPair(user, false);

		assertThat(firstTokenPair.accessToken()).isNotEqualTo(secondTokenPair.accessToken());
		assertThat(firstTokenPair.refreshToken()).isNotEqualTo(secondTokenPair.refreshToken());
		assertThat(jwtProvider.parseRefreshToken(firstTokenPair.refreshToken()).getId()).isNotBlank();
		assertThat(jwtProvider.parseRefreshToken(secondTokenPair.refreshToken()).getId()).isNotBlank();
	}

	@Test
	void rejectsRefreshTokenAsAccessToken() {
		JwtTokenPair tokenPair = jwtProvider.createTokenPair(user, false);

		assertThatThrownBy(() -> jwtProvider.parseAccessToken(tokenPair.refreshToken()))
				.isInstanceOf(CustomException.class)
				.extracting(exception -> ((CustomException) exception).getErrorCode())
				.isEqualTo(ErrorCode.INVALID_ACCESS_TOKEN);
	}

	@Test
	void rejectsTamperedToken() {
		JwtTokenPair tokenPair = jwtProvider.createTokenPair(user, false);
		String tamperedToken = tokenPair.accessToken() + "tampered";

		assertThatThrownBy(() -> jwtProvider.parseAccessToken(tamperedToken))
				.isInstanceOf(CustomException.class)
				.extracting(exception -> ((CustomException) exception).getErrorCode())
				.isEqualTo(ErrorCode.INVALID_ACCESS_TOKEN);
	}

	@Test
	void createsEmailVerificationTokenWithTemporaryAuthorityAndPurpose() {
		JwtEmailVerificationToken token = jwtProvider.createEmailVerificationToken(
				"user@example.com",
				VerificationPurpose.PASSWORD_RESET);

		Claims claims = jwtProvider.parseEmailVerificationToken(token.token());

		assertThat(jwtProvider.getEmail(claims)).isEqualTo("user@example.com");
		assertThat(jwtProvider.getPurpose(claims)).isEqualTo(VerificationPurpose.PASSWORD_RESET);
		assertThat(claims.get("authority", String.class)).isEqualTo("TEMP");
		assertThat(token.expiresAt()).isAfter(java.time.LocalDateTime.now().plusMinutes(29));
	}
}
