package com.umc.learninglm.global.config;

import com.umc.learninglm.global.security.oauth.handler.OAuth2FailureHandler;
import com.umc.learninglm.global.security.oauth.handler.OAuth2SuccessHandler;
import com.umc.learninglm.global.security.jwt.JwtAuthenticationEntryPoint;
import com.umc.learninglm.global.security.jwt.JwtAuthenticationFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

	private static final String[] PUBLIC_PATHS = {
			"/api/auth/oauth2/**",
			"/api/auth/google/**",
			"/api/ai/**",
			"/swagger-ui/**",
			"/v3/api-docs/**"
	};
	private final OAuth2SuccessHandler oAuth2SuccessHandler;
	private final OAuth2FailureHandler oAuth2FailureHandler;
	private final JwtAuthenticationFilter jwtAuthenticationFilter;
	private final JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;

	public SecurityConfig(
			OAuth2SuccessHandler oAuth2SuccessHandler,
			OAuth2FailureHandler oAuth2FailureHandler,
			JwtAuthenticationFilter jwtAuthenticationFilter,
			JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint) {
		this.oAuth2SuccessHandler = oAuth2SuccessHandler;
		this.oAuth2FailureHandler = oAuth2FailureHandler;
		this.jwtAuthenticationFilter = jwtAuthenticationFilter;
		this.jwtAuthenticationEntryPoint = jwtAuthenticationEntryPoint;
	}

	@Bean
	public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
		http
				.cors(Customizer.withDefaults())
				.csrf(csrf -> csrf.disable())
				.sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED))
				.formLogin(form -> form.disable())
				.httpBasic(basic -> basic.disable())
				.exceptionHandling(exception -> exception
						.authenticationEntryPoint(jwtAuthenticationEntryPoint))
				.authorizeHttpRequests(auth -> auth
						.requestMatchers(PUBLIC_PATHS).permitAll()
						.requestMatchers(HttpMethod.POST,
								"/api/auth/signup",
								"/api/auth/login",
								"/api/auth/reissue",
								"/api/auth/password",
								"/api/auth/email/request",
								"/api/auth/email/verify").permitAll()
						.anyRequest().authenticated()
				)
				.oauth2Login(oauth2 -> oauth2
						.authorizationEndpoint(endpoint -> endpoint
								.baseUri("/api/auth/oauth2/authorization"))
						.redirectionEndpoint(endpoint -> endpoint
								.baseUri("/api/auth/google/callback"))
						.successHandler(oAuth2SuccessHandler)
						.failureHandler(oAuth2FailureHandler)
				)
				.addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);
		return http.build();
	}

	@Bean
	public PasswordEncoder passwordEncoder() {
		return new BCryptPasswordEncoder();
	}
}
