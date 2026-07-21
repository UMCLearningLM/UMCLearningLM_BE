package com.umc.learninglm.domain.auth.service;

import com.umc.learninglm.domain.auth.entity.TokenCode;
import com.umc.learninglm.domain.auth.enums.TokenStatus;
import com.umc.learninglm.domain.auth.enums.TokenType;
import com.umc.learninglm.domain.auth.enums.VerificationPurpose;
import com.umc.learninglm.domain.auth.repository.TokenCodeRepository;
import com.umc.learninglm.global.error.CustomException;
import com.umc.learninglm.global.error.ErrorCode;
import com.umc.learninglm.global.security.jwt.JwtProvider;
import io.jsonwebtoken.Claims;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

@Service
public class TokenCodeService {

	private final TokenCodeRepository tokenCodeRepository;
	private final TokenHashService tokenHashService;
	private final JwtProvider jwtProvider;
	private final int maxVerificationAttempts;

	public TokenCodeService(
			TokenCodeRepository tokenCodeRepository,
			TokenHashService tokenHashService,
			JwtProvider jwtProvider,
			@Value("${email-verification.max-verify-attempts}") int maxVerificationAttempts) {
		this.tokenCodeRepository = tokenCodeRepository;
		this.tokenHashService = tokenHashService;
		this.jwtProvider = jwtProvider;
		this.maxVerificationAttempts = maxVerificationAttempts;
	}

	@Transactional
	public TokenCode findValidEmailVerificationToken(
			String rawToken,
			String expectedEmail,
			VerificationPurpose expectedPurpose) {
		TokenCode tokenCode = findValidEmailVerificationToken(rawToken, expectedPurpose);
		if (!matchesEmail(tokenCode, expectedEmail)) {
			throw new CustomException(ErrorCode.INVALID_EMAIL_VERIFICATION_TOKEN);
		}
		return tokenCode;
	}

	@Transactional
	public TokenCode findValidEmailVerificationToken(
			String rawToken,
			VerificationPurpose expectedPurpose) {
		if (rawToken == null || rawToken.isBlank()) {
			throw new CustomException(ErrorCode.EMAIL_VERIFICATION_TOKEN_MISSING);
		}
		String token = removeBearerPrefix(rawToken);
		Claims claims = jwtProvider.parseEmailVerificationToken(token);
		if (jwtProvider.getPurpose(claims) != expectedPurpose) {
			throw new CustomException(ErrorCode.TOKEN_PURPOSE_MISMATCH);
		}

		TokenCode tokenCode = tokenCodeRepository
				.findFirstByTokenHashOrderByCreatedAtDesc(tokenHashService.hash(token))
				.orElseThrow(() -> new CustomException(ErrorCode.INVALID_EMAIL_VERIFICATION_TOKEN));

		validateType(tokenCode, TokenType.EMAIL_VERIFICATION);
		validatePurpose(tokenCode, expectedPurpose);
		validateVerificationTokenStatus(tokenCode);
		if (!matchesEmail(tokenCode, jwtProvider.getEmail(claims))) {
			throw new CustomException(ErrorCode.INVALID_EMAIL_VERIFICATION_TOKEN);
		}
		return tokenCode;
	}

	@Transactional(noRollbackFor = CustomException.class)
	public TokenCode findValidEmailCode(
			String email,
			String rawCode,
			VerificationPurpose expectedPurpose) {
		if (rawCode == null || rawCode.isBlank()) {
			throw new CustomException(ErrorCode.EMAIL_CODE_MISSING);
		}

		TokenCode tokenCode = tokenCodeRepository
				.findFirstByEmailAndTypeAndPurposeAndStatusOrderByCreatedAtDesc(
						email,
						TokenType.EMAIL_CODE,
						expectedPurpose,
						TokenStatus.ACTIVE)
				.orElseThrow(() -> new CustomException(ErrorCode.EMAIL_CODE_MISMATCH));

		if (tokenCode.isExpired(LocalDateTime.now())) {
			throw new CustomException(ErrorCode.EMAIL_CODE_EXPIRED);
		}
		if (tokenCode.getAttemptCount() >= maxVerificationAttempts) {
			throw new CustomException(ErrorCode.EMAIL_CODE_ATTEMPT_LIMIT_EXCEEDED);
		}
		String submittedHash = tokenHashService.hash(rawCode);
		if (!MessageDigest.isEqual(
				tokenCode.getTokenHash().getBytes(StandardCharsets.UTF_8),
				submittedHash.getBytes(StandardCharsets.UTF_8))) {
			tokenCode.recordFailedAttempt();
			if (tokenCode.getAttemptCount() >= maxVerificationAttempts) {
				tokenCode.revoke(LocalDateTime.now());
				throw new CustomException(ErrorCode.EMAIL_CODE_ATTEMPT_LIMIT_EXCEEDED);
			}
			throw new CustomException(ErrorCode.EMAIL_CODE_MISMATCH);
		}
		return tokenCode;
	}

	private void validateType(TokenCode tokenCode, TokenType expectedType) {
		if (tokenCode.getType() != expectedType) {
			throw new CustomException(ErrorCode.TOKEN_TYPE_MISMATCH);
		}
	}

	private void validatePurpose(TokenCode tokenCode, VerificationPurpose expectedPurpose) {
		if (tokenCode.getPurpose() != expectedPurpose) {
			throw new CustomException(ErrorCode.TOKEN_PURPOSE_MISMATCH);
		}
	}

	private void validateVerificationTokenStatus(TokenCode tokenCode) {
		if (tokenCode.isExpired(LocalDateTime.now())) {
			throw new CustomException(ErrorCode.EMAIL_VERIFICATION_TOKEN_EXPIRED);
		}
		if (tokenCode.getStatus() != TokenStatus.ACTIVE) {
			throw new CustomException(ErrorCode.INVALID_EMAIL_VERIFICATION_TOKEN);
		}
	}

	private boolean matchesEmail(TokenCode tokenCode, String expectedEmail) {
		if (tokenCode.getEmail() != null) {
			return tokenCode.getEmail().equals(expectedEmail);
		}
		return tokenCode.getUser() != null && tokenCode.getUser().getEmail().equals(expectedEmail);
	}

	private String removeBearerPrefix(String rawToken) {
		return rawToken.startsWith("Bearer ") ? rawToken.substring(7) : rawToken;
	}
}
