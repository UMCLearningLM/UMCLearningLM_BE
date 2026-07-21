package com.umc.learninglm.global.security.oauth.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.umc.learninglm.global.common.BaseResponse;
import com.umc.learninglm.global.error.ErrorCode;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2ErrorCodes;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationFailureHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

@Component
public class OAuth2FailureHandler extends SimpleUrlAuthenticationFailureHandler {

	private final ObjectMapper objectMapper;

	public OAuth2FailureHandler(ObjectMapper objectMapper) {
		this.objectMapper = objectMapper;
	}

	@Override
	public void onAuthenticationFailure(
			HttpServletRequest request,
			HttpServletResponse response,
			AuthenticationException exception) throws IOException, ServletException {
		invalidateSession(request);
		writeFailure(response, resolveErrorCode(exception));
	}

	public void writeFailure(HttpServletResponse response, ErrorCode errorCode) throws IOException {
		response.setStatus(errorCode.getHttpStatus().value());
		response.setContentType(MediaType.APPLICATION_JSON_VALUE);
		response.setCharacterEncoding(StandardCharsets.UTF_8.name());
		objectMapper.writeValue(
				response.getWriter(),
				BaseResponse.failure(errorCode.getCode(), errorCode.getMessage()));
	}

	private void invalidateSession(HttpServletRequest request) {
		HttpSession session = request.getSession(false);
		if (session != null) {
			session.invalidate();
		}
	}

	private ErrorCode resolveErrorCode(AuthenticationException exception) {
		if (!(exception instanceof OAuth2AuthenticationException oAuth2Exception)) {
			return ErrorCode.OAUTH_AUTHENTICATION_FAILED;
		}

		return switch (oAuth2Exception.getError().getErrorCode()) {
			case OAuth2ErrorCodes.ACCESS_DENIED -> ErrorCode.OAUTH_AUTHENTICATION_FAILED;
			case "authorization_request_not_found", "invalid_state_parameter" -> ErrorCode.OAUTH_STATE_INVALID;
			case "invalid_token_response" -> ErrorCode.GOOGLE_TOKEN_ISSUE_FAILED;
			case "invalid_user_info_response" -> ErrorCode.GOOGLE_USER_INFO_FAILED;
			default -> ErrorCode.OAUTH_AUTHENTICATION_FAILED;
		};
	}
}
