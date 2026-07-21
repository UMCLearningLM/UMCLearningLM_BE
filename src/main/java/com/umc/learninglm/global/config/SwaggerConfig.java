package com.umc.learninglm.global.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Paths;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import org.springdoc.core.customizers.OpenApiCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class SwaggerConfig {

	private static final String BEARER_AUTH = "bearerAuth";
	private static final String EMAIL_VERIFICATION_TOKEN = "emailVerificationToken";
	private static final String PROFILE_UPDATE_PATH = "/auth/me/profile";
	private static final String EMAIL_REQUEST_PATH = "/auth/email/request";
	private static final String EMAIL_VERIFY_PATH = "/auth/email/verify";

	@Bean
	public OpenAPI openAPI() {
		return new OpenAPI()
				.components(new Components()
						.addSecuritySchemes(BEARER_AUTH, new SecurityScheme()
								.type(SecurityScheme.Type.HTTP)
								.scheme("bearer")
								.bearerFormat("JWT")
								.description("일반 로그인 Access Token"))
						.addSecuritySchemes(EMAIL_VERIFICATION_TOKEN, new SecurityScheme()
								.type(SecurityScheme.Type.APIKEY)
								.in(SecurityScheme.In.HEADER)
								.name("X-Email-Verification-Token")
								.description("SIGNUP, PASSWORD_RESET, EMAIL_CHANGE 목적의 임시 이메일 인증 토큰. Bearer 접두사 없이 입력합니다.")))
				.servers(List.of(new Server()
						.url("/api")
						.description("API Base URL")))
				.info(new Info()
						.title("LearningLM API")
						.description("LearningLM 백엔드 API 명세")
						.version("v0.0.1"));
	}

	@Bean
	public OpenApiCustomizer apiPrefixCustomizer() {
		return openApi -> {
			Paths pathsWithoutApiPrefix = new Paths();
			openApi.getPaths().forEach((path, pathItem) -> {
				String documentedPath = path.startsWith("/api/") ? path.substring(4) : path;
				pathsWithoutApiPrefix.addPathItem(documentedPath, pathItem);
			});
			openApi.setPaths(pathsWithoutApiPrefix);
			setProfileSecurity(pathsWithoutApiPrefix);
			setOptionalBearerSecurity(pathsWithoutApiPrefix, EMAIL_REQUEST_PATH);
			setOptionalBearerSecurity(pathsWithoutApiPrefix, EMAIL_VERIFY_PATH);
		};
	}

	private void setProfileSecurity(Paths paths) {
		if (paths.get(PROFILE_UPDATE_PATH) == null || paths.get(PROFILE_UPDATE_PATH).getPost() == null) {
			return;
		}
		paths.get(PROFILE_UPDATE_PATH).getPost().setSecurity(List.of(
				new SecurityRequirement()
						.addList(BEARER_AUTH)
						.addList(EMAIL_VERIFICATION_TOKEN),
				new SecurityRequirement().addList(BEARER_AUTH)));
	}

	private void setOptionalBearerSecurity(Paths paths, String path) {
		if (paths.get(path) == null || paths.get(path).getPost() == null) {
			return;
		}
		paths.get(path).getPost().setSecurity(List.of(
				new SecurityRequirement(),
				new SecurityRequirement().addList(BEARER_AUTH)));
	}
}
