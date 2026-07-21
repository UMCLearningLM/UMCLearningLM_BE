package com.umc.learninglm.domain.auth.service;

import com.umc.learninglm.domain.auth.dto.request.EmailVerificationRequest;
import com.umc.learninglm.domain.auth.dto.request.EmailVerificationVerifyRequest;
import com.umc.learninglm.domain.auth.dto.request.LoginRequest;
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
import com.umc.learninglm.domain.auth.dto.response.PasswordResetResponse;
import com.umc.learninglm.domain.auth.dto.response.ProfileResponse;
import com.umc.learninglm.domain.auth.dto.response.ReissueResponse;
import com.umc.learninglm.domain.auth.entity.TokenCode;
import com.umc.learninglm.domain.auth.entity.User;
import com.umc.learninglm.domain.auth.enums.TokenStatus;
import com.umc.learninglm.domain.auth.enums.TokenType;
import com.umc.learninglm.domain.auth.enums.UserProvider;
import com.umc.learninglm.domain.auth.enums.VerificationPurpose;
import com.umc.learninglm.domain.auth.enums.VerificationType;
import com.umc.learninglm.domain.auth.repository.TokenCodeRepository;
import com.umc.learninglm.domain.auth.repository.UserRepository;
import com.umc.learninglm.global.error.CustomException;
import com.umc.learninglm.global.error.ErrorCode;
import com.umc.learninglm.global.security.jwt.JwtEmailVerificationToken;
import com.umc.learninglm.global.security.jwt.JwtProvider;
import com.umc.learninglm.global.security.jwt.JwtTokenPair;
import io.jsonwebtoken.Claims;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

@Service
public class AuthService {

	private static final String BEARER_PREFIX = "Bearer ";
	private static final String VERIFICATION_CODE_CHARACTERS =
			"ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
	private static final int VERIFICATION_CODE_LENGTH = 10;
	private static final long MILLISECONDS_PER_SECOND = 1_000L;

	private final UserRepository userRepository;
	private final PasswordEncoder passwordEncoder;
	private final TokenCodeService tokenCodeService;
	private final TokenCodeRepository tokenCodeRepository;
	private final TokenHashService tokenHashService;
	private final JwtProvider jwtProvider;
	private final long emailCodeExpirationMs;
	private final int maxEmailCodeSendAttempts;
	private final long emailCodeSendWindowMs;
	private final SecureRandom secureRandom = new SecureRandom();

	public AuthService(
			UserRepository userRepository,
			PasswordEncoder passwordEncoder,
			TokenCodeService tokenCodeService,
			TokenCodeRepository tokenCodeRepository,
			TokenHashService tokenHashService,
			JwtProvider jwtProvider,
			@Value("${email-verification.code-expiration-ms}") long emailCodeExpirationMs,
			@Value("${email-verification.max-send-attempts}") int maxEmailCodeSendAttempts,
			@Value("${email-verification.send-window-ms}") long emailCodeSendWindowMs) {
		this.userRepository = userRepository;
		this.passwordEncoder = passwordEncoder;
		this.tokenCodeService = tokenCodeService;
		this.tokenCodeRepository = tokenCodeRepository;
		this.tokenHashService = tokenHashService;
		this.jwtProvider = jwtProvider;
		this.emailCodeExpirationMs = emailCodeExpirationMs;
		this.maxEmailCodeSendAttempts = maxEmailCodeSendAttempts;
		this.emailCodeSendWindowMs = emailCodeSendWindowMs;
	}

	@Transactional
	public AuthTokenResponse signup(String emailVerificationToken, SignupRequest request) {
		TokenCode verificationToken = tokenCodeService.findValidEmailVerificationToken(
				emailVerificationToken, request.email(), VerificationPurpose.SIGNUP);

		if (userRepository.existsByEmail(request.email())) {
			throw new CustomException(ErrorCode.EMAIL_ALREADY_EXISTS);
		}

		User user = User.createLocal(
				request.email(),
				passwordEncoder.encode(request.password()),
				request.nickname());
		User savedUser;
		try {
			savedUser = userRepository.saveAndFlush(user);
		} catch (DataIntegrityViolationException e) {
			throw new CustomException(ErrorCode.EMAIL_ALREADY_EXISTS);
		}
		verificationToken.markUsed(LocalDateTime.now());
		return issueAuthTokens(savedUser, false);
	}

	@Transactional
	public AuthTokenResponse login(LoginRequest request) {
		User user = userRepository.findByEmail(request.email())
				.orElseThrow(() -> new CustomException(ErrorCode.ACCOUNT_NOT_FOUND));

		if (user.getProvider() != UserProvider.LOCAL) {
			throw new CustomException(ErrorCode.SOCIAL_ACCOUNT_LOCAL_LOGIN);
		}
		if (!passwordEncoder.matches(request.password(), user.getPasswordHash())) {
			throw new CustomException(ErrorCode.PASSWORD_MISMATCH);
		}

		return issueAuthTokens(user, request.rememberMe());
	}

