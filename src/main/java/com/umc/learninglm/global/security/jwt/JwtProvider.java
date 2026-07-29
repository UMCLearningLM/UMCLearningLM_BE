package com.umc.learninglm.global.security.jwt;

import com.umc.learninglm.domain.auth.entity.User;
import com.umc.learninglm.domain.auth.enums.UserRole;
import com.umc.learninglm.domain.auth.enums.VerificationPurpose;
import com.umc.learninglm.global.error.CustomException;
import com.umc.learninglm.global.error.ErrorCode;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.UUID;

@Component
public class JwtProvider {

	private static final String EMAIL_CLAIM = "email";
	private static final String AUTHORITY_CLAIM = "authority";
	private static final String TOKEN_TYPE_CLAIM = "tokenType";
	private static final String PURPOSE_CLAIM = "purpose";
	private static final String REMEMBER_ME_CLAIM = "rememberMe";
	private static final String TEMP_AUTHORITY = "TEMP";

	private final SecretKey secretKey;
	private final long accessTokenExpirationMs;
	private final long refreshTokenExpirationMs;
	private final long rememberMeRefreshTokenExpirationMs;
	private final long emailVerificationTokenExpirationMs;

	public JwtProvider(
			@Value("${jwt.secret}") String secret,
			@Value("${jwt.access-token-expiration-ms}") long accessTokenExpirationMs,
			@Value("${jwt.refresh-token-expiration-ms}") long refreshTokenExpirationMs,
			@Value("${jwt.remember-me-refresh-token-expiration-ms}") long rememberMeRefreshTokenExpirationMs,
			@Value("${jwt.email-verification-token-expiration-ms}") long emailVerificationTokenExpirationMs) {
		this.secretKey = Keys.hmacShaKeyFor(Decoders.BASE64.decode(secret));
		this.accessTokenExpirationMs = accessTokenExpirationMs;
		this.refreshTokenExpirationMs = refreshTokenExpirationMs;
		this.rememberMeRefreshTokenExpirationMs = rememberMeRefreshTokenExpirationMs;
		this.emailVerificationTokenExpirationMs = emailVerificationTokenExpirationMs;
	}

	public JwtTokenPair createTokenPair(User user, boolean rememberMe) {
		Instant issuedAt = Instant.now();
		Instant accessExpiresAt = issuedAt.plusMillis(accessTokenExpirationMs);
		long refreshExpirationMs = rememberMe
				? rememberMeRefreshTokenExpirationMs
				: refreshTokenExpirationMs;
		Instant refreshExpiresAt = issuedAt.plusMillis(refreshExpirationMs);

		String accessToken = createToken(
				user.getEmail(),
				user.getRole().name(),
				JwtTokenType.ACCESS,
				JwtTokenType.ACCESS.name(),
				false,
				issuedAt,
				accessExpiresAt);
		String refreshToken = createToken(
				user.getEmail(),
				user.getRole().name(),
				JwtTokenType.REFRESH,
				JwtTokenType.REFRESH.name(),
				rememberMe,
				issuedAt,
				refreshExpiresAt);

		return new JwtTokenPair(
				accessToken,
				refreshToken,
				toLocalDateTime(accessExpiresAt),
				toLocalDateTime(refreshExpiresAt));
	}

	public JwtEmailVerificationToken createEmailVerificationToken(
			String email,
			VerificationPurpose purpose) {
		Instant issuedAt = Instant.now();
		Instant expiresAt = issuedAt.plusMillis(emailVerificationTokenExpirationMs);
		String token = createToken(
				email,
				TEMP_AUTHORITY,
				JwtTokenType.EMAIL_VERIFICATION,
				purpose.name(),
				false,
				issuedAt,
				expiresAt);
		return new JwtEmailVerificationToken(token, toLocalDateTime(expiresAt));
	}

	public Claims parseAccessToken(String token) {
		return parseToken(token, JwtTokenType.ACCESS, ErrorCode.INVALID_ACCESS_TOKEN);
	}

