package com.umc.learninglm.domain.auth.dto.response;

import com.umc.learninglm.domain.auth.enums.VerificationPurpose;
import com.umc.learninglm.domain.auth.enums.VerificationType;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "이메일 인증코드 검증 응답")
public record EmailVerificationVerifyResponse(
		@Schema(example = "NON_LOGIN") VerificationType verificationType,
		@Schema(example = "PASSWORD_RESET") VerificationPurpose purpose,
		@Schema(example = "user@example.com") String email,
		@Schema(example = "true") boolean verified,
		@Schema(example = "temporary-access-token") String temporaryAccessToken,
		@Schema(example = "300") int expiresInSeconds
) {
}
