package com.umc.learninglm.global.common;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class BaseResponse<T> {

	private final boolean isSuccess;
	private final String code;
	private final String message;
	private final T result;

	private BaseResponse(boolean isSuccess, String code, String message, T result) {
		this.isSuccess = isSuccess;
		this.code = code;
		this.message = message;
		this.result = result;
	}

	public static <T> BaseResponse<T> success(T result) {
		return new BaseResponse<>(true, "COMMON200", "성공입니다.", result);
	}

	public static <T> BaseResponse<T> success(String message, T result) {
		return new BaseResponse<>(true, "COMMON200", message, result);
	}

	public static <T> BaseResponse<T> failure(String code, String message) {
		return new BaseResponse<>(false, code, message, null);
	}

	public boolean isSuccess() {
		return isSuccess;
	}

	public String getCode() {
		return code;
	}

	public String getMessage() {
		return message;
	}

	public T getResult() {
		return result;
	}
}
