package com.umc.learninglm.global.security.jwt;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.umc.learninglm.domain.auth.enums.TokenStatus;
import com.umc.learninglm.domain.auth.enums.TokenType;
import com.umc.learninglm.domain.auth.enums.UserRole;
import com.umc.learninglm.domain.auth.repository.TokenCodeRepository;
import com.umc.learninglm.domain.auth.service.TokenHashService;
import io.jsonwebtoken.Claims;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.context.SecurityContextHolder;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class JwtAuthenticationFilterTest {

	@Mock
	private JwtProvider jwtProvider;

	@Mock
	private TokenCodeRepository tokenCodeRepository;

	@Mock
	private TokenHashService tokenHashService;

	@Mock
	private Claims claims;

	private ObjectMapper objectMapper;
	private JwtAuthenticationFilter filter;

	@BeforeEach
	void setUp() {
		objectMapper = new ObjectMapper();
		filter = new JwtAuthenticationFilter(
				jwtProvider,
				tokenCodeRepository,
				tokenHashService,
				new JwtAuthenticationEntryPoint(objectMapper));
	}

	@AfterEach
	void tearDown() {
		SecurityContextHolder.clearContext();
	}

	@Test
	void authenticatesValidAccessToken() throws Exception {
		MockHttpServletRequest request = bearerRequest();
		MockHttpServletResponse response = new MockHttpServletResponse();
		MockFilterChain filterChain = new MockFilterChain();
		when(jwtProvider.parseAccessToken("access-token")).thenReturn(claims);
		when(tokenHashService.hash("access-token")).thenReturn("access-hash");
		when(tokenCodeRepository.existsByTokenHashAndTypeAndStatus(
				"access-hash", TokenType.ACCESS_BLACKLIST, TokenStatus.ACTIVE))
				.thenReturn(false);
		when(jwtProvider.getEmail(claims)).thenReturn("user@example.com");
		when(jwtProvider.getAuthority(claims)).thenReturn(UserRole.USER);

		filter.doFilter(request, response, filterChain);

		assertThat(filterChain.getRequest()).isNotNull();
		assertThat(SecurityContextHolder.getContext().getAuthentication().getName())
				.isEqualTo("user@example.com");
		assertThat(SecurityContextHolder.getContext().getAuthentication().getAuthorities())
				.extracting("authority")
				.containsExactly("ROLE_USER");
	}

	@Test
	void rejectsBlacklistedAccessToken() throws Exception {
		MockHttpServletRequest request = bearerRequest();
		MockHttpServletResponse response = new MockHttpServletResponse();
		MockFilterChain filterChain = new MockFilterChain();
		when(jwtProvider.parseAccessToken("access-token")).thenReturn(claims);
		when(tokenHashService.hash("access-token")).thenReturn("access-hash");
		when(tokenCodeRepository.existsByTokenHashAndTypeAndStatus(
				"access-hash", TokenType.ACCESS_BLACKLIST, TokenStatus.ACTIVE))
				.thenReturn(true);

		filter.doFilter(request, response, filterChain);

		JsonNode responseBody = objectMapper.readTree(response.getContentAsString());
		assertThat(response.getStatus()).isEqualTo(401);
		assertThat(responseBody.path("code").asText()).isEqualTo("AUTH40105");
		assertThat(filterChain.getRequest()).isNull();
	}

	private MockHttpServletRequest bearerRequest() {
		MockHttpServletRequest request = new MockHttpServletRequest();
		request.addHeader("Authorization", "Bearer access-token");
		return request;
	}
}
