package com.umc.learninglm.global.security.oauth.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.umc.learninglm.domain.auth.dto.response.AuthTokenResponse;
import com.umc.learninglm.domain.auth.enums.UserProvider;
import com.umc.learninglm.domain.auth.service.SocialLoginService;
import com.umc.learninglm.global.common.BaseResponse;
import com.umc.learninglm.global.error.CustomException;
import com.umc.learninglm.global.error.ErrorCode;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Locale;
import java.util.Map;

@Component
@Slf4j
public class OAuth2SuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

	private static final Map<String, UserProvider> SUPPORTED_PROVIDERS = Map.of(
			"google", UserProvider.GOOGLE);

	private final SocialLoginService socialLoginService;
	private final OAuth2FailureHandler oAuth2FailureHandler;
	private final ObjectMapper objectMapper;

	public OAuth2SuccessHandler(
			SocialLoginService socialLoginService,
			OAuth2FailureHandler oAuth2FailureHandler,
			ObjectMapper objectMapper) {
		this.socialLoginService = socialLoginService;
		this.oAuth2FailureHandler = oAuth2FailureHandler;
		this.objectMapper = objectMapper;
	}

	@Override
	public void onAuthenticationSuccess(
			HttpServletRequest request,
			HttpServletResponse response,
			Authentication authentication) throws IOException, ServletException {
		try {
			OAuth2AuthenticationToken oAuth2Token = resolveOAuth2Token(authentication);
			OidcUser oidcUser = resolveOidcUser(oAuth2Token);
			UserProvider provider = resolveProvider(oAuth2Token.getAuthorizedClientRegistrationId());

			AuthTokenResponse tokenResponse = socialLoginService.login(
					provider,
					oidcUser.getSubject(),
					oidcUser.getEmail(),
					oidcUser.getFullName());

			clearAuthenticationAttributes(request);
			invalidateSession(request);
			writeSuccess(response, tokenResponse);
		} catch (CustomException e) {
			log.warn("Social login failed: {}", e.getErrorCode(), e);
			invalidateSession(request);
			oAuth2FailureHandler.writeFailure(response, e.getErrorCode());
		} catch (RuntimeException e) {
			log.error("Unexpected error during OAuth2 login success handling", e);
			invalidateSession(request);
			oAuth2FailureHandler.writeFailure(response, ErrorCode.SOCIAL_ACCOUNT_PROCESSING_FAILED);
		}
	}

	private OAuth2AuthenticationToken resolveOAuth2Token(Authentication authentication) {
		if (authentication instanceof OAuth2AuthenticationToken oAuth2Token) {
			return oAuth2Token;
		}
		throw new CustomException(ErrorCode.OAUTH_AUTHENTICATION_FAILED);
	}

	private OidcUser resolveOidcUser(OAuth2AuthenticationToken oAuth2Token) {
		if (oAuth2Token.getPrincipal() instanceof OidcUser oidcUser) {
			return oidcUser;
		}
		throw new CustomException(ErrorCode.OAUTH_AUTHENTICATION_FAILED);
	}

	private UserProvider resolveProvider(String registrationId) {
		String providerKey = registrationId == null
				? ""
				: registrationId.toLowerCase(Locale.ROOT);
		UserProvider provider = SUPPORTED_PROVIDERS.get(providerKey);
		if (provider == null) {
			throw new CustomException(ErrorCode.SOCIAL_ACCOUNT_PROCESSING_FAILED);
		}
		return provider;
	}

	private void writeSuccess(HttpServletResponse response, AuthTokenResponse tokenResponse) throws IOException {
		response.setStatus(HttpStatus.OK.value());
		response.setContentType(MediaType.APPLICATION_JSON_VALUE);
		response.setCharacterEncoding(StandardCharsets.UTF_8.name());
		objectMapper.writeValue(response.getWriter(), BaseResponse.success(tokenResponse));
	}

	private void invalidateSession(HttpServletRequest request) {
		HttpSession session = request.getSession(false);
		if (session != null) {
			session.invalidate();
		}
	}
}
