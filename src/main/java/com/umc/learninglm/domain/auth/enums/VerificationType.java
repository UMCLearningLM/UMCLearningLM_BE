package com.umc.learninglm.domain.auth.enums;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "이메일 인증 요청 상태")
public enum VerificationType {
	LOGIN,
	NON_LOGIN
}
