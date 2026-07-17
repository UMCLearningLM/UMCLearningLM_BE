package com.umc.learninglm.domain.auth.dto.request;

import com.umc.learninglm.domain.auth.enums.VerificationPurpose;
import com.umc.learninglm.domain.auth.enums.VerificationType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

@Schema(description = "이메일 인증코드 검증 요청")
public record EmailVerificationVerifyRequest(
		@Schema(example = "NON_LOGIN")
		@NotNull(message = "인증 타입이 필요합니다.")
		VerificationType verificationType,

		@Schema(example = "PASSWORD_RESET")
		@NotNull(message = "인증 목적이 필요합니다.")
		VerificationPurpose purpose,

		@Schema(description = "비로그인 인증일 때 필수", example = "user@example.com", nullable = true)
		@Email(message = "이메일 형식이 올바르지 않습니다.")
		String email,

		@Schema(example = "123456")
		@NotBlank(message = "인증코드를 입력해주세요.")
		@Pattern(regexp = "\\d{6}", message = "인증코드는 6자리 숫자여야 합니다.")
		String code
) {
}
