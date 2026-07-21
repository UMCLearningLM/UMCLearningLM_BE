package com.umc.learninglm.domain.auth.controller;

import com.umc.learninglm.domain.auth.config.AuthErrorExampleOpenApiCustomizer;
import com.umc.learninglm.domain.auth.service.AuthService;
import com.umc.learninglm.global.config.CorsConfig;
import com.umc.learninglm.global.config.SecurityConfig;
import com.umc.learninglm.global.config.SwaggerConfig;
import com.umc.learninglm.global.error.GlobalExceptionHandler;
import com.umc.learninglm.global.security.oauth.handler.OAuth2FailureHandler;
import com.umc.learninglm.global.security.oauth.handler.OAuth2SuccessHandler;
import com.umc.learninglm.global.security.jwt.JwtAuthenticationEntryPoint;
import com.umc.learninglm.global.security.jwt.JwtAuthenticationFilter;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.options;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;

@SpringBootTest(
        classes = AuthOpenApiTest.TestApplication.class,
        properties = {
                "GOOGLE_CLIENT_ID=test-client-id",
                "GOOGLE_CLIENT_SECRET=test-client-secret",
                "GOOGLE_REDIRECT_URI=http://localhost:8080/api/auth/google/callback"
        })
@AutoConfigureMockMvc
class AuthOpenApiTest {

    private final MockMvc mockMvc;

    @MockitoBean
    private OAuth2SuccessHandler oAuth2SuccessHandler;

    @MockitoBean
    private OAuth2FailureHandler oAuth2FailureHandler;

    @MockitoBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @MockitoBean
    private JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;

    @MockitoBean
    private AuthService authService;

    @Autowired
    AuthOpenApiTest(MockMvc mockMvc) {
        this.mockMvc = mockMvc;
    }

    @BeforeEach
    void setUpJwtFilter() throws Exception {
        doAnswer(invocation -> {
            jakarta.servlet.FilterChain filterChain = invocation.getArgument(2);
            filterChain.doFilter(invocation.getArgument(0), invocation.getArgument(1));
            return null;
        }).when(jwtAuthenticationFilter).doFilter(any(), any(), any());
    }

