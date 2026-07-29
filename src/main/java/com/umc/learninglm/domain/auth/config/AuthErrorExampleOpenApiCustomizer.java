package com.umc.learninglm.domain.auth.config;

import com.umc.learninglm.global.error.ErrorCode;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.PathItem;
import io.swagger.v3.oas.models.examples.Example;
import io.swagger.v3.oas.models.media.Content;
import io.swagger.v3.oas.models.media.MediaType;
import io.swagger.v3.oas.models.responses.ApiResponse;
import org.springdoc.core.customizers.OpenApiCustomizer;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
public class AuthErrorExampleOpenApiCustomizer implements OpenApiCustomizer {

	private static final String API_PREFIX = "/api";
	private static final String APPLICATION_JSON = "application/json";

	@Override
	public void customise(OpenAPI openApi) {
		addPostExamples(openApi, "/auth/signup",
				ErrorCode.EMAIL_ALREADY_EXISTS,
				ErrorCode.REQUIRED_VALUE_MISSING,
				ErrorCode.TERMS_AGREEMENT_REQUIRED,
				ErrorCode.INVALID_PASSWORD_FORMAT,
				ErrorCode.INVALID_NICKNAME_FORMAT,
				ErrorCode.INVALID_EMAIL_FORMAT,
				ErrorCode.EMAIL_VERIFICATION_TOKEN_MISSING,
				ErrorCode.INVALID_EMAIL_VERIFICATION_TOKEN,
				ErrorCode.EMAIL_VERIFICATION_TOKEN_EXPIRED,
				ErrorCode.TOKEN_TYPE_MISMATCH,
				ErrorCode.TOKEN_PURPOSE_MISMATCH);

		addPostExamples(openApi, "/auth/login",
				ErrorCode.SOCIAL_ACCOUNT_LOCAL_LOGIN,
				ErrorCode.ACCOUNT_NOT_FOUND,
				ErrorCode.PASSWORD_MISMATCH);

		addPostExamples(openApi, "/auth/logout",
				ErrorCode.ACCESS_TOKEN_MISSING,
				ErrorCode.INVALID_ACCESS_TOKEN,
				ErrorCode.ALREADY_LOGGED_OUT_TOKEN,
				ErrorCode.REFRESH_TOKEN_MISSING,
				ErrorCode.INVALID_REFRESH_TOKEN,
				ErrorCode.SESSION_EXPIRED);

		addPostExamples(openApi, "/auth/reissue",
				ErrorCode.REFRESH_TOKEN_MISSING,
				ErrorCode.INVALID_REFRESH_TOKEN,
				ErrorCode.SESSION_EXPIRED);

		addGetExamples(openApi, "/auth/me",
				ErrorCode.ACCESS_TOKEN_MISSING,
				ErrorCode.INVALID_ACCESS_TOKEN,
				ErrorCode.ALREADY_LOGGED_OUT_TOKEN,
				ErrorCode.USER_NOT_FOUND);

		addPostExamples(openApi, "/auth/me/profile",
				ErrorCode.EMAIL_ALREADY_EXISTS,
				ErrorCode.INVALID_PASSWORD_FORMAT,
				ErrorCode.INVALID_NICKNAME_FORMAT,
				ErrorCode.INVALID_EMAIL_FORMAT,
				ErrorCode.NEW_PASSWORD_MISSING,
				ErrorCode.SAME_AS_CURRENT_PASSWORD,
				ErrorCode.NO_PROFILE_CHANGES,
				ErrorCode.SAME_AS_CURRENT_EMAIL,
				ErrorCode.CURRENT_PASSWORD_MISSING,
				ErrorCode.SOCIAL_PASSWORD_CHANGE_NOT_ALLOWED,
				ErrorCode.ACCESS_TOKEN_MISSING,
				ErrorCode.INVALID_ACCESS_TOKEN,
				ErrorCode.ALREADY_LOGGED_OUT_TOKEN,
				ErrorCode.EMAIL_VERIFICATION_TOKEN_MISSING,
				ErrorCode.INVALID_EMAIL_VERIFICATION_TOKEN,
				ErrorCode.EMAIL_VERIFICATION_TOKEN_EXPIRED,
				ErrorCode.CURRENT_PASSWORD_MISMATCH,
				ErrorCode.TOKEN_TYPE_MISMATCH,
				ErrorCode.TOKEN_PURPOSE_MISMATCH,
				ErrorCode.EMAIL_CHANGE_NOT_ALLOWED,
				ErrorCode.USER_NOT_FOUND);

		addPostExamples(openApi, "/auth/email/request",
				ErrorCode.INVALID_EMAIL_FORMAT,
				ErrorCode.VERIFICATION_TYPE_MISSING,
				ErrorCode.VERIFICATION_TYPE_INVALID,
				ErrorCode.VERIFICATION_PURPOSE_MISSING,
				ErrorCode.VERIFICATION_PURPOSE_INVALID,
				ErrorCode.EMAIL_REQUIRED,
				ErrorCode.ACCESS_TOKEN_MISSING,
				ErrorCode.INVALID_ACCESS_TOKEN,
				ErrorCode.ALREADY_LOGGED_OUT_TOKEN,
				ErrorCode.USER_NOT_FOUND,
				ErrorCode.EMAIL_CODE_SEND_LIMIT_EXCEEDED);

		addPostExamples(openApi, "/auth/email/verify",
				ErrorCode.INVALID_EMAIL_FORMAT,
				ErrorCode.VERIFICATION_TYPE_MISSING,
				ErrorCode.VERIFICATION_TYPE_INVALID,
				ErrorCode.VERIFICATION_PURPOSE_MISSING,
				ErrorCode.VERIFICATION_PURPOSE_INVALID,
				ErrorCode.EMAIL_REQUIRED,
				ErrorCode.EMAIL_CODE_MISSING,
				ErrorCode.EMAIL_CODE_MISMATCH,
				ErrorCode.EMAIL_CODE_EXPIRED,
				ErrorCode.ACCESS_TOKEN_MISSING,
				ErrorCode.INVALID_ACCESS_TOKEN,
				ErrorCode.ALREADY_LOGGED_OUT_TOKEN,
				ErrorCode.EMAIL_CODE_ATTEMPT_LIMIT_EXCEEDED);

		addPostExamples(openApi, "/auth/password",
				ErrorCode.INVALID_PASSWORD_FORMAT,
				ErrorCode.NEW_PASSWORD_MISSING,
				ErrorCode.SAME_AS_CURRENT_PASSWORD,
				ErrorCode.SOCIAL_PASSWORD_CHANGE_NOT_ALLOWED,
				ErrorCode.EMAIL_VERIFICATION_TOKEN_MISSING,
				ErrorCode.INVALID_EMAIL_VERIFICATION_TOKEN,
				ErrorCode.EMAIL_VERIFICATION_TOKEN_EXPIRED,
				ErrorCode.TOKEN_TYPE_MISMATCH,
				ErrorCode.TOKEN_PURPOSE_MISMATCH,
				ErrorCode.USER_NOT_FOUND);
	}

