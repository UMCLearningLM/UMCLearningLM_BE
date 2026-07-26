package com.umc.learninglm.global.error;

import org.springframework.http.HttpStatus;

public enum ErrorCode {

	// Common
	INVALID_INPUT_VALUE(HttpStatus.BAD_REQUEST, "COMMON400", "요청 값이 올바르지 않습니다."),
	INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "COMMON500", "서버 내부 오류가 발생했습니다."),

	// Tutorial
	TUTORIAL_INVALID_STEP(HttpStatus.BAD_REQUEST, "TUTORIAL40001", "유효하지 않은 진행 단계 값입니다."),
	TUTORIAL_INVALID_FILTER(HttpStatus.BAD_REQUEST, "TUTORIAL40002", "유효하지 않은 검색/필터 파라미터입니다."),
	TUTORIAL_NOT_STARTED(HttpStatus.BAD_REQUEST, "TUTORIAL40003", "학습이 시작되지 않은 튜토리얼입니다."),
	TUTORIAL_FLOW_MISMATCH(HttpStatus.BAD_REQUEST, "TUTORIAL40004", "해당 튜토리얼의 flow가 아닙니다."),
	TUTORIAL_NOT_FOUND(HttpStatus.NOT_FOUND, "TUTORIAL40401", "존재하지 않는 튜토리얼입니다."),
	TUTORIAL_PROGRESS_NOT_FOUND(HttpStatus.NOT_FOUND, "TUTORIAL40402", "저장(진행) 정보가 없습니다."),
	TUTORIAL_STEPS_NOT_FOUND(HttpStatus.NOT_FOUND, "TUTORIAL40403", "튜토리얼 단계 정보가 없습니다."),
	TUTORIAL_ALREADY_SAVED(HttpStatus.CONFLICT, "TUTORIAL40901", "이미 저장된 튜토리얼입니다.");

	private final HttpStatus httpStatus;
	private final String code;
	private final String message;

	ErrorCode(HttpStatus httpStatus, String code, String message) {
		this.httpStatus = httpStatus;
		this.code = code;
		this.message = message;
	}

	public HttpStatus getHttpStatus() {
		return httpStatus;
	}

	public String getCode() {
		return code;
	}

	public String getMessage() {
		return message;
	}
}
