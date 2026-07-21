package com.umc.learninglm.global.error;

import com.umc.learninglm.global.common.BaseResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

	@ExceptionHandler(CustomException.class)
	public ResponseEntity<BaseResponse<Void>> handleCustomException(CustomException e) {
		ErrorCode errorCode = e.getErrorCode();
		return ResponseEntity.status(errorCode.getHttpStatus())
				.body(BaseResponse.failure(errorCode.getCode(), errorCode.getMessage()));
	}

	@ExceptionHandler(MethodArgumentNotValidException.class)
	public ResponseEntity<BaseResponse<Void>> handleValidationException(MethodArgumentNotValidException e) {
		ErrorCode errorCode = e.getBindingResult().getAllErrors().stream()
				.findFirst()
				.map(this::resolveValidationError)
				.orElse(ErrorCode.INVALID_INPUT_VALUE);
		return ResponseEntity.status(errorCode.getHttpStatus())
				.body(BaseResponse.failure(errorCode.getCode(), errorCode.getMessage()));
	}

	@ExceptionHandler(HttpMessageNotReadableException.class)
	public ResponseEntity<BaseResponse<Void>> handleUnreadableMessage(HttpMessageNotReadableException e) {
		String causeMessage = e.getMostSpecificCause().getMessage();
		ErrorCode errorCode;
		if (causeMessage != null && causeMessage.contains("VerificationType")) {
			errorCode = ErrorCode.VERIFICATION_TYPE_INVALID;
		} else if (causeMessage != null && causeMessage.contains("VerificationPurpose")) {
			errorCode = ErrorCode.VERIFICATION_PURPOSE_INVALID;
		} else {
			errorCode = ErrorCode.INVALID_INPUT_VALUE;
		}
		return ResponseEntity.status(errorCode.getHttpStatus())
				.body(BaseResponse.failure(errorCode.getCode(), errorCode.getMessage()));
	}

	private ErrorCode resolveValidationError(ObjectError error) {
		String message = error.getDefaultMessage();
		if (message == null) {
			return ErrorCode.INVALID_INPUT_VALUE;
		}
		return switch (message) {
			case "회원가입을 위해 약관 동의가 필요합니다." -> ErrorCode.TERMS_AGREEMENT_REQUIRED;
			case "이메일 형식이 올바르지 않습니다." -> ErrorCode.INVALID_EMAIL_FORMAT;
			case "비밀번호 형식이 올바르지 않습니다." -> ErrorCode.INVALID_PASSWORD_FORMAT;
			case "닉네임 형식이 올바르지 않습니다." -> ErrorCode.INVALID_NICKNAME_FORMAT;
			case "새 비밀번호를 입력해주세요." -> ErrorCode.NEW_PASSWORD_MISSING;
			case "현재 비밀번호를 입력해주세요." -> ErrorCode.CURRENT_PASSWORD_MISSING;
			case "수정할 정보가 없습니다." -> ErrorCode.NO_PROFILE_CHANGES;
			case "리프레시 토큰이 필요합니다." -> ErrorCode.REFRESH_TOKEN_MISSING;
			default -> ErrorCode.REQUIRED_VALUE_MISSING;
		};
	}

	@ExceptionHandler(Exception.class)
	public ResponseEntity<BaseResponse<Void>> handleException(Exception e) {
		return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
				.body(BaseResponse.failure(ErrorCode.INTERNAL_SERVER_ERROR.getCode(), ErrorCode.INTERNAL_SERVER_ERROR.getMessage()));
	}
}