    @Test
	void exposesAuthEndpointsWithoutApiPrefix() throws Exception {
        mockMvc.perform(get("/v3/api-docs"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.servers[0].url").value("/api"))
				.andExpect(jsonPath("$.components.securitySchemes.bearerAuth").exists())
				.andExpect(jsonPath("$.components.securitySchemes.emailVerificationToken").exists())
				.andExpect(jsonPath("$.components.securitySchemes.emailVerificationToken.name")
						.value("X-Email-Verification-Token"))
				.andExpect(jsonPath("$.paths.length()").value(10))
                .andExpect(jsonPath("$.paths['/auth/signup'].post").exists())
				.andExpect(jsonPath("$.paths['/auth/signup'].post.security[0].emailVerificationToken").exists())
				.andExpect(jsonPath("$.paths['/auth/signup'].post.responses['400'].content['application/json'].examples.AUTH40001.value.success")
						.value(false))
				.andExpect(jsonPath("$.paths['/auth/signup'].post.responses['400'].content['application/json'].examples.AUTH40001.value.code")
						.value("AUTH40001"))
				.andExpect(jsonPath("$.paths['/auth/signup'].post.responses['400'].content['application/json'].examples.AUTH40001.value.message")
						.value("이미 가입된 이메일입니다."))
                .andExpect(jsonPath("$.paths['/auth/login'].post").exists())
                .andExpect(jsonPath("$.paths['/auth/logout'].post").exists())
				.andExpect(jsonPath("$.paths['/auth/logout'].post.parameters").doesNotExist())
                .andExpect(jsonPath("$.paths['/auth/google'].get").exists())
                .andExpect(jsonPath("$.paths['/auth/reissue'].post").exists())
				.andExpect(jsonPath("$.paths['/auth/me'].get").exists())
				.andExpect(jsonPath("$.paths['/auth/me/profile'].post").exists())
				.andExpect(jsonPath("$.paths['/auth/me/profile'].post.parameters").doesNotExist())
				.andExpect(jsonPath("$.paths['/auth/me/profile'].post.security.length()").value(2))
				.andExpect(jsonPath("$.paths['/auth/me/profile'].post.security[0].bearerAuth").exists())
				.andExpect(jsonPath("$.paths['/auth/me/profile'].post.security[0].emailVerificationToken").exists())
				.andExpect(jsonPath("$.paths['/auth/me/profile'].post.security[1].bearerAuth").exists())
                .andExpect(jsonPath("$.paths['/auth/email/request'].post").exists())
				.andExpect(jsonPath("$.paths['/auth/email/request'].post.security.length()").value(2))
				.andExpect(jsonPath("$.paths['/auth/email/request'].post.security[1].bearerAuth").exists())
                .andExpect(jsonPath("$.paths['/auth/email/verify'].post").exists())
				.andExpect(jsonPath("$.paths['/auth/email/verify'].post.security.length()").value(2))
				.andExpect(jsonPath("$.paths['/auth/email/verify'].post.security[1].bearerAuth").exists())
				.andExpect(jsonPath("$.paths['/auth/password'].post").exists())
				.andExpect(jsonPath("$.paths['/auth/password'].post.security[0].emailVerificationToken").exists());
    }

	@Test
	void allowsCorsPreflightForProtectedAuthEndpoint() throws Exception {
		mockMvc.perform(options("/api/auth/me")
					.header(HttpHeaders.ORIGIN, "http://localhost:5173")
					.header(HttpHeaders.ACCESS_CONTROL_REQUEST_METHOD, "GET"))
				.andExpect(status().isOk())
				.andExpect(header().string(
						HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN,
						"http://localhost:5173"));
	}

	@Test
	void reissueReturnsRefreshTokenMissingErrorForBlankToken() throws Exception {
		mockMvc.perform(post("/api/auth/reissue")
					.contentType(MediaType.APPLICATION_JSON)
					.content("""
							{
							  "refreshToken": ""
							}
							"""))
				.andExpect(status().isUnauthorized())
				.andExpect(jsonPath("$.code").value("AUTH40108"))
				.andExpect(jsonPath("$.message").value("리프레시 토큰이 필요합니다."));
	}

    @Test
    void signupReturnsDomainErrorCodeForInvalidEmail() throws Exception {
        mockMvc.perform(post("/api/auth/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "email": "invalid-email",
                                  "password": "Password123!",
                                  "nickname": "홍길동",
                                  "termsAgreed": true
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("AUTH40006"));
    }

    @Test
    void signupReturnsRequiredValueCodeForMissingEmail() throws Exception {
        mockMvc.perform(post("/api/auth/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "password": "Password123!",
                                  "nickname": "홍길동",
                                  "termsAgreed": true
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("AUTH40002"));
    }

    @Test
    void signupReturnsPasswordFormatCode() throws Exception {
        mockMvc.perform(post("/api/auth/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "email": "user@example.com",
                                  "password": "password",
                                  "nickname": "홍길동",
                                  "termsAgreed": true
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("AUTH40004"));
    }

    @Test
	void emailVerificationReturnsDomainErrorCodeForInvalidVerificationType() throws Exception {
		mockMvc.perform(post("/api/auth/email/request")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "verificationType": "INVALID",
                                  "purpose": "SIGNUP",
                                  "email": "user@example.com"
                                }
                                """))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.code").value("AUTH40010"));
	}

	@Test
	void loginEmailVerificationRequestRequiresAccessTokenHeader() throws Exception {
		mockMvc.perform(post("/api/auth/email/request")
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{
								  "verificationType": "LOGIN",
								  "purpose": "EMAIL_CHANGE",
								  "email": "new@example.com"
								}
								"""))
				.andExpect(status().isUnauthorized())
				.andExpect(jsonPath("$.code").value("AUTH40103"));
	}

	@Test
	void loginEmailVerificationVerifyRequiresAccessTokenHeader() throws Exception {
		mockMvc.perform(post("/api/auth/email/verify")
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{
								  "verificationType": "LOGIN",
								  "purpose": "EMAIL_CHANGE",
								  "email": "new@example.com",
								  "code": "A1b2C3d4E5"
								}
								"""))
				.andExpect(status().isUnauthorized())
				.andExpect(jsonPath("$.code").value("AUTH40103"));
	}

    @SpringBootConfiguration
    @EnableAutoConfiguration(exclude = {
            DataSourceAutoConfiguration.class,
            HibernateJpaAutoConfiguration.class
    })
	@Import({
			AuthController.class,
			AuthErrorExampleOpenApiCustomizer.class,
			CorsConfig.class,
            SwaggerConfig.class,
            SecurityConfig.class,
            GlobalExceptionHandler.class
    })
    static class TestApplication {
    }
}
