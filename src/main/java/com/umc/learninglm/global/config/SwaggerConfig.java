package com.umc.learninglm.global.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Paths;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import org.springdoc.core.customizers.OpenApiCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class SwaggerConfig {

	@Bean
	public OpenAPI openAPI() {
		return new OpenAPI()
				.components(new Components()
						.addSecuritySchemes("bearerAuth", new SecurityScheme()
								.type(SecurityScheme.Type.HTTP)
								.scheme("bearer")
								.bearerFormat("JWT")))
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
		};
	}
}