	private void addPostExamples(OpenAPI openApi, String path, ErrorCode... errorCodes) {
		addExamples(openApi, path, PathItem::getPost, errorCodes);
	}

	private void addGetExamples(OpenAPI openApi, String path, ErrorCode... errorCodes) {
		addExamples(openApi, path, PathItem::getGet, errorCodes);
	}

	private void addExamples(
			OpenAPI openApi,
			String path,
			Function<PathItem, Operation> operationResolver,
			ErrorCode... errorCodes) {
		PathItem pathItem = resolvePathItem(openApi, path);
		if (pathItem == null) {
			return;
		}

		Operation operation = operationResolver.apply(pathItem);
		if (operation == null || operation.getResponses() == null) {
			return;
		}

		Map<String, List<ErrorCode>> errorsByStatus = Arrays.stream(errorCodes)
				.collect(Collectors.groupingBy(
						errorCode -> String.valueOf(errorCode.getHttpStatus().value()),
						LinkedHashMap::new,
						Collectors.toList()));

		errorsByStatus.forEach((status, errors) -> addStatusExamples(operation, status, errors));
	}

	private PathItem resolvePathItem(OpenAPI openApi, String path) {
		if (openApi.getPaths() == null) {
			return null;
		}
		PathItem pathItem = openApi.getPaths().get(path);
		return pathItem != null ? pathItem : openApi.getPaths().get(API_PREFIX + path);
	}

	private void addStatusExamples(Operation operation, String status, List<ErrorCode> errors) {
		ApiResponse apiResponse = operation.getResponses().get(status);
		if (apiResponse == null) {
			apiResponse = new ApiResponse().description("오류 응답");
			operation.getResponses().addApiResponse(status, apiResponse);
		}

		MediaType mediaType = new MediaType();
		for (ErrorCode errorCode : errors) {
			mediaType.addExamples(errorCode.getCode(), createExample(errorCode));
		}
		apiResponse.setContent(new Content().addMediaType(APPLICATION_JSON, mediaType));
	}

	private Example createExample(ErrorCode errorCode) {
		Map<String, Object> response = new LinkedHashMap<>();
		response.put("success", false);
		response.put("code", errorCode.getCode());
		response.put("message", errorCode.getMessage());

		return new Example()
				.summary(errorCode.getCode() + ": " + errorCode.getMessage())
				.value(response);
	}
}
