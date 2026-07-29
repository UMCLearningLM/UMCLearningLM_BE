package com.umc.learninglm.domain.auth.dto.request;

import com.umc.learninglm.domain.auth.enums.VerificationPurpose;
import com.umc.learninglm.domain.auth.enums.VerificationType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;

@Schema(description = "이메일 인증코드 발송 요청")
public record EmailVerificationRequest(
		@Schema(example = "NON_LOGIN")
		VerificationType verificationType,

		@Schema(example = "SIGNUP")
		VerificationPurpose purpose,

		@Schema(description = "비로그인 인증 또는 LOGIN/EMAIL_CHANGE의 새 이메일일 때 필수",
				example = "user@example.com", nullable = true)
		@Email(message = "이메일 형식이 올바르지 않습니다.")
		String email
) {
}
