package com.umc.learninglm.global.security.oauth.handler;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.umc.learninglm.domain.auth.dto.response.AuthTokenResponse;
import com.umc.learninglm.domain.auth.enums.UserProvider;
import com.umc.learninglm.domain.auth.service.SocialLoginService;
import com.umc.learninglm.global.error.CustomException;
import com.umc.learninglm.global.error.ErrorCode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OAuth2HandlerTest {

	@Mock
	private SocialLoginService socialLoginService;

	@Mock
	private OidcUser oidcUser;

	private ObjectMapper objectMapper;
	private OAuth2SuccessHandler successHandler;

	@BeforeEach
	void setUp() {
		objectMapper = new ObjectMapper();
		OAuth2FailureHandler failureHandler = new OAuth2FailureHandler(objectMapper);
		successHandler = new OAuth2SuccessHandler(socialLoginService, failureHandler, objectMapper);
	}

	@Test
	void successHandlerDelegatesGoogleUserToSocialLoginService() throws Exception {
		MockHttpServletRequest request = new MockHttpServletRequest();
		MockHttpSession session = (MockHttpSession) request.getSession();
		MockHttpServletResponse response = new MockHttpServletResponse();
		OAuth2AuthenticationToken authentication = googleAuthentication();
		AuthTokenResponse tokenResponse = new AuthTokenResponse(
				1L, "user@example.com", "홍길동", "액세스 토큰", "리프레시 토큰");
		when(socialLoginService.login(
				UserProvider.GOOGLE,
				"google-sub-123",
				"user@example.com",
				"홍길동"))
				.thenReturn(tokenResponse);

		successHandler.onAuthenticationSuccess(request, response, authentication);

		JsonNode responseBody = objectMapper.readTree(response.getContentAsString());
		assertThat(response.getStatus()).isEqualTo(200);
		assertThat(responseBody.path("code").asText()).isEqualTo("COMMON200");
		assertThat(responseBody.path("result").path("accessToken").asText()).isEqualTo("액세스 토큰");
		assertThat(session.isInvalid()).isTrue();
		verify(socialLoginService).login(
				UserProvider.GOOGLE,
				"google-sub-123",
				"user@example.com",
				"홍길동");
	}

	@Test
	void successHandlerWritesDomainErrorWhenSocialLoginFails() throws Exception {
		MockHttpServletRequest request = new MockHttpServletRequest();
		request.getSession();
		MockHttpServletResponse response = new MockHttpServletResponse();
		OAuth2AuthenticationToken authentication = googleAuthentication();
		when(socialLoginService.login(
				UserProvider.GOOGLE,
				"google-sub-123",
				"user@example.com",
				"홍길동"))
				.thenThrow(new CustomException(ErrorCode.EMAIL_ALREADY_EXISTS));

		successHandler.onAuthenticationSuccess(request, response, authentication);

		JsonNode responseBody = objectMapper.readTree(response.getContentAsString());
		assertThat(response.getStatus()).isEqualTo(400);
		assertThat(responseBody.path("code").asText()).isEqualTo("AUTH40001");
	}

	@Test
	void failureHandlerMapsInvalidStateError() throws Exception {
		MockHttpServletRequest request = new MockHttpServletRequest();
		request.getSession();
		MockHttpServletResponse response = new MockHttpServletResponse();
		OAuth2FailureHandler failureHandler = new OAuth2FailureHandler(objectMapper);
		OAuth2AuthenticationException exception = new OAuth2AuthenticationException(
				new OAuth2Error("invalid_state_parameter"));

		failureHandler.onAuthenticationFailure(request, response, exception);

		JsonNode responseBody = objectMapper.readTree(response.getContentAsString());
		assertThat(response.getStatus()).isEqualTo(401);
		assertThat(responseBody.path("code").asText()).isEqualTo("AUTH40107");
	}

	private OAuth2AuthenticationToken googleAuthentication() {
		when(oidcUser.getSubject()).thenReturn("google-sub-123");
		when(oidcUser.getEmail()).thenReturn("user@example.com");
		when(oidcUser.getFullName()).thenReturn("홍길동");
		return new OAuth2AuthenticationToken(oidcUser, List.of(), "google");
	}
}
