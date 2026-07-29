package com.umc.learninglm.domain.auth.dto.request;

import com.umc.learninglm.domain.auth.validation.AuthValidationPatterns;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

@Schema(description = "로컬 회원가입 요청")
public record SignupRequest(
		@Schema(example = "user@example.com")
		@NotBlank(message = "이메일은 필수입니다.")
		@Email(message = "이메일 형식이 올바르지 않습니다.")
		String email,

		@Schema(example = "Password123!")
		@NotBlank(message = "비밀번호는 필수입니다.")
		@Pattern(regexp = AuthValidationPatterns.PASSWORD, message = "비밀번호 형식이 올바르지 않습니다.")
		String password,

		@Schema(example = "홍길동")
		@NotBlank(message = "닉네임은 필수입니다.")
		@Pattern(regexp = AuthValidationPatterns.NICKNAME, message = "닉네임 형식이 올바르지 않습니다.")
		String nickname,

		@Schema(example = "true")
		@AssertTrue(message = "회원가입을 위해 약관 동의가 필요합니다.")
		boolean termsAgreed
) {
}
