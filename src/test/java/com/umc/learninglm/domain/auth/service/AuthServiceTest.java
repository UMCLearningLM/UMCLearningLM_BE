package com.umc.learninglm.domain.auth.service;

import com.umc.learninglm.domain.auth.dto.request.LoginRequest;
import com.umc.learninglm.domain.auth.dto.request.EmailVerificationRequest;
import com.umc.learninglm.domain.auth.dto.request.EmailVerificationVerifyRequest;
import com.umc.learninglm.domain.auth.dto.request.LogoutRequest;
import com.umc.learninglm.domain.auth.dto.request.PasswordResetRequest;
import com.umc.learninglm.domain.auth.dto.request.ProfileUpdateRequest;
import com.umc.learninglm.domain.auth.dto.request.ReissueRequest;
import com.umc.learninglm.domain.auth.dto.request.SignupRequest;
import com.umc.learninglm.domain.auth.dto.response.AuthTokenResponse;
import com.umc.learninglm.domain.auth.dto.response.EmailVerificationResponse;
import com.umc.learninglm.domain.auth.dto.response.EmailVerificationVerifyResponse;
import com.umc.learninglm.domain.auth.dto.response.LogoutResponse;
import com.umc.learninglm.domain.auth.dto.response.MeResponse;
import com.umc.learninglm.domain.auth.dto.response.ProfileResponse;
import com.umc.learninglm.domain.auth.dto.response.ReissueResponse;
import com.umc.learninglm.domain.auth.entity.TokenCode;
import com.umc.learninglm.domain.auth.entity.User;
import com.umc.learninglm.domain.auth.enums.TokenStatus;
import com.umc.learninglm.domain.auth.enums.TokenType;
import com.umc.learninglm.domain.auth.enums.UserProvider;
import com.umc.learninglm.domain.auth.enums.VerificationPurpose;
import com.umc.learninglm.domain.auth.enums.VerificationType;
import com.umc.learninglm.domain.auth.repository.UserRepository;
import com.umc.learninglm.domain.auth.repository.TokenCodeRepository;
import com.umc.learninglm.global.error.CustomException;
import com.umc.learninglm.global.error.ErrorCode;
import com.umc.learninglm.global.security.jwt.JwtProvider;
import com.umc.learninglm.global.security.jwt.JwtEmailVerificationToken;
import com.umc.learninglm.global.security.jwt.JwtTokenPair;
import io.jsonwebtoken.Claims;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;
import java.util.List;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

	@Mock
	private UserRepository userRepository;

	@Mock
	private PasswordEncoder passwordEncoder;

	@Mock
	private TokenCodeService tokenCodeService;

	@Mock
	private TokenCodeRepository tokenCodeRepository;

	@Mock
	private TokenHashService tokenHashService;

	@Mock
	private JwtProvider jwtProvider;

	private AuthService authService;

	@BeforeEach
	void setUp() {
		authService = new AuthService(
				userRepository,
				passwordEncoder,
				tokenCodeService,
				tokenCodeRepository,
				tokenHashService,
				jwtProvider,
				1_800_000L,
				5,
				3_600_000L);
	}

	@Test
	void signupSavesLocalUserWithEncodedPassword() {
		SignupRequest request = new SignupRequest("user@example.com", "Password123!", "홍길동", true);
		TokenCode verificationToken = mock(TokenCode.class);
		when(tokenCodeService.findValidEmailVerificationToken(
				"Bearer verification-token", request.email(), com.umc.learninglm.domain.auth.enums.VerificationPurpose.SIGNUP))
				.thenReturn(verificationToken);
		when(userRepository.existsByEmail(request.email())).thenReturn(false);
		when(passwordEncoder.encode(request.password())).thenReturn("encoded-password");
		when(userRepository.saveAndFlush(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));
		stubTokenIssuance();

		AuthTokenResponse response = authService.signup("Bearer verification-token", request);

		assertThat(response.email()).isEqualTo(request.email());
		assertThat(response.nickname()).isEqualTo(request.nickname());
		assertThat(response.accessToken()).isEqualTo("access-token");
		assertThat(response.refreshToken()).isEqualTo("refresh-token");
		verify(passwordEncoder).encode(request.password());
		verify(userRepository).saveAndFlush(any(User.class));
		verify(verificationToken).markUsed(any(LocalDateTime.class));
	}

	@Test
	void signupRejectsDuplicateEmail() {
		SignupRequest request = new SignupRequest("user@example.com", "Password123!", "홍길동", true);
		when(tokenCodeService.findValidEmailVerificationToken(
				"Bearer verification-token", request.email(), com.umc.learninglm.domain.auth.enums.VerificationPurpose.SIGNUP))
				.thenReturn(mock(TokenCode.class));
		when(userRepository.existsByEmail(request.email())).thenReturn(true);

		assertThatThrownBy(() -> authService.signup("Bearer verification-token", request))
				.isInstanceOf(CustomException.class)
				.extracting(exception -> ((CustomException) exception).getErrorCode())
				.isEqualTo(ErrorCode.EMAIL_ALREADY_EXISTS);
		verify(userRepository, never()).save(any(User.class));
	}

	@Test
	void loginReturnsUserInformationAndMockTokens() {
		LoginRequest request = new LoginRequest("user@example.com", "Password123!", true);
		User user = User.createLocal(request.email(), "encoded-password", "홍길동");
		when(userRepository.findByEmail(request.email())).thenReturn(Optional.of(user));
		when(passwordEncoder.matches(request.password(), user.getPasswordHash())).thenReturn(true);
		stubTokenIssuance();

		AuthTokenResponse response = authService.login(request);

		assertThat(response.email()).isEqualTo(request.email());
		assertThat(response.nickname()).isEqualTo("홍길동");
		assertThat(response.accessToken()).isEqualTo("access-token");
		assertThat(response.refreshToken()).isEqualTo("refresh-token");
	}

	@Test
	void loginRejectsUnknownAccount() {
		LoginRequest request = new LoginRequest("missing@example.com", "Password123!", false);
		when(userRepository.findByEmail(request.email())).thenReturn(Optional.empty());

		assertThatThrownBy(() -> authService.login(request))
				.isInstanceOf(CustomException.class)
				.extracting(exception -> ((CustomException) exception).getErrorCode())
				.isEqualTo(ErrorCode.ACCOUNT_NOT_FOUND);
	}

	@Test
	void loginRejectsSocialAccount() {
		LoginRequest request = new LoginRequest("social@example.com", "Password123!", false);
		User user = org.mockito.Mockito.mock(User.class);
		when(user.getProvider()).thenReturn(UserProvider.GOOGLE);
		when(userRepository.findByEmail(request.email())).thenReturn(Optional.of(user));

		assertThatThrownBy(() -> authService.login(request))
				.isInstanceOf(CustomException.class)
				.extracting(exception -> ((CustomException) exception).getErrorCode())
				.isEqualTo(ErrorCode.SOCIAL_ACCOUNT_LOCAL_LOGIN);
	}

	@Test
	void loginRejectsWrongPassword() {
		LoginRequest request = new LoginRequest("user@example.com", "WrongPassword1!", false);
		User user = User.createLocal(request.email(), "encoded-password", "홍길동");
		when(userRepository.findByEmail(request.email())).thenReturn(Optional.of(user));
		when(passwordEncoder.matches(request.password(), user.getPasswordHash())).thenReturn(false);

		assertThatThrownBy(() -> authService.login(request))
				.isInstanceOf(CustomException.class)
				.extracting(exception -> ((CustomException) exception).getErrorCode())
				.isEqualTo(ErrorCode.PASSWORD_MISMATCH);
	}

	@Test
	void reissueRotatesStoredRefreshToken() {
		User user = User.createLocal("user@example.com", "encoded-password", "홍길동");
		TokenCode storedRefreshToken = mock(TokenCode.class);
		Claims refreshClaims = mock(Claims.class);
		JwtTokenPair newTokenPair = new JwtTokenPair(
				"new-access-token",
				"new-refresh-token",
				LocalDateTime.now().plusMinutes(30),
				LocalDateTime.now().plusDays(7));
		when(jwtProvider.parseRefreshToken("old-refresh-token")).thenReturn(refreshClaims);
		when(tokenHashService.hash("old-refresh-token")).thenReturn("old-refresh-hash");
		when(tokenCodeRepository.findFirstByTokenHashAndTypeOrderByCreatedAtDesc(
				"old-refresh-hash", TokenType.REFRESH))
				.thenReturn(Optional.of(storedRefreshToken));
		when(storedRefreshToken.isActive(any(LocalDateTime.class))).thenReturn(true);
		when(storedRefreshToken.getUser()).thenReturn(user);
		when(jwtProvider.getEmail(refreshClaims)).thenReturn("user@example.com");
		when(jwtProvider.isRememberMe(refreshClaims)).thenReturn(true);
		when(jwtProvider.createTokenPair(user, true)).thenReturn(newTokenPair);
		when(tokenHashService.hash("new-refresh-token")).thenReturn("new-refresh-hash");

		ReissueResponse response = authService.reissue(new ReissueRequest("old-refresh-token"));

		assertThat(response.accessToken()).isEqualTo("new-access-token");
		assertThat(response.refreshToken()).isEqualTo("new-refresh-token");
		verify(storedRefreshToken).markUsed(any(LocalDateTime.class));
		verify(tokenCodeRepository).save(any(TokenCode.class));
	}

	@Test
	void logoutRevokesRefreshTokenAndBlacklistsAccessToken() {
		User user = User.createLocal("user@example.com", "encoded-password", "홍길동");
		TokenCode storedRefreshToken = mock(TokenCode.class);
		Claims accessClaims = mock(Claims.class);
		Claims refreshClaims = mock(Claims.class);
		when(jwtProvider.parseAccessToken("access-token")).thenReturn(accessClaims);
		when(jwtProvider.parseRefreshToken("refresh-token")).thenReturn(refreshClaims);
		when(tokenHashService.hash("refresh-token")).thenReturn("refresh-hash");
		when(tokenCodeRepository.findFirstByTokenHashAndTypeOrderByCreatedAtDesc(
				"refresh-hash", TokenType.REFRESH))
				.thenReturn(Optional.of(storedRefreshToken));
		when(storedRefreshToken.isActive(any(LocalDateTime.class))).thenReturn(true);
		when(storedRefreshToken.getUser()).thenReturn(user);
		when(jwtProvider.getEmail(accessClaims)).thenReturn("user@example.com");
		when(jwtProvider.getEmail(refreshClaims)).thenReturn("user@example.com");
		when(tokenHashService.hash("access-token")).thenReturn("access-hash");
		when(tokenCodeRepository.existsByTokenHashAndTypeAndStatus(
				"access-hash", TokenType.ACCESS_BLACKLIST, TokenStatus.ACTIVE))
				.thenReturn(false);
		when(jwtProvider.getExpiration(accessClaims)).thenReturn(LocalDateTime.now().plusMinutes(30));

		LogoutResponse response = authService.logout(
				"Bearer access-token", new LogoutRequest("refresh-token"));

		assertThat(response.loggedOut()).isTrue();
		verify(storedRefreshToken).revoke(any(LocalDateTime.class));
		verify(tokenCodeRepository).save(org.mockito.ArgumentMatchers.argThat(tokenCode ->
				tokenCode.getType() == TokenType.ACCESS_BLACKLIST
						&& tokenCode.getTokenHash().equals("access-hash")));
	}

	@Test
	void meReturnsUserIdentifiedByAccessTokenEmail() {
		User user = User.createLocal("user@example.com", "encoded-password", "홍길동");
		when(userRepository.findByEmail("user@example.com")).thenReturn(Optional.of(user));

		MeResponse response = authService.me("user@example.com");

		assertThat(response.email()).isEqualTo("user@example.com");
		assertThat(response.nickname()).isEqualTo("홍길동");
		assertThat(response.loginType()).isEqualTo("LOCAL");
		assertThat(response.provider()).isNull();
	}

	@Test
	void requestEmailVerificationCreatesTenCharacterCodeAndStoresOnlyHash() {
		EmailVerificationRequest request = new EmailVerificationRequest(
				VerificationType.NON_LOGIN,
				VerificationPurpose.SIGNUP,
				"user@example.com");
		when(tokenCodeRepository.findAllByEmailAndTypeAndPurposeAndStatus(
				request.email(), TokenType.EMAIL_CODE, request.purpose(), TokenStatus.ACTIVE))
				.thenReturn(List.of());
		when(userRepository.findByEmail(request.email())).thenReturn(Optional.empty());
		when(tokenHashService.hash(anyString()))
				.thenAnswer(invocation -> "hashed-" + invocation.getArgument(0, String.class));

		EmailVerificationResponse response = authService.requestEmailVerification(null, request);

		assertThat(response.verificationCode()).matches("[A-Za-z0-9]{10}");
		assertThat(response.expiresInSeconds()).isEqualTo(1_800);
		verify(tokenCodeRepository).save(org.mockito.ArgumentMatchers.argThat(tokenCode ->
				tokenCode.getType() == TokenType.EMAIL_CODE
						&& tokenCode.getPurpose() == VerificationPurpose.SIGNUP
						&& tokenCode.getTokenHash().equals("hashed-" + response.verificationCode())
						&& !tokenCode.getTokenHash().equals(response.verificationCode())));
	}

	@Test
	void requestEmailVerificationRejectsSendLimitExceeded() {
		EmailVerificationRequest request = new EmailVerificationRequest(
				VerificationType.NON_LOGIN,
				VerificationPurpose.SIGNUP,
				"user@example.com");
		when(tokenCodeRepository.countByEmailAndTypeAndCreatedAtAfter(
				org.mockito.ArgumentMatchers.eq(request.email()),
				org.mockito.ArgumentMatchers.eq(TokenType.EMAIL_CODE),
				any(LocalDateTime.class)))
				.thenReturn(5L);

		assertThatThrownBy(() -> authService.requestEmailVerification(null, request))
				.isInstanceOf(CustomException.class)
				.extracting(exception -> ((CustomException) exception).getErrorCode())
				.isEqualTo(ErrorCode.EMAIL_CODE_SEND_LIMIT_EXCEEDED);
		verify(tokenCodeRepository, never()).save(any(TokenCode.class));
	}

	@Test
	void requestEmailChangeVerificationTargetsNewEmailAndBindsCurrentUser() {
		User currentUser = User.createLocal("old@example.com", "password-hash", "홍길동");
		EmailVerificationRequest request = new EmailVerificationRequest(
				VerificationType.LOGIN,
				VerificationPurpose.EMAIL_CHANGE,
				"new@example.com");
		when(tokenCodeRepository.findAllByEmailAndTypeAndPurposeAndStatus(
				request.email(), TokenType.EMAIL_CODE, request.purpose(), TokenStatus.ACTIVE))
				.thenReturn(List.of());
		when(userRepository.findByEmail("old@example.com")).thenReturn(Optional.of(currentUser));
		when(tokenHashService.hash(anyString())).thenReturn("code-hash");

		EmailVerificationResponse response = authService.requestEmailVerification(
				"old@example.com",
				request);

		assertThat(response.email()).isEqualTo("new@example.com");
		verify(tokenCodeRepository).save(org.mockito.ArgumentMatchers.argThat(tokenCode ->
				tokenCode.getUser() == currentUser
						&& tokenCode.getEmail().equals("new@example.com")
						&& tokenCode.getPurpose() == VerificationPurpose.EMAIL_CHANGE));
	}

	@Test
	void verifyEmailCodeConsumesCodeAndStoresEmailVerificationJwtHash() {
		EmailVerificationVerifyRequest request = new EmailVerificationVerifyRequest(
				VerificationType.NON_LOGIN,
				VerificationPurpose.PASSWORD_RESET,
				"user@example.com",
				"A1b2C3d4E5");
		TokenCode emailCode = mock(TokenCode.class);
		JwtEmailVerificationToken temporaryToken = new JwtEmailVerificationToken(
				"email-verification-token",
				LocalDateTime.now().plusMinutes(30));
		when(tokenCodeService.findValidEmailCode(request.email(), request.code(), request.purpose()))
				.thenReturn(emailCode);
		when(tokenCodeRepository.findAllByEmailAndTypeAndPurposeAndStatus(
				request.email(), TokenType.EMAIL_VERIFICATION, request.purpose(), TokenStatus.ACTIVE))
				.thenReturn(List.of());
		when(jwtProvider.createEmailVerificationToken(request.email(), request.purpose()))
				.thenReturn(temporaryToken);
		when(userRepository.findByEmail(request.email())).thenReturn(Optional.empty());
		when(tokenHashService.hash(temporaryToken.token())).thenReturn("temporary-token-hash");

		EmailVerificationVerifyResponse response = authService.verifyEmailCode(null, request);

		assertThat(response.verified()).isTrue();
		assertThat(response.temporaryAccessToken()).isEqualTo(temporaryToken.token());
		assertThat(response.expiresInSeconds()).isEqualTo(1_800);
		verify(emailCode).markUsed(any(LocalDateTime.class));
		verify(tokenCodeRepository).save(org.mockito.ArgumentMatchers.argThat(tokenCode ->
				tokenCode.getType() == TokenType.EMAIL_VERIFICATION
						&& tokenCode.getPurpose() == VerificationPurpose.PASSWORD_RESET
						&& tokenCode.getTokenHash().equals("temporary-token-hash")));
	}

	@Test
	void resetPasswordUsesAndConsumesPasswordResetTokenOnly() {
		TokenCode verificationToken = mock(TokenCode.class);
		User user = User.createLocal("user@example.com", "old-password-hash", "홍길동");
		when(tokenCodeService.findValidEmailVerificationToken(
				"Bearer temporary-token", VerificationPurpose.PASSWORD_RESET))
				.thenReturn(verificationToken);
		when(verificationToken.getEmail()).thenReturn("user@example.com");
		when(userRepository.findByEmail("user@example.com")).thenReturn(Optional.of(user));
		when(passwordEncoder.matches("NewPassword123!", "old-password-hash")).thenReturn(false);
		when(passwordEncoder.encode("NewPassword123!")).thenReturn("new-password-hash");

		authService.resetPassword(
				"Bearer temporary-token",
				new PasswordResetRequest("NewPassword123!"));

		assertThat(user.getPasswordHash()).isEqualTo("new-password-hash");
		verify(verificationToken).markUsed(any(LocalDateTime.class));
	}

	@Test
	void resetPasswordRejectsSocialAccount() {
		TokenCode verificationToken = mock(TokenCode.class);
		User user = mock(User.class);
		when(tokenCodeService.findValidEmailVerificationToken(
				"temporary-token", VerificationPurpose.PASSWORD_RESET))
				.thenReturn(verificationToken);
		when(verificationToken.getEmail()).thenReturn("social@example.com");
		when(userRepository.findByEmail("social@example.com")).thenReturn(Optional.of(user));
		when(user.getProvider()).thenReturn(UserProvider.GOOGLE);

		assertThatThrownBy(() -> authService.resetPassword(
				"temporary-token",
				new PasswordResetRequest("NewPassword123!")))
				.isInstanceOf(CustomException.class)
				.extracting(exception -> ((CustomException) exception).getErrorCode())
				.isEqualTo(ErrorCode.SOCIAL_PASSWORD_CHANGE_NOT_ALLOWED);
		verify(verificationToken, never()).markUsed(any(LocalDateTime.class));
		verify(passwordEncoder, never()).encode(anyString());
	}

	@Test
	void updateProfileChangesNicknameOnAuthenticatedUser() {
		User user = User.createLocal("user@example.com", "password-hash", "기존닉네임");
		when(userRepository.findByEmail("user@example.com")).thenReturn(Optional.of(user));
		when(userRepository.saveAndFlush(user)).thenReturn(user);

		ProfileResponse response = authService.updateProfile(
				"user@example.com",
				null,
				new ProfileUpdateRequest(null, null, null, "새닉네임"));

		assertThat(response.nickname()).isEqualTo("새닉네임");
		verify(userRepository).saveAndFlush(user);
	}

	@Test
	void updateProfileUsesEmailChangeTokenBoundToAuthenticatedUser() {
		User user = mock(User.class);
		TokenCode verificationToken = mock(TokenCode.class);
		when(user.getUserId()).thenReturn(1L);
		when(user.getEmail()).thenReturn("old@example.com");
		when(userRepository.findByEmail("old@example.com")).thenReturn(Optional.of(user));
		when(tokenCodeService.findValidEmailVerificationToken(
				"temporary-token",
				"new@example.com",
				VerificationPurpose.EMAIL_CHANGE))
				.thenReturn(verificationToken);
		when(verificationToken.getUser()).thenReturn(user);
		when(userRepository.existsByEmail("new@example.com")).thenReturn(false);
		when(userRepository.saveAndFlush(user)).thenReturn(user);

		authService.updateProfile(
				"old@example.com",
				"temporary-token",
				new ProfileUpdateRequest("new@example.com", null, null, null));

		verify(user).changeEmail("new@example.com");
		verify(verificationToken).markUsed(any(LocalDateTime.class));
	}

	private void stubTokenIssuance() {
		JwtTokenPair tokenPair = new JwtTokenPair(
				"access-token",
				"refresh-token",
				LocalDateTime.now().plusMinutes(30),
				LocalDateTime.now().plusDays(1));
		when(jwtProvider.createTokenPair(any(User.class), org.mockito.ArgumentMatchers.anyBoolean()))
				.thenReturn(tokenPair);
		when(tokenHashService.hash("refresh-token")).thenReturn("refresh-token-hash");
		when(tokenCodeRepository.save(any(com.umc.learninglm.domain.auth.entity.TokenCode.class)))
				.thenAnswer(invocation -> invocation.getArgument(0));
	}

}