	public Claims parseRefreshToken(String token) {
		return parseToken(token, JwtTokenType.REFRESH, ErrorCode.INVALID_REFRESH_TOKEN);
	}

	public Claims parseEmailVerificationToken(String token) {
		Claims claims = parseToken(
				token,
				JwtTokenType.EMAIL_VERIFICATION,
				ErrorCode.INVALID_EMAIL_VERIFICATION_TOKEN,
				ErrorCode.TOKEN_TYPE_MISMATCH,
				ErrorCode.EMAIL_VERIFICATION_TOKEN_EXPIRED);
		if (!TEMP_AUTHORITY.equals(claims.get(AUTHORITY_CLAIM, String.class))) {
			throw new CustomException(ErrorCode.TOKEN_TYPE_MISMATCH);
		}
		return claims;
	}

	public String getEmail(Claims claims) {
		return claims.get(EMAIL_CLAIM, String.class);
	}

	public UserRole getAuthority(Claims claims) {
		try {
			return UserRole.valueOf(claims.get(AUTHORITY_CLAIM, String.class));
		} catch (IllegalArgumentException | NullPointerException e) {
			throw new CustomException(ErrorCode.INVALID_ACCESS_TOKEN);
		}
	}

	public boolean isRememberMe(Claims claims) {
		return Boolean.TRUE.equals(claims.get(REMEMBER_ME_CLAIM, Boolean.class));
	}

	public VerificationPurpose getPurpose(Claims claims) {
		try {
			return VerificationPurpose.valueOf(claims.get(PURPOSE_CLAIM, String.class));
		} catch (IllegalArgumentException | NullPointerException e) {
			throw new CustomException(ErrorCode.INVALID_EMAIL_VERIFICATION_TOKEN);
		}
	}

	public LocalDateTime getExpiration(Claims claims) {
		return LocalDateTime.ofInstant(claims.getExpiration().toInstant(), ZoneId.systemDefault());
	}

	private String createToken(
			String email,
			String authority,
			JwtTokenType tokenType,
			String purpose,
			boolean rememberMe,
			Instant issuedAt,
			Instant expiresAt) {
		return Jwts.builder()
				// TODO: 현재 이메일을 사용하는 sub는 추후 변경되지 않는 사용자 UID로 전환할 예정입니다.
				.subject(email)
				.id(UUID.randomUUID().toString())
				.claim(EMAIL_CLAIM, email)
				.claim(AUTHORITY_CLAIM, authority)
				.claim(TOKEN_TYPE_CLAIM, tokenType.name())
				.claim(PURPOSE_CLAIM, purpose)
				.claim(REMEMBER_ME_CLAIM, rememberMe)
				.issuedAt(Date.from(issuedAt))
				.expiration(Date.from(expiresAt))
				.signWith(secretKey)
				.compact();
	}

	private Claims parseToken(String token, JwtTokenType expectedType, ErrorCode errorCode) {
		return parseToken(token, expectedType, errorCode, errorCode, errorCode);
	}

	private Claims parseToken(
			String token,
			JwtTokenType expectedType,
			ErrorCode invalidErrorCode,
			ErrorCode typeMismatchErrorCode,
			ErrorCode expiredErrorCode) {
		try {
			Claims claims = Jwts.parser()
					.verifyWith(secretKey)
					.build()
					.parseSignedClaims(token)
					.getPayload();
			String tokenType = claims.get(TOKEN_TYPE_CLAIM, String.class);
			if (!expectedType.name().equals(tokenType)) {
				throw new CustomException(typeMismatchErrorCode);
			}
			return claims;
		} catch (CustomException e) {
			throw e;
		} catch (ExpiredJwtException e) {
			throw new CustomException(expiredErrorCode);
		} catch (JwtException | IllegalArgumentException e) {
			throw new CustomException(invalidErrorCode);
		}
	}

	private LocalDateTime toLocalDateTime(Instant instant) {
		return LocalDateTime.ofInstant(instant, ZoneId.systemDefault());
	}
}
