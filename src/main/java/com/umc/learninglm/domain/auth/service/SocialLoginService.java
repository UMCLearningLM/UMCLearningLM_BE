package com.umc.learninglm.domain.auth.service;

import com.umc.learninglm.domain.auth.dto.response.AuthTokenResponse;
import com.umc.learninglm.domain.auth.entity.User;
import com.umc.learninglm.domain.auth.entity.TokenCode;
import com.umc.learninglm.domain.auth.enums.UserProvider;
import com.umc.learninglm.domain.auth.repository.TokenCodeRepository;
import com.umc.learninglm.domain.auth.repository.UserRepository;
import com.umc.learninglm.global.error.CustomException;
import com.umc.learninglm.global.error.ErrorCode;
import com.umc.learninglm.global.security.jwt.JwtProvider;
import com.umc.learninglm.global.security.jwt.JwtTokenPair;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.dao.DataIntegrityViolationException;

@Service
public class SocialLoginService {

	private static final int MAX_NICKNAME_LENGTH = 50;

	private final UserRepository userRepository;
	private final TokenCodeRepository tokenCodeRepository;
	private final TokenHashService tokenHashService;
	private final JwtProvider jwtProvider;

	public SocialLoginService(
			UserRepository userRepository,
			TokenCodeRepository tokenCodeRepository,
			TokenHashService tokenHashService,
			JwtProvider jwtProvider) {
		this.userRepository = userRepository;
		this.tokenCodeRepository = tokenCodeRepository;
		this.tokenHashService = tokenHashService;
		this.jwtProvider = jwtProvider;
	}

	@Transactional
	public AuthTokenResponse login(
			UserProvider provider,
			String providerId,
			String email,
			String nickname) {
		User user = userRepository.findByProviderAndProviderId(provider, providerId)
				.orElseGet(() -> createSocialUser(provider, providerId, email, nickname));
		return toAuthTokenResponse(user);
	}

	private User createSocialUser(
			UserProvider provider,
			String providerId,
			String email,
			String nickname) {
		if (userRepository.existsByEmail(email)) {
			throw new CustomException(ErrorCode.EMAIL_ALREADY_EXISTS);
		}
		try {
			return userRepository.saveAndFlush(User.createSocial(
					email,
					provider,
					providerId,
					normalizeNickname(nickname, email)));
		} catch (DataIntegrityViolationException e) {
			throw new CustomException(ErrorCode.EMAIL_ALREADY_EXISTS);
		}
	}

	private String normalizeNickname(String nickname, String email) {
		String normalizedNickname = nickname;
		if (normalizedNickname == null || normalizedNickname.isBlank()) {
			int atIndex = email.indexOf('@');
			normalizedNickname = atIndex > 0 ? email.substring(0, atIndex) : email;
		}
		return normalizedNickname.length() > MAX_NICKNAME_LENGTH
				? normalizedNickname.substring(0, MAX_NICKNAME_LENGTH)
				: normalizedNickname;
	}

	private AuthTokenResponse toAuthTokenResponse(User user) {
		JwtTokenPair tokenPair = jwtProvider.createTokenPair(user, false);
		tokenCodeRepository.save(TokenCode.createRefresh(
				user,
				tokenHashService.hash(tokenPair.refreshToken()),
				tokenPair.refreshTokenExpiresAt()));
		return new AuthTokenResponse(
				user.getUserId(),
				user.getEmail(),
				user.getNickname(),
				tokenPair.accessToken(),
				tokenPair.refreshToken());
	}
}
