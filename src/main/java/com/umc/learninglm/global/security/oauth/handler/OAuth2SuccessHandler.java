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

@Component
public class OAuth2SuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

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
			OAuth2AuthenticationToken oAuth2Token = (OAuth2AuthenticationToken) authentication;
			OidcUser oidcUser = (OidcUser) oAuth2Token.getPrincipal();
			UserProvider provider = UserProvider.valueOf(
					oAuth2Token.getAuthorizedClientRegistrationId().toUpperCase(Locale.ROOT));

			AuthTokenResponse tokenResponse = socialLoginService.login(
					provider,
					oidcUser.getSubject(),
					oidcUser.getEmail(),
					oidcUser.getFullName());

			clearAuthenticationAttributes(request);
			invalidateSession(request);
			writeSuccess(response, tokenResponse);
		} catch (CustomException e) {
			invalidateSession(request);
			oAuth2FailureHandler.writeFailure(response, e.getErrorCode());
		} catch (RuntimeException e) {
			invalidateSession(request);
			oAuth2FailureHandler.writeFailure(response, ErrorCode.SOCIAL_ACCOUNT_PROCESSING_FAILED);
		}
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
