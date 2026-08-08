package com.umc.learninglm.global.error;

import org.springframework.http.HttpStatus;

public enum ErrorCode {

	// Common
	INVALID_INPUT_VALUE(HttpStatus.BAD_REQUEST, "COMMON400", "요청 값이 올바르지 않습니다."),
	INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "COMMON500", "서버 내부 오류가 발생했습니다."),

	// Auth
	EMAIL_ALREADY_EXISTS(HttpStatus.BAD_REQUEST, "AUTH40001", "이미 가입된 이메일입니다."),
	REQUIRED_VALUE_MISSING(HttpStatus.BAD_REQUEST, "AUTH40002", "필수 입력값이 누락되었습니다."),
	TERMS_AGREEMENT_REQUIRED(HttpStatus.BAD_REQUEST, "AUTH40003", "회원가입을 위해 약관 동의가 필요합니다."),
	INVALID_PASSWORD_FORMAT(HttpStatus.BAD_REQUEST, "AUTH40004", "비밀번호 형식이 올바르지 않습니다."),
	INVALID_NICKNAME_FORMAT(HttpStatus.BAD_REQUEST, "AUTH40005", "닉네임 형식이 올바르지 않습니다."),
	INVALID_EMAIL_FORMAT(HttpStatus.BAD_REQUEST, "AUTH40006", "이메일 형식이 올바르지 않습니다."),
	SOCIAL_ACCOUNT_LOCAL_LOGIN(HttpStatus.BAD_REQUEST, "AUTH40007", "소셜 로그인으로 가입된 계정입니다."),
	VERIFICATION_TYPE_MISSING(HttpStatus.BAD_REQUEST, "AUTH40009", "인증 타입이 필요합니다."),
	VERIFICATION_TYPE_INVALID(HttpStatus.BAD_REQUEST, "AUTH40010", "인증 타입이 올바르지 않습니다."),
	VERIFICATION_PURPOSE_MISSING(HttpStatus.BAD_REQUEST, "AUTH40011", "인증 목적이 필요합니다."),
	VERIFICATION_PURPOSE_INVALID(HttpStatus.BAD_REQUEST, "AUTH40012", "인증 목적이 올바르지 않습니다."),
	EMAIL_REQUIRED(HttpStatus.BAD_REQUEST, "AUTH40013", "이메일이 필요합니다."),
	SAME_AS_CURRENT_PASSWORD(HttpStatus.BAD_REQUEST, "AUTH40018", "기존 비밀번호와 다른 비밀번호를 입력해주세요."),
	ACCOUNT_NOT_FOUND(HttpStatus.UNAUTHORIZED, "AUTH40101", "이메일 또는 비밀번호가 올바르지 않습니다."),
	PASSWORD_MISMATCH(HttpStatus.UNAUTHORIZED, "AUTH40102", "이메일 또는 비밀번호가 올바르지 않습니다."),
	ACCESS_TOKEN_MISSING(HttpStatus.UNAUTHORIZED, "AUTH40103", "인증 토큰이 필요합니다."),
	INVALID_ACCESS_TOKEN(HttpStatus.UNAUTHORIZED, "AUTH40104", "유효하지 않은 토큰입니다."),
	ALREADY_LOGGED_OUT_TOKEN(HttpStatus.UNAUTHORIZED, "AUTH40105", "이미 로그아웃된 토큰입니다."),
	REFRESH_TOKEN_MISSING(HttpStatus.UNAUTHORIZED, "AUTH40108", "리프레시 토큰이 필요합니다."),
	INVALID_REFRESH_TOKEN(HttpStatus.UNAUTHORIZED, "AUTH40109", "유효하지 않은 리프레시 토큰입니다."),
	SESSION_EXPIRED(HttpStatus.UNAUTHORIZED, "AUTH40110", "세션이 만료되었습니다."),
	EMAIL_CODE_MISSING(HttpStatus.BAD_REQUEST, "AUTH40014", "인증코드를 입력해주세요."),
	EMAIL_CODE_MISMATCH(HttpStatus.BAD_REQUEST, "AUTH40015", "인증코드가 일치하지 않습니다."),
	EMAIL_CODE_EXPIRED(HttpStatus.BAD_REQUEST, "AUTH40016", "인증코드가 만료되었습니다."),
	NEW_PASSWORD_MISSING(HttpStatus.BAD_REQUEST, "AUTH40017", "새 비밀번호를 입력해주세요."),
	EMAIL_VERIFICATION_TOKEN_MISSING(HttpStatus.UNAUTHORIZED, "AUTH40111", "이메일 인증 토큰이 필요합니다."),
	INVALID_EMAIL_VERIFICATION_TOKEN(HttpStatus.UNAUTHORIZED, "AUTH40112", "유효하지 않은 이메일 인증 토큰입니다."),
	EMAIL_VERIFICATION_TOKEN_EXPIRED(HttpStatus.UNAUTHORIZED, "AUTH40113", "이메일 인증 토큰이 만료되었습니다."),
	TOKEN_TYPE_MISMATCH(HttpStatus.FORBIDDEN, "AUTH40301", "토큰의 tokenType이 일치하지 않습니다."),
	TOKEN_PURPOSE_MISMATCH(HttpStatus.FORBIDDEN, "AUTH40302", "토큰의 purpose가 일치하지 않습니다."),
	NO_PROFILE_CHANGES(HttpStatus.BAD_REQUEST, "AUTH40019", "수정할 정보가 없습니다."),
	SAME_AS_CURRENT_EMAIL(HttpStatus.BAD_REQUEST, "AUTH40020", "현재 이메일과 다른 이메일을 입력해주세요."),
	CURRENT_PASSWORD_MISSING(HttpStatus.BAD_REQUEST, "AUTH40021", "현재 비밀번호를 입력해주세요."),
	SOCIAL_PASSWORD_CHANGE_NOT_ALLOWED(HttpStatus.BAD_REQUEST, "AUTH40022", "소셜 로그인 계정은 비밀번호를 변경할 수 없습니다."),
	CURRENT_PASSWORD_MISMATCH(HttpStatus.UNAUTHORIZED, "AUTH40114", "현재 비밀번호가 일치하지 않습니다."),
	EMAIL_CHANGE_NOT_ALLOWED(HttpStatus.FORBIDDEN, "AUTH40303", "이메일 변경 권한이 없습니다."),
	EMAIL_CODE_SEND_LIMIT_EXCEEDED(HttpStatus.TOO_MANY_REQUESTS, "AUTH42901", "인증코드 전송 요청이 너무 많습니다."),
	EMAIL_CODE_ATTEMPT_LIMIT_EXCEEDED(HttpStatus.TOO_MANY_REQUESTS, "AUTH42902", "인증 시도 횟수를 초과했습니다."),
	USER_NOT_FOUND(HttpStatus.NOT_FOUND, "AUTH40401", "사용자를 찾을 수 없습니다."),
	OAUTH_CODE_MISSING(HttpStatus.BAD_REQUEST, "AUTH40008", "OAuth 인증 코드가 누락되었습니다."),
	OAUTH_AUTHENTICATION_FAILED(HttpStatus.UNAUTHORIZED, "AUTH40106", "소셜 로그인 인증에 실패했습니다."),
	OAUTH_STATE_INVALID(HttpStatus.UNAUTHORIZED, "AUTH40107", "유효하지 않은 OAuth 요청입니다."),
	SOCIAL_ACCOUNT_PROCESSING_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "AUTH50001", "소셜 계정 처리 중 오류가 발생했습니다."),
	GOOGLE_TOKEN_ISSUE_FAILED(HttpStatus.BAD_GATEWAY, "AUTH50201", "Google 인증 서버 연동에 실패했습니다."),
	GOOGLE_USER_INFO_FAILED(HttpStatus.BAD_GATEWAY, "AUTH50202", "Google 사용자 정보를 조회하지 못했습니다."),
	EMAIL_SEND_FAILED(HttpStatus.BAD_GATEWAY, "AUTH50203", "인증 이메일 전송에 실패했습니다."),

	// Tutorial
	TUTORIAL_INVALID_STEP(HttpStatus.BAD_REQUEST, "TUTORIAL40001", "유효하지 않은 진행 단계 값입니다."),
	TUTORIAL_INVALID_FILTER(HttpStatus.BAD_REQUEST, "TUTORIAL40002", "유효하지 않은 검색/필터 파라미터입니다."),
	TUTORIAL_NOT_STARTED(HttpStatus.BAD_REQUEST, "TUTORIAL40003", "학습이 시작되지 않은 튜토리얼입니다."),
	TUTORIAL_FLOW_MISMATCH(HttpStatus.BAD_REQUEST, "TUTORIAL40004", "해당 튜토리얼의 flow가 아닙니다."),
	TUTORIAL_NOT_GUIDED_FLOW(HttpStatus.BAD_REQUEST, "TUTORIAL40005", "가이드 모드 flow가 아닙니다."),
	TUTORIAL_NOT_FOUND(HttpStatus.NOT_FOUND, "TUTORIAL40401", "존재하지 않는 튜토리얼입니다."),
	TUTORIAL_PROGRESS_NOT_FOUND(HttpStatus.NOT_FOUND, "TUTORIAL40402", "저장(진행) 정보가 없습니다."),
	TUTORIAL_STEPS_NOT_FOUND(HttpStatus.NOT_FOUND, "TUTORIAL40403", "튜토리얼 단계 정보가 없습니다."),
	TUTORIAL_ALREADY_SAVED(HttpStatus.CONFLICT, "TUTORIAL40901", "이미 저장된 튜토리얼입니다."),
	TUTORIAL_ALREADY_STARTED(HttpStatus.CONFLICT, "TUTORIAL40902", "이미 다른 flow로 진행 중인 튜토리얼입니다."),
	TUTORIAL_ALREADY_COMPLETED(HttpStatus.CONFLICT, "TUTORIAL40903", "이미 완료한 튜토리얼입니다."),

    // Home
    HOME_CATEGORY_CONFIGURATION_MISSING(HttpStatus.INTERNAL_SERVER_ERROR, "HOME50001","홈 화면 필수 카테고리 데이터가 누락되었습니다."),
    HOME_CATEGORY_CODE_INVALID(HttpStatus.INTERNAL_SERVER_ERROR,"HOME50002","유효하지 않은 카테고리 데이터가 존재합니다."),

    // Library
    LIBRARY_INVALID_FILTER(HttpStatus.BAD_REQUEST,"LIBRARY40001","잘못된 필터 값입니다."),
    LIBRARY_CATEGORY_LIMIT_EXCEEDED(HttpStatus.BAD_REQUEST,"LIBRARY40003","카테고리는 최대 3개까지 선택할 수 있습니다."),
    LIBRARY_DIFFICULTY_LIMIT_EXCEEDED(HttpStatus.BAD_REQUEST,"LIBRARY40004","난이도는 최대 2개까지 선택할 수 있습니다."),
    LIBRARY_FLOW_NOT_FOUND(HttpStatus.NOT_FOUND, "LIBRARY40401", "공개 흐름을 찾을 수 없습니다."),

    // Storage
    STORAGE_INVALID_PARAMETER(HttpStatus.BAD_REQUEST, "STORAGE40001", "유효하지 않은 조회 파라미터입니다."),

    // Block
    BLOCK_INVALID_STAGE(HttpStatus.BAD_REQUEST,"BLOCK40001", "유효하지 않은 stage 값입니다."),
    BLOCK_PROMPT_VARIABLE_MISSING(HttpStatus.BAD_REQUEST, "BLOCK40002", "프롬프트 변수에 대응하는 값이 없습니다."),
    BLOCK_PROMPT_VARIABLE_AMBIGUOUS(HttpStatus.BAD_REQUEST, "BLOCK40003", "동일한 프롬프트 변수가 여러 입력 영역에 존재합니다."),
    BLOCK_PROMPT_TEMPLATE_NOT_FOUND(HttpStatus.NOT_FOUND, "BLOCK40401", "사용 가능한 블록 프롬프트 템플릿이 없습니다."),
    BLOCK_STAGE_CONFIGURATION_INVALID(HttpStatus.INTERNAL_SERVER_ERROR, "BLOCK50001", "유효하지 않은 블록 단계 데이터가 존재합니다."),
    BLOCK_PROMPT_CONVERSION_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "BLOCK50002", "블록 프롬프트 변환 중 오류가 발생했습니다."),
    BLOCK_PROMPT_TEMPLATE_STAGE_MISMATCH(HttpStatus.INTERNAL_SERVER_ERROR, "BLOCK50003", "블록과 프롬프트 템플릿의 단계가 일치하지 않습니다."),

    // Flow
    FLOW_INVALID_REQUEST(HttpStatus.BAD_REQUEST, "FLOW40001", "잘못된 요청입니다."),
    FLOW_OPTIONS_PARSE_FAILED(HttpStatus.BAD_REQUEST, "FLOW40003", "블록 옵션을 파싱할 수 없습니다."),
    FLOW_OWNER_MISMATCH(HttpStatus.FORBIDDEN, "FLOW40301", "흐름 소유자가 아닙니다."),
    FLOW_NOT_FOUND(HttpStatus.NOT_FOUND, "FLOW40401", "존재하지 않는 흐름입니다."),
    FLOW_BLOCK_NOT_FOUND(HttpStatus.NOT_FOUND, "FLOW40402", "존재하지 않는 블록입니다."),
    FLOW_ORIGIN_NOT_FOUND(HttpStatus.NOT_FOUND, "FLOW40403", "복사할 원본 흐름을 찾을 수 없습니다.");

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
