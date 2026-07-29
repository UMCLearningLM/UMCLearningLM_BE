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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TokenCodeServiceTest {

	@Mock
	private TokenCodeRepository tokenCodeRepository;

	@Mock
	private TokenHashService tokenHashService;

	@Mock
	private TokenCode tokenCode;

	@Mock
	private JwtProvider jwtProvider;

	@Mock
	private Claims claims;

	private TokenCodeService tokenCodeService;

	@BeforeEach
	void setUp() {
		tokenCodeService = new TokenCodeService(tokenCodeRepository, tokenHashService, jwtProvider, 5);
	}

	@Test
	void verificationTokenRejectsMissingToken() {
		assertThatThrownBy(() -> tokenCodeService.findValidEmailVerificationToken(
				null, "user@example.com", VerificationPurpose.SIGNUP))
				.isInstanceOf(CustomException.class)
				.extracting(exception -> ((CustomException) exception).getErrorCode())
				.isEqualTo(ErrorCode.EMAIL_VERIFICATION_TOKEN_MISSING);
		verify(tokenCodeRepository, never()).findFirstByTokenHashOrderByCreatedAtDesc(any());
	}

	@Test
	void verificationTokenRejectsUnknownToken() {
		stubVerificationJwt("temporary-token", VerificationPurpose.SIGNUP);
		when(tokenHashService.hash("temporary-token")).thenReturn("token-hash");
		when(tokenCodeRepository.findFirstByTokenHashOrderByCreatedAtDesc("token-hash"))
				.thenReturn(Optional.empty());

		assertThatThrownBy(() -> tokenCodeService.findValidEmailVerificationToken(
				"Bearer temporary-token", "user@example.com", VerificationPurpose.SIGNUP))
				.isInstanceOf(CustomException.class)
				.extracting(exception -> ((CustomException) exception).getErrorCode())
				.isEqualTo(ErrorCode.INVALID_EMAIL_VERIFICATION_TOKEN);
	}

	@Test
	void verificationTokenRejectsWrongPurpose() {
		stubVerificationToken();
		when(tokenCode.getPurpose()).thenReturn(VerificationPurpose.EMAIL_CHANGE);

		assertThatThrownBy(() -> tokenCodeService.findValidEmailVerificationToken(
				"temporary-token", "user@example.com", VerificationPurpose.SIGNUP))
				.isInstanceOf(CustomException.class)
				.extracting(exception -> ((CustomException) exception).getErrorCode())
				.isEqualTo(ErrorCode.TOKEN_PURPOSE_MISMATCH);
	}

	@Test
	void verificationTokenRejectsExpiredToken() {
		stubVerificationToken();
		when(tokenCode.getPurpose()).thenReturn(VerificationPurpose.SIGNUP);
		when(tokenCode.isExpired(any(LocalDateTime.class))).thenReturn(true);

		assertThatThrownBy(() -> tokenCodeService.findValidEmailVerificationToken(
				"temporary-token", "user@example.com", VerificationPurpose.SIGNUP))
				.isInstanceOf(CustomException.class)
				.extracting(exception -> ((CustomException) exception).getErrorCode())
				.isEqualTo(ErrorCode.EMAIL_VERIFICATION_TOKEN_EXPIRED);
	}

	@Test
	void verificationTokenReturnsActiveMatchingToken() {
		stubVerificationToken();
		when(tokenCode.getPurpose()).thenReturn(VerificationPurpose.SIGNUP);
		when(tokenCode.isExpired(any(LocalDateTime.class))).thenReturn(false);
		when(tokenCode.getStatus()).thenReturn(TokenStatus.ACTIVE);
		when(tokenCode.getEmail()).thenReturn("user@example.com");

		TokenCode result = tokenCodeService.findValidEmailVerificationToken(
				"temporary-token", "user@example.com", VerificationPurpose.SIGNUP);

		assertThat(result).isSameAs(tokenCode);
	}

	@Test
	void emailCodeRejectsUnknownCode() {
		when(tokenCodeRepository.findFirstByEmailAndTypeAndPurposeAndStatusOrderByCreatedAtDesc(
				"user@example.com",
				TokenType.EMAIL_CODE,
				VerificationPurpose.PASSWORD_RESET,
				TokenStatus.ACTIVE))
				.thenReturn(Optional.empty());

		assertThatThrownBy(() -> tokenCodeService.findValidEmailCode(
				"user@example.com", "123456", VerificationPurpose.PASSWORD_RESET))
				.isInstanceOf(CustomException.class)
				.extracting(exception -> ((CustomException) exception).getErrorCode())
				.isEqualTo(ErrorCode.EMAIL_CODE_MISMATCH);
	}

	@Test
	void emailCodeRejectsExpiredCode() {
		stubEmailCode();
		when(tokenCode.isExpired(any(LocalDateTime.class))).thenReturn(true);

		assertThatThrownBy(() -> tokenCodeService.findValidEmailCode(
				"user@example.com", "123456", VerificationPurpose.PASSWORD_RESET))
				.isInstanceOf(CustomException.class)
				.extracting(exception -> ((CustomException) exception).getErrorCode())
				.isEqualTo(ErrorCode.EMAIL_CODE_EXPIRED);
	}

	@Test
	void emailCodeReturnsActiveMatchingCode() {
		stubEmailCode();
		when(tokenCode.isExpired(any(LocalDateTime.class))).thenReturn(false);

		TokenCode result = tokenCodeService.findValidEmailCode(
				"user@example.com", "123456", VerificationPurpose.PASSWORD_RESET);

		assertThat(result).isSameAs(tokenCode);
	}

	@Test
	void emailCodeIsRevokedAfterFifthFailedAttempt() {
		TokenCode storedCode = TokenCode.createEmailCode(
				null,
				"user@example.com",
				"correct-hash",
				VerificationPurpose.PASSWORD_RESET,
				LocalDateTime.now().plusMinutes(30));
		when(tokenCodeRepository.findFirstByEmailAndTypeAndPurposeAndStatusOrderByCreatedAtDesc(
				"user@example.com",
				TokenType.EMAIL_CODE,
				VerificationPurpose.PASSWORD_RESET,
				TokenStatus.ACTIVE))
				.thenReturn(Optional.of(storedCode));
		when(tokenHashService.hash("wrong-code")).thenReturn("wrong-hash");

		for (int attempt = 1; attempt < 5; attempt++) {
			assertThatThrownBy(() -> tokenCodeService.findValidEmailCode(
					"user@example.com", "wrong-code", VerificationPurpose.PASSWORD_RESET))
					.isInstanceOf(CustomException.class)
					.extracting(exception -> ((CustomException) exception).getErrorCode())
					.isEqualTo(ErrorCode.EMAIL_CODE_MISMATCH);
		}
		assertThatThrownBy(() -> tokenCodeService.findValidEmailCode(
				"user@example.com", "wrong-code", VerificationPurpose.PASSWORD_RESET))
				.isInstanceOf(CustomException.class)
				.extracting(exception -> ((CustomException) exception).getErrorCode())
				.isEqualTo(ErrorCode.EMAIL_CODE_ATTEMPT_LIMIT_EXCEEDED);
		assertThat(storedCode.getAttemptCount()).isEqualTo(5);
		assertThat(storedCode.getStatus()).isEqualTo(TokenStatus.REVOKED);
	}

	private void stubVerificationToken() {
		stubVerificationJwt("temporary-token", VerificationPurpose.SIGNUP);
		when(tokenHashService.hash("temporary-token")).thenReturn("token-hash");
		when(tokenCodeRepository.findFirstByTokenHashOrderByCreatedAtDesc("token-hash"))
				.thenReturn(Optional.of(tokenCode));
		when(tokenCode.getType()).thenReturn(TokenType.EMAIL_VERIFICATION);
	}

	private void stubVerificationJwt(String token, VerificationPurpose purpose) {
		when(jwtProvider.parseEmailVerificationToken(token)).thenReturn(claims);
		when(jwtProvider.getPurpose(claims)).thenReturn(purpose);
		lenient().when(jwtProvider.getEmail(claims)).thenReturn("user@example.com");
	}

	private void stubEmailCode() {
		when(tokenCodeRepository.findFirstByEmailAndTypeAndPurposeAndStatusOrderByCreatedAtDesc(
				"user@example.com",
				TokenType.EMAIL_CODE,
				VerificationPurpose.PASSWORD_RESET,
				TokenStatus.ACTIVE))
				.thenReturn(Optional.of(tokenCode));
		lenient().when(tokenCode.getAttemptCount()).thenReturn(0);
		lenient().when(tokenCode.getTokenHash()).thenReturn("code-hash");
		lenient().when(tokenHashService.hash("123456")).thenReturn("code-hash");
	}
}
