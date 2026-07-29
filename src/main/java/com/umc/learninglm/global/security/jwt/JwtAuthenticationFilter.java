package com.umc.learninglm.global.security.jwt;

import com.umc.learninglm.domain.auth.enums.TokenStatus;
import com.umc.learninglm.domain.auth.enums.TokenType;
import com.umc.learninglm.domain.auth.repository.TokenCodeRepository;
import com.umc.learninglm.domain.auth.service.TokenHashService;
import com.umc.learninglm.global.error.CustomException;
import com.umc.learninglm.global.error.ErrorCode;
import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;
import java.util.Set;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

	private static final String AUTHORIZATION_HEADER = "Authorization";
	private static final String BEARER_PREFIX = "Bearer ";
	private static final String SIGNUP_PATH = "/api/auth/signup";
	private static final String LOGIN_PATH = "/api/auth/login";
	private static final String REISSUE_PATH = "/api/auth/reissue";
	private static final String PASSWORD_RESET_PATH = "/api/auth/password";
	private static final Set<String> ACCESS_TOKEN_FREE_PATHS = Set.of(
			SIGNUP_PATH,
			LOGIN_PATH,
			REISSUE_PATH,
			PASSWORD_RESET_PATH);

	private final JwtProvider jwtProvider;
	private final TokenCodeRepository tokenCodeRepository;
	private final TokenHashService tokenHashService;
	private final JwtAuthenticationEntryPoint authenticationEntryPoint;

	public JwtAuthenticationFilter(
			JwtProvider jwtProvider,
			TokenCodeRepository tokenCodeRepository,
			TokenHashService tokenHashService,
			JwtAuthenticationEntryPoint authenticationEntryPoint) {
		this.jwtProvider = jwtProvider;
		this.tokenCodeRepository = tokenCodeRepository;
		this.tokenHashService = tokenHashService;
		this.authenticationEntryPoint = authenticationEntryPoint;
	}

	@Override
	protected boolean shouldNotFilter(HttpServletRequest request) {
		return ACCESS_TOKEN_FREE_PATHS.contains(request.getRequestURI());
	}

	@Override
	protected void doFilterInternal(
			HttpServletRequest request,
			HttpServletResponse response,
			FilterChain filterChain) throws ServletException, IOException {
		String token = resolveBearerToken(request);
		if (token == null) {
			filterChain.doFilter(request, response);
			return;
		}

		try {
			Claims claims = jwtProvider.parseAccessToken(token);
			validateNotBlacklisted(token);
			String email = jwtProvider.getEmail(claims);
			String authority = "ROLE_" + jwtProvider.getAuthority(claims).name();
			UsernamePasswordAuthenticationToken authentication =
					new UsernamePasswordAuthenticationToken(
							email,
							null,
							List.of(new SimpleGrantedAuthority(authority)));
			SecurityContextHolder.getContext().setAuthentication(authentication);
			filterChain.doFilter(request, response);
		} catch (CustomException e) {
			SecurityContextHolder.clearContext();
			authenticationEntryPoint.writeError(response, e.getErrorCode());
		}
	}

	private String resolveBearerToken(HttpServletRequest request) {
		String authorization = request.getHeader(AUTHORIZATION_HEADER);
		if (authorization == null || !authorization.startsWith(BEARER_PREFIX)) {
			return null;
		}
		String token = authorization.substring(BEARER_PREFIX.length());
		return token.isBlank() ? null : token;
	}

	private void validateNotBlacklisted(String token) {
		boolean blacklisted = tokenCodeRepository.existsByTokenHashAndTypeAndStatus(
				tokenHashService.hash(token),
				TokenType.ACCESS_BLACKLIST,
				TokenStatus.ACTIVE);
		if (blacklisted) {
			throw new CustomException(ErrorCode.ALREADY_LOGGED_OUT_TOKEN);
		}
	}
}
