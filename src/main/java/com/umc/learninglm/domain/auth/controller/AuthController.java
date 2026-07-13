package com.umc.learninglm.domain.auth.controller;

import com.umc.learninglm.domain.auth.dto.request.EmailVerificationRequest;
import com.umc.learninglm.domain.auth.dto.request.EmailVerificationVerifyRequest;
import com.umc.learninglm.domain.auth.dto.request.LoginRequest;
import com.umc.learninglm.domain.auth.dto.request.LogoutRequest;
import com.umc.learninglm.domain.auth.dto.request.PasswordResetRequest;
import com.umc.learninglm.domain.auth.dto.request.ReissueRequest;
import com.umc.learninglm.domain.auth.dto.request.SignupRequest;
import com.umc.learninglm.domain.auth.dto.response.AuthTokenResponse;
import com.umc.learninglm.domain.auth.dto.response.EmailVerificationResponse;
import com.umc.learninglm.domain.auth.dto.response.EmailVerificationVerifyResponse;
import com.umc.learninglm.domain.auth.dto.response.LogoutResponse;
import com.umc.learninglm.domain.auth.dto.response.MeResponse;
import com.umc.learninglm.domain.auth.dto.response.OAuthAuthorizationResponse;
import com.umc.learninglm.domain.auth.dto.response.PasswordResetResponse;
import com.umc.learninglm.domain.auth.dto.response.ReissueResponse;
import com.umc.learninglm.global.common.BaseResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Auth", description = "회원가입, 로그인, 토큰 및 이메일 인증 API")
@RestController
@RequestMapping("/api/auth")
public class AuthController {

	@PostMapping("/signup")
	@Operation(summary = "로컬 회원가입", description = "이메일 인증용 임시 Access Token으로 신규 사용자를 생성합니다.",
			security = @SecurityRequirement(name = "bearerAuth"))
	@ApiResponses({
			@ApiResponse(responseCode = "200", description = "회원가입 성공"),
			@ApiResponse(responseCode = "400", description = "AUTH40001~AUTH40006: 회원가입 입력값 오류")
	})
	public BaseResponse<AuthTokenResponse> signup(@Valid @RequestBody SignupRequest request) {
		return BaseResponse.success(new AuthTokenResponse(
				1L, request.email(), request.nickname(), "access-token", "refresh-token"));
	}

	@PostMapping("/login")
	@Operation(summary = "로컬 로그인", description = "이메일과 비밀번호를 검증하고 토큰을 발급합니다.")
	@ApiResponses({
			@ApiResponse(responseCode = "200", description = "로그인 성공"),
			@ApiResponse(responseCode = "400", description = "AUTH40007: 소셜 가입 계정"),
			@ApiResponse(responseCode = "401", description = "AUTH40101~AUTH40102: 이메일 또는 비밀번호 불일치")
	})
	public BaseResponse<AuthTokenResponse> login(@Valid @RequestBody LoginRequest request) {
		return BaseResponse.success(new AuthTokenResponse(
				1L, request.email(), "홍길동", "access-token", "refresh-token"));
	}

	@PostMapping("/logout")
	@Operation(summary = "로그아웃", description = "Refresh Token을 폐기하고 Access Token을 블랙리스트에 등록합니다.",
			security = @SecurityRequirement(name = "bearerAuth"))
	@ApiResponses({
			@ApiResponse(responseCode = "200", description = "로그아웃 성공"),
			@ApiResponse(responseCode = "401", description = "AUTH40103~AUTH40105: Access Token 오류")
	})
	public BaseResponse<LogoutResponse> logout(@Valid @RequestBody LogoutRequest request) {
		return BaseResponse.success(new LogoutResponse(true));
	}

	@GetMapping("/google")
	@Operation(summary = "Google OAuth 인증 URL 조회")
	@ApiResponse(responseCode = "200", description = "인증 URL 조회 성공")
	public BaseResponse<OAuthAuthorizationResponse> getGoogleAuthorizationUrl() {
		return BaseResponse.success(new OAuthAuthorizationResponse(
				"https://accounts.google.com/o/oauth2/v2/auth"));
	}

	@GetMapping("/google/callback")
	@Operation(summary = "Google OAuth 콜백", description = "Google 인증 결과를 처리합니다. 실제 리다이렉트 및 토큰 처리는 서비스 구현 시 연결합니다.")
	@ApiResponses({
			@ApiResponse(responseCode = "200", description = "OAuth 콜백 처리 성공"),
			@ApiResponse(responseCode = "400", description = "AUTH40008: OAuth code 누락"),
			@ApiResponse(responseCode = "401", description = "AUTH40106~AUTH40107: 인증 취소 또는 state 검증 실패"),
			@ApiResponse(responseCode = "500", description = "AUTH50001: 소셜 계정 처리 실패"),
			@ApiResponse(responseCode = "502", description = "AUTH50201~AUTH50202: Google 연동 실패")
	})
	public BaseResponse<Void> googleCallback(
			@RequestParam(required = false) String code,
			@RequestParam(required = false) String state,
			@RequestParam(required = false) String error) {
		return BaseResponse.success(null);
	}