	@Transactional
	public ReissueResponse reissue(ReissueRequest request) {
		if (request.refreshToken() == null || request.refreshToken().isBlank()) {
			throw new CustomException(ErrorCode.REFRESH_TOKEN_MISSING);
		}

		Claims refreshClaims = jwtProvider.parseRefreshToken(request.refreshToken());
		TokenCode storedRefreshToken = findStoredRefreshToken(request.refreshToken());
		validateRefreshOwner(storedRefreshToken, refreshClaims);
		storedRefreshToken.markUsed(LocalDateTime.now());

		JwtTokenPair tokenPair = createAndStoreTokenPair(
				storedRefreshToken.getUser(),
				jwtProvider.isRememberMe(refreshClaims));
		return new ReissueResponse(tokenPair.accessToken(), tokenPair.refreshToken());
	}

	@Transactional
	public LogoutResponse logout(String authorization, LogoutRequest request) {
		if (request.refreshToken() == null || request.refreshToken().isBlank()) {
			throw new CustomException(ErrorCode.REFRESH_TOKEN_MISSING);
		}
		String accessToken = resolveBearerToken(authorization);
		Claims accessClaims = jwtProvider.parseAccessToken(accessToken);
		Claims refreshClaims = jwtProvider.parseRefreshToken(request.refreshToken());
		TokenCode storedRefreshToken = findStoredRefreshToken(request.refreshToken());
		validateRefreshOwner(storedRefreshToken, refreshClaims);
		if (!jwtProvider.getEmail(accessClaims).equals(jwtProvider.getEmail(refreshClaims))) {
			throw new CustomException(ErrorCode.INVALID_REFRESH_TOKEN);
		}

		storedRefreshToken.revoke(LocalDateTime.now());
		String accessTokenHash = tokenHashService.hash(accessToken);
		if (!tokenCodeRepository.existsByTokenHashAndTypeAndStatus(
				accessTokenHash,
				TokenType.ACCESS_BLACKLIST,
				TokenStatus.ACTIVE)) {
			tokenCodeRepository.save(TokenCode.createAccessBlacklist(
					storedRefreshToken.getUser(),
					accessTokenHash,
					jwtProvider.getExpiration(accessClaims)));
		}
		return new LogoutResponse(true);
	}

