package com.umc.learninglm.domain.auth.dto.response;

import com.umc.learninglm.domain.auth.enums.VerificationPurpose;
import com.umc.learninglm.domain.auth.enums.VerificationType;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "이메일 인증코드 발송 응답")
public record EmailVerificationResponse(
		@Schema(example = "NON_LOGIN") VerificationType verificationType,
		@Schema(example = "SIGNUP") VerificationPurpose purpose,
		@Schema(example = "user@example.com") String email,
		@Schema(example = "1800") int expiresInSeconds,
		@Schema(description = "SMTP 연동 전 개발 확인용 인증코드", example = "A1b2C3d4E5") String verificationCode
) {
}