	@PostMapping("/reissue")
	@Operation(summary = "토큰 재발급", description = "Refresh Token을 검증하고 Rotation 방식으로 토큰을 재발급합니다.")
	@ApiResponses({
			@ApiResponse(responseCode = "200", description = "토큰 재발급 성공"),
			@ApiResponse(responseCode = "401", description = "AUTH40108~AUTH40110: Refresh Token 오류")
	})
	public BaseResponse<ReissueResponse> reissue(@Valid @RequestBody ReissueRequest request) {
		return BaseResponse.success(new ReissueResponse("new-access-token", "new-refresh-token"));
	}

	@GetMapping("/me")
	@Operation(summary = "현재 사용자 조회", description = "Access Token에서 식별한 사용자 정보를 반환합니다.",
			security = @SecurityRequirement(name = "bearerAuth"))
	@ApiResponses({
			@ApiResponse(responseCode = "200", description = "사용자 조회 성공"),
			@ApiResponse(responseCode = "401", description = "AUTH40103~AUTH40105: Access Token 오류"),
			@ApiResponse(responseCode = "404", description = "AUTH40401: 사용자 없음")
	})
	public BaseResponse<MeResponse> me() {
		return BaseResponse.success(new MeResponse(
				1L, "user@example.com", "홍길동", "LOCAL", null));
	}

	@PostMapping("/email/request")
	@Operation(summary = "이메일 인증코드 발송", description = "Gmail SMTP로 이메일 인증코드를 전송합니다.")
	@ApiResponses({
			@ApiResponse(responseCode = "200", description = "인증코드 발송 성공"),
			@ApiResponse(responseCode = "400", description = "AUTH40006, AUTH40009~AUTH40013: 요청값 오류"),
			@ApiResponse(responseCode = "401", description = "AUTH40103~AUTH40104: 로그인 인증 토큰 오류"),
			@ApiResponse(responseCode = "429", description = "AUTH42901: 전송 횟수 초과"),
			@ApiResponse(responseCode = "502", description = "AUTH50203: Gmail SMTP 전송 실패")
	})
	@Parameter(name = "Authorization", description = "LOGIN 타입일 때 Bearer Access Token", in = ParameterIn.HEADER,
			required = false, example = "Bearer access-token")
	public BaseResponse<EmailVerificationResponse> requestEmailVerification(
			@RequestHeader(value = "Authorization", required = false) String authorization,
			@Valid @RequestBody EmailVerificationRequest request) {
		return BaseResponse.success(new EmailVerificationResponse(
				request.verificationType(), request.purpose(), request.email(), 300));
	}

	@PostMapping("/email/verify")
	@Operation(summary = "이메일 인증코드 검증", description = "인증 성공 시 후속 API용 임시 Access Token을 발급합니다.")
	@ApiResponses({
			@ApiResponse(responseCode = "200", description = "인증코드 검증 성공"),
			@ApiResponse(responseCode = "400", description = "AUTH40009~AUTH40016: 요청값 또는 인증코드 오류"),
			@ApiResponse(responseCode = "401", description = "AUTH40103~AUTH40104: 로그인 인증 토큰 오류"),
			@ApiResponse(responseCode = "429", description = "AUTH42902: 인증 시도 횟수 초과")
	})
	@Parameter(name = "Authorization", description = "LOGIN 타입일 때 Bearer Access Token", in = ParameterIn.HEADER,
			required = false, example = "Bearer access-token")
	public BaseResponse<EmailVerificationVerifyResponse> verifyEmail(
			@RequestHeader(value = "Authorization", required = false) String authorization,
			@Valid @RequestBody EmailVerificationVerifyRequest request) {
		return BaseResponse.success(new EmailVerificationVerifyResponse(
				request.verificationType(), request.purpose(), request.email(), true,
				"temporary-access-token", 300));
	}

	@PostMapping("/password")
	@Operation(summary = "비밀번호 재설정", description = "PASSWORD_RESET 목적의 임시 Access Token으로 비밀번호를 변경합니다.",
			security = @SecurityRequirement(name = "bearerAuth"))
	@ApiResponses({
			@ApiResponse(responseCode = "200", description = "비밀번호 재설정 성공"),
			@ApiResponse(responseCode = "400", description = "AUTH40004, AUTH40017~AUTH40018: 비밀번호 입력 오류"),
			@ApiResponse(responseCode = "401", description = "AUTH40111~AUTH40113: 임시 Access Token 오류"),
			@ApiResponse(responseCode = "403", description = "AUTH40301~AUTH40302: 토큰 타입 또는 목적 불일치"),
			@ApiResponse(responseCode = "404", description = "AUTH40401: 사용자 없음")
	})
	public BaseResponse<PasswordResetResponse> resetPassword(
			@Valid @RequestBody PasswordResetRequest request) {
		return BaseResponse.success(new PasswordResetResponse(true, true));
	}
}