	@Transactional(readOnly = true)
	public MeResponse me(String email) {
		User user = userRepository.findByEmail(email)
				.orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));
		boolean localAccount = user.getProvider() == UserProvider.LOCAL;
		return new MeResponse(
				user.getUserId(),
				user.getEmail(),
				user.getNickname(),
				localAccount ? "LOCAL" : "SOCIAL",
				localAccount ? null : user.getProvider().name());
	}

	@Transactional
	public ProfileResponse updateProfile(
			String authenticatedEmail,
			String emailVerificationToken,
			ProfileUpdateRequest request) {
		if (request.email() == null && request.newPassword() == null && request.nickname() == null) {
			throw new CustomException(ErrorCode.NO_PROFILE_CHANGES);
		}
		User user = userRepository.findByEmail(authenticatedEmail)
				.orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

		if (request.email() != null) {
			changeEmail(user, emailVerificationToken, request.email());
		}
		if (request.newPassword() != null) {
			changePassword(user, request.currentPassword(), request.newPassword());
		}
		if (request.nickname() != null) {
			user.changeNickname(request.nickname());
		}

		User savedUser;
		try {
			savedUser = userRepository.saveAndFlush(user);
		} catch (DataIntegrityViolationException e) {
			throw new CustomException(ErrorCode.EMAIL_ALREADY_EXISTS);
		}
		return new ProfileResponse(
				savedUser.getUserId(),
				savedUser.getEmail(),
				savedUser.getNickname(),
				savedUser.getProvider(),
				savedUser.getRole(),
				savedUser.getStatus(),
				savedUser.getUpdatedAt());
	}

	@Transactional
	public EmailVerificationResponse requestEmailVerification(
			String authenticatedEmail,
			EmailVerificationRequest request) {
		String email = resolveVerificationEmail(
				authenticatedEmail,
				request.verificationType(),
				request.purpose(),
				request.email());
		validateEmailCodeSendLimit(email);
		revokeActiveTokens(email, TokenType.EMAIL_CODE, request.purpose());

		String verificationCode = generateVerificationCode();
		LocalDateTime expiresAt = LocalDateTime.now().plus(Duration.ofMillis(emailCodeExpirationMs));
		User user = resolveVerificationUser(authenticatedEmail, request.verificationType(), email);
		tokenCodeRepository.save(TokenCode.createEmailCode(
				user,
				email,
				tokenHashService.hash(verificationCode),
				request.purpose(),
				expiresAt));

		return new EmailVerificationResponse(
				request.verificationType(),
				request.purpose(),
				email,
				toExpirationSeconds(emailCodeExpirationMs),
				verificationCode);
	}

	@Transactional(noRollbackFor = CustomException.class)
	public EmailVerificationVerifyResponse verifyEmailCode(
			String authenticatedEmail,
			EmailVerificationVerifyRequest request) {
		String email = resolveVerificationEmail(
				authenticatedEmail,
				request.verificationType(),
				request.purpose(),
				request.email());
		TokenCode emailCode = tokenCodeService.findValidEmailCode(
				email,
				request.code(),
				request.purpose());
		emailCode.markUsed(LocalDateTime.now());

		revokeActiveTokens(email, TokenType.EMAIL_VERIFICATION, request.purpose());
		JwtEmailVerificationToken verificationToken =
				jwtProvider.createEmailVerificationToken(email, request.purpose());
		User user = emailCode.getUser() != null
				? emailCode.getUser()
				: userRepository.findByEmail(email).orElse(null);
		tokenCodeRepository.save(TokenCode.createEmailVerification(
				user,
				email,
				tokenHashService.hash(verificationToken.token()),
				request.purpose(),
				verificationToken.expiresAt()));

		return new EmailVerificationVerifyResponse(
				request.verificationType(),
				request.purpose(),
				email,
				true,
				verificationToken.token(),
				toExpirationSeconds(emailCodeExpirationMs));
	}

	@Transactional
	public PasswordResetResponse resetPassword(
			String emailVerificationToken,
			PasswordResetRequest request) {
		TokenCode verificationToken = tokenCodeService.findValidEmailVerificationToken(
				emailVerificationToken, VerificationPurpose.PASSWORD_RESET);
		String email = verificationToken.getEmail() != null
				? verificationToken.getEmail()
				: verificationToken.getUser().getEmail();
		User user = userRepository.findByEmail(email)
				.orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));
		if (user.getProvider() != UserProvider.LOCAL) {
			throw new CustomException(ErrorCode.SOCIAL_PASSWORD_CHANGE_NOT_ALLOWED);
		}
		if (passwordEncoder.matches(request.newPassword(), user.getPasswordHash())) {
			throw new CustomException(ErrorCode.SAME_AS_CURRENT_PASSWORD);
		}
		user.changePassword(passwordEncoder.encode(request.newPassword()));
		verificationToken.markUsed(LocalDateTime.now());

		return new PasswordResetResponse(true, true);
	}

	private void changeEmail(User user, String emailVerificationToken, String newEmail) {
		if (user.getEmail().equals(newEmail)) {
			throw new CustomException(ErrorCode.SAME_AS_CURRENT_EMAIL);
		}
		TokenCode verificationToken = tokenCodeService.findValidEmailVerificationToken(
				emailVerificationToken,
				newEmail,
				VerificationPurpose.EMAIL_CHANGE);
		if (verificationToken.getUser() == null
				|| !Objects.equals(verificationToken.getUser().getUserId(), user.getUserId())) {
			throw new CustomException(ErrorCode.EMAIL_CHANGE_NOT_ALLOWED);
		}
		if (userRepository.existsByEmail(newEmail)) {
			throw new CustomException(ErrorCode.EMAIL_ALREADY_EXISTS);
		}
		user.changeEmail(newEmail);
		verificationToken.markUsed(LocalDateTime.now());
	}

	private void changePassword(User user, String currentPassword, String newPassword) {
		if (user.getProvider() != UserProvider.LOCAL) {
			throw new CustomException(ErrorCode.SOCIAL_PASSWORD_CHANGE_NOT_ALLOWED);
		}
		if (currentPassword == null || currentPassword.isBlank()) {
			throw new CustomException(ErrorCode.CURRENT_PASSWORD_MISSING);
		}
		if (!passwordEncoder.matches(currentPassword, user.getPasswordHash())) {
			throw new CustomException(ErrorCode.CURRENT_PASSWORD_MISMATCH);
		}
		if (passwordEncoder.matches(newPassword, user.getPasswordHash())) {
			throw new CustomException(ErrorCode.SAME_AS_CURRENT_PASSWORD);
		}
		user.changePassword(passwordEncoder.encode(newPassword));
	}

	private String resolveVerificationEmail(
			String authenticatedEmail,
			VerificationType verificationType,
			VerificationPurpose purpose,
			String requestedEmail) {
		if (verificationType == null) {
			throw new CustomException(ErrorCode.VERIFICATION_TYPE_MISSING);
		}
		if (purpose == null) {
			throw new CustomException(ErrorCode.VERIFICATION_PURPOSE_MISSING);
		}
		if (verificationType == VerificationType.LOGIN) {
			if (authenticatedEmail == null || authenticatedEmail.isBlank()) {
				throw new CustomException(ErrorCode.ACCESS_TOKEN_MISSING);
			}
			if (purpose != VerificationPurpose.EMAIL_CHANGE) {
				throw new CustomException(ErrorCode.VERIFICATION_PURPOSE_INVALID);
			}
			if (requestedEmail == null || requestedEmail.isBlank()) {
				throw new CustomException(ErrorCode.EMAIL_REQUIRED);
			}
			return requestedEmail;
		}
		if (purpose == VerificationPurpose.EMAIL_CHANGE) {
			throw new CustomException(ErrorCode.VERIFICATION_PURPOSE_INVALID);
		}
		if (requestedEmail == null || requestedEmail.isBlank()) {
			throw new CustomException(ErrorCode.EMAIL_REQUIRED);
		}
		return requestedEmail;
	}

	private User resolveVerificationUser(
			String authenticatedEmail,
			VerificationType verificationType,
			String targetEmail) {
		if (verificationType == VerificationType.LOGIN) {
			return userRepository.findByEmail(authenticatedEmail)
					.orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));
		}
		return userRepository.findByEmail(targetEmail).orElse(null);
	}

	private void validateEmailCodeSendLimit(String email) {
		LocalDateTime windowStart = LocalDateTime.now()
				.minus(Duration.ofMillis(emailCodeSendWindowMs));
		long sendCount = tokenCodeRepository.countByEmailAndTypeAndCreatedAtAfter(
				email,
				TokenType.EMAIL_CODE,
				windowStart);
		if (sendCount >= maxEmailCodeSendAttempts) {
			throw new CustomException(ErrorCode.EMAIL_CODE_SEND_LIMIT_EXCEEDED);
		}
	}

	private String generateVerificationCode() {
		StringBuilder code = new StringBuilder(VERIFICATION_CODE_LENGTH);
		for (int index = 0; index < VERIFICATION_CODE_LENGTH; index++) {
			int characterIndex = secureRandom.nextInt(VERIFICATION_CODE_CHARACTERS.length());
			code.append(VERIFICATION_CODE_CHARACTERS.charAt(characterIndex));
		}
		return code.toString();
	}

	private void revokeActiveTokens(
			String email,
			TokenType type,
			VerificationPurpose purpose) {
		List<TokenCode> activeTokens = tokenCodeRepository
				.findAllByEmailAndTypeAndPurposeAndStatus(
						email,
						type,
						purpose,
						TokenStatus.ACTIVE);
		LocalDateTime revokedAt = LocalDateTime.now();
		activeTokens.forEach(token -> token.revoke(revokedAt));
	}

	private int toExpirationSeconds(long expirationMs) {
		return Math.toIntExact(expirationMs / MILLISECONDS_PER_SECOND);
	}

	private AuthTokenResponse issueAuthTokens(User user, boolean rememberMe) {
		JwtTokenPair tokenPair = createAndStoreTokenPair(user, rememberMe);
		return new AuthTokenResponse(
				user.getUserId(),
				user.getEmail(),
				user.getNickname(),
				tokenPair.accessToken(),
				tokenPair.refreshToken());
	}

	private JwtTokenPair createAndStoreTokenPair(User user, boolean rememberMe) {
		JwtTokenPair tokenPair = jwtProvider.createTokenPair(user, rememberMe);
		tokenCodeRepository.save(TokenCode.createRefresh(
				user,
				tokenHashService.hash(tokenPair.refreshToken()),
				tokenPair.refreshTokenExpiresAt()));
		return tokenPair;
	}

	private TokenCode findStoredRefreshToken(String refreshToken) {
		return tokenCodeRepository.findFirstByTokenHashAndTypeOrderByCreatedAtDesc(
				tokenHashService.hash(refreshToken),
				TokenType.REFRESH)
				.filter(tokenCode -> tokenCode.isActive(LocalDateTime.now()))
				.orElseThrow(() -> new CustomException(ErrorCode.SESSION_EXPIRED));
	}

	private void validateRefreshOwner(TokenCode tokenCode, Claims claims) {
		if (tokenCode.getUser() == null
				|| !tokenCode.getUser().getEmail().equals(jwtProvider.getEmail(claims))) {
			throw new CustomException(ErrorCode.INVALID_REFRESH_TOKEN);
		}
	}

	private String resolveBearerToken(String authorization) {
		if (authorization == null || !authorization.startsWith(BEARER_PREFIX)) {
			throw new CustomException(ErrorCode.ACCESS_TOKEN_MISSING);
		}
		String token = authorization.substring(BEARER_PREFIX.length());
		if (token.isBlank()) {
			throw new CustomException(ErrorCode.ACCESS_TOKEN_MISSING);
		}
		return token;
	}
}
