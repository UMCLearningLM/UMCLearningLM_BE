package com.umc.learninglm.domain.auth.service;

import com.umc.learninglm.domain.auth.dto.response.AuthTokenResponse;
import com.umc.learninglm.domain.auth.entity.User;
import com.umc.learninglm.domain.auth.enums.UserProvider;
import com.umc.learninglm.domain.auth.repository.UserRepository;
import com.umc.learninglm.domain.auth.repository.TokenCodeRepository;
import com.umc.learninglm.global.error.CustomException;
import com.umc.learninglm.global.error.ErrorCode;
import com.umc.learninglm.global.security.jwt.JwtProvider;
import com.umc.learninglm.global.security.jwt.JwtTokenPair;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SocialLoginServiceTest {

	@Mock
	private UserRepository userRepository;

	@Mock
	private TokenCodeRepository tokenCodeRepository;

	@Mock
	private TokenHashService tokenHashService;

	@Mock
	private JwtProvider jwtProvider;

	private SocialLoginService socialLoginService;

	@BeforeEach
	void setUp() {
		socialLoginService = new SocialLoginService(
				userRepository, tokenCodeRepository, tokenHashService, jwtProvider);
	}

	@Test
	void loginReturnsExistingSocialUser() {
		User user = User.createSocial(
				"google@example.com", UserProvider.GOOGLE, "google-sub-123", "구글사용자");
		when(userRepository.findByProviderAndProviderId(UserProvider.GOOGLE, "google-sub-123"))
				.thenReturn(Optional.of(user));
		stubTokenIssuance();

		AuthTokenResponse response = socialLoginService.login(
				UserProvider.GOOGLE, "google-sub-123", "google@example.com", "구글사용자");

		assertThat(response.email()).isEqualTo("google@example.com");
		assertThat(response.nickname()).isEqualTo("구글사용자");
		assertThat(response.accessToken()).isEqualTo("access-token");
		verify(userRepository, never()).save(any(User.class));
	}

	@Test
	void loginCreatesSocialUserWhenNotRegistered() {
		when(userRepository.findByProviderAndProviderId(UserProvider.GOOGLE, "google-sub-123"))
				.thenReturn(Optional.empty());
		when(userRepository.existsByEmail("google@example.com")).thenReturn(false);
		when(userRepository.saveAndFlush(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));
		stubTokenIssuance();

		AuthTokenResponse response = socialLoginService.login(
				UserProvider.GOOGLE, "google-sub-123", "google@example.com", "구글사용자");

		assertThat(response.email()).isEqualTo("google@example.com");
		assertThat(response.nickname()).isEqualTo("구글사용자");
		verify(userRepository).saveAndFlush(org.mockito.ArgumentMatchers.argThat(user ->
				user.getProvider() == UserProvider.GOOGLE
						&& user.getProviderId().equals("google-sub-123")
						&& user.getPasswordHash() == null));
	}

	@Test
	void loginUsesEmailPrefixWhenNicknameIsMissing() {
		when(userRepository.findByProviderAndProviderId(UserProvider.GOOGLE, "google-sub-123"))
				.thenReturn(Optional.empty());
		when(userRepository.existsByEmail("google-user@example.com")).thenReturn(false);
		when(userRepository.saveAndFlush(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));
		stubTokenIssuance();

		AuthTokenResponse response = socialLoginService.login(
				UserProvider.GOOGLE, "google-sub-123", "google-user@example.com", null);

		assertThat(response.nickname()).isEqualTo("google-user");
	}

	@Test
	void loginRejectsEmailUsedByAnotherAccount() {
		when(userRepository.findByProviderAndProviderId(UserProvider.GOOGLE, "google-sub-123"))
				.thenReturn(Optional.empty());
		when(userRepository.existsByEmail("user@example.com")).thenReturn(true);

		assertThatThrownBy(() -> socialLoginService.login(
				UserProvider.GOOGLE, "google-sub-123", "user@example.com", "홍길동"))
				.isInstanceOf(CustomException.class)
				.extracting(exception -> ((CustomException) exception).getErrorCode())
				.isEqualTo(ErrorCode.EMAIL_ALREADY_EXISTS);
		verify(userRepository, never()).save(any(User.class));
	}

	private void stubTokenIssuance() {
		JwtTokenPair tokenPair = new JwtTokenPair(
				"access-token",
				"refresh-token",
				LocalDateTime.now().plusMinutes(30),
				LocalDateTime.now().plusDays(1));
		when(jwtProvider.createTokenPair(any(User.class), org.mockito.ArgumentMatchers.eq(false)))
				.thenReturn(tokenPair);
		when(tokenHashService.hash("refresh-token")).thenReturn("refresh-token-hash");
		when(tokenCodeRepository.save(any(com.umc.learninglm.domain.auth.entity.TokenCode.class)))
				.thenAnswer(invocation -> invocation.getArgument(0));
	}
}
