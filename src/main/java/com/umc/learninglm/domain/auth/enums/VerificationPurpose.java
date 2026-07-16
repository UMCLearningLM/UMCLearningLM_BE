package com.umc.learninglm.domain.auth.enums;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "이메일 인증 목적")
public enum VerificationPurpose {
	SIGNUP,
	PASSWORD_RESET,
	EMAIL_CHANGE
}
