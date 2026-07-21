package com.umc.learninglm.domain.auth.controller;

import com.umc.learninglm.domain.auth.dto.request.EmailVerificationRequest;
import com.umc.learninglm.domain.auth.dto.request.EmailVerificationVerifyRequest;
import com.umc.learninglm.domain.auth.dto.request.LoginRequest;
import com.umc.learninglm.domain.auth.dto.request.LogoutRequest;
import com.umc.learninglm.domain.auth.dto.request.PasswordResetRequest;
import com.umc.learninglm.domain.auth.dto.request.ProfileUpdateRequest;
import com.umc.learninglm.domain.auth.dto.request.ReissueRequest;
import com.umc.learninglm.domain.auth.dto.request.SignupRequest;
import com.umc.learninglm.domain.auth.dto.response.AuthTokenResponse;
import com.umc.learninglm.domain.auth.dto.response.EmailVerificationResponse;
import com.umc.learninglm.domain.auth.dto.response.EmailVerificationVerifyResponse;
import com.umc.learninglm.domain.auth.dto.response.LogoutResponse;
import com.umc.learninglm.domain.auth.dto.response.MeResponse;
import com.umc.learninglm.domain.auth.dto.response.OAuthAuthorizationResponse;
import com.umc.learninglm.domain.auth.dto.response.PasswordResetResponse;
import com.umc.learninglm.domain.auth.dto.response.ProfileResponse;
import com.umc.learninglm.domain.auth.dto.response.ReissueResponse;
import com.umc.learninglm.domain.auth.service.AuthService;
import com.umc.learninglm.global.common.BaseResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Auth", description = "회원가입, 로그인, 토큰 및 이메일 인증 API")
@RestController
@RequestMapping("/api/auth")
public class AuthController {

	private final AuthService authService;
	private final String googleAuthorizationEntryPoint;

	public AuthController(
			AuthService authService,
			@Value("${GOOGLE_AUTHORIZATION_ENTRY_POINT:http://localhost:8080/api/auth/oauth2/authorization/google}")
			String googleAuthorizationEntryPoint) {
		this.authService = authService;
		this.googleAuthorizationEntryPoint = googleAuthorizationEntryPoint;
	}

	@PostMapping("/signup")
	@Operation(summary = "로컬 회원가입", description = """
			신규 로컬 사용자를 생성하고 로그인용 Access Token과 Refresh Token을 발급합니다.

			실행 전 준비:

			① POST /auth/email/request를 verificationType=NON_LOGIN, purpose=SIGNUP으로 호출합니다.

			② 응답의 verificationCode로 POST /auth/email/verify를 호출합니다.

			③ 검증 응답의 temporaryAccessToken을 Swagger 상단 **Authorize > emailVerificationToken**에 등록합니다. Bearer 접두사는 입력하지 않습니다.

			④ 이메일 인증에 사용한 이메일과 동일한 email을 요청 Body에 입력합니다.

			이메일 인증코드가 아닌 코드 검증 후 발급된 임시 Access Token이 필요하며, 토큰은 30분 동안 유효하고 회원가입 성공 시 사용 처리됩니다.
			""",
			security = @SecurityRequirement(name = "emailVerificationToken"))
	@ApiResponses({
			@ApiResponse(responseCode = "200", description = "회원가입 성공"),
			@ApiResponse(responseCode = "400", description = "AUTH40001~AUTH40006: 회원가입 입력값 오류"),
			@ApiResponse(responseCode = "401", description = "AUTH40111~AUTH40113: 이메일 인증 토큰 오류"),
			@ApiResponse(responseCode = "403", description = "AUTH40301~AUTH40302: 토큰 타입 또는 목적 불일치")
	})
	public BaseResponse<AuthTokenResponse> signup(
			@Parameter(hidden = true)
			@RequestHeader(value = "X-Email-Verification-Token", required = false) String emailVerificationToken,
			@Valid @RequestBody SignupRequest request) {
		return BaseResponse.success(authService.signup(emailVerificationToken, request));
	}

	@PostMapping("/login")
	@Operation(summary = "로컬 로그인", description = """
			가입이 완료된 로컬 계정의 이메일과 비밀번호를 검증하고 Access Token과 Refresh Token을 발급합니다.

			별도의 Authorize 설정은 필요하지 않습니다. Google 소셜 계정은 이 API로 로그인할 수 없습니다.
			rememberMe=false이면 Refresh Token은 1일, true이면 7일 동안 유효하며 Access Token은 모두 30분 동안 유효합니다.
			응답 토큰은 사용자 조회·프로필 수정·로그아웃·토큰 재발급에 사용합니다.
			""")
	@ApiResponses({
			@ApiResponse(responseCode = "200", description = "로그인 성공"),
			@ApiResponse(responseCode = "400", description = "AUTH40007: 소셜 가입 계정"),
			@ApiResponse(responseCode = "401", description = "AUTH40101~AUTH40102: 이메일 또는 비밀번호 불일치")
	})
	public BaseResponse<AuthTokenResponse> login(@Valid @RequestBody LoginRequest request) {
		return BaseResponse.success(authService.login(request));
	}

	@PostMapping("/logout")
	@Operation(summary = "로그아웃", description = """
			현재 세션의 Refresh Token을 폐기하고 Access Token을 남은 만료 시간 동안 블랙리스트에 등록합니다.

			실행 전 준비:

			① 로컬 또는 Google 로그인 응답의 accessToken을 Swagger 상단 **Authorize > bearerAuth**에 등록합니다. Bearer 접두사는 입력하지 않습니다.

			② 같은 로그인 응답에서 받은 refreshToken을 요청 Body에 입력합니다.

			Access Token과 Refresh Token의 사용자가 서로 다르거나 이미 폐기된 Refresh Token이면 실패합니다.
			""",
			security = @SecurityRequirement(name = "bearerAuth"))
	@ApiResponses({
			@ApiResponse(responseCode = "200", description = "로그아웃 성공"),
			@ApiResponse(responseCode = "401", description = "AUTH40103~AUTH40105, AUTH40108~AUTH40110: Access/Refresh Token 오류")
	})
	public BaseResponse<LogoutResponse> logout(
			@Parameter(hidden = true)
			@RequestHeader(value = "Authorization", required = false) String authorization,
			@Valid @RequestBody LogoutRequest request) {
		return BaseResponse.success(authService.logout(authorization, request));
	}

	@GetMapping("/google")
	@Operation(summary = "Google OAuth 인증 URL 조회", description = """
			Google 소셜 로그인을 시작할 백엔드 인증 URL을 반환합니다. Authorize 설정은 필요하지 않습니다.

			Swagger에서 Execute한 뒤 응답의 authorizationUrl을 브라우저 주소창에서 엽니다. Google 로그인을 완료하면 Google이 /auth/google/callback을 자동 호출하며, 성공 시 해당 브라우저 화면에 사용자 정보와 Access Token·Refresh Token이 JSON으로 반환됩니다.
			콜백 주소는 프론트엔드가 직접 호출하거나 요청 Body로 전달하는 API가 아닙니다.
			""")
	@ApiResponse(responseCode = "200", description = "인증 URL 조회 성공")
	public BaseResponse<OAuthAuthorizationResponse> getGoogleAuthorizationUrl() {
		return BaseResponse.success(new OAuthAuthorizationResponse(googleAuthorizationEntryPoint));
	}

	@PostMapping("/reissue")
	@Operation(summary = "토큰 재발급", description = """
			로컬 또는 Google 로그인에서 발급받은 활성 Refresh Token으로 새로운 Access Token과 Refresh Token을 발급합니다.

			Authorize 설정은 필요하지 않으며 refreshToken을 요청 Body에 입력합니다. 재발급에 성공하면 기존 Refresh Token은 사용 처리되므로 이후에는 응답으로 받은 새 Refresh Token을 저장하고 사용해야 합니다.
			""")
	@ApiResponses({
			@ApiResponse(responseCode = "200", description = "토큰 재발급 성공"),
			@ApiResponse(responseCode = "401", description = "AUTH40108~AUTH40110: Refresh Token 오류")
	})
	public BaseResponse<ReissueResponse> reissue(@Valid @RequestBody ReissueRequest request) {
		return BaseResponse.success(authService.reissue(request));
	}

	@GetMapping("/me")
	@Operation(summary = "현재 사용자 조회", description = """
			Access Token에서 이메일을 식별하여 현재 로그인 사용자의 정보를 반환합니다.

			로컬 로그인, Google 로그인 또는 토큰 재발급 응답의 accessToken을 Swagger 상단 **Authorize > bearerAuth**에 등록한 뒤 실행합니다. Refresh Token이나 이메일 인증용 임시 Access Token은 사용할 수 없습니다.
			""",
			security = @SecurityRequirement(name = "bearerAuth"))
	@ApiResponses({
			@ApiResponse(responseCode = "200", description = "사용자 조회 성공"),
			@ApiResponse(responseCode = "401", description = "AUTH40103~AUTH40105: Access Token 오류"),
			@ApiResponse(responseCode = "404", description = "AUTH40401: 사용자 없음")
	})
	public BaseResponse<MeResponse> me(Authentication authentication) {
		return BaseResponse.success(authService.me(authentication.getName()));
	}

	@PostMapping("/me/profile")
	@Operation(summary = "프로필 수정", description = """
			현재 사용자의 이메일, 비밀번호 또는 닉네임을 수정합니다. 변경하지 않을 필드는 Body에서 생략합니다.

			공통 준비: 로그인 응답의 accessToken을 Swagger 상단 **Authorize > bearerAuth**에 등록합니다.

			필드별 추가 조건:

			• 닉네임 변경: nickname만 입력할 수 있습니다.

			• 비밀번호 변경: 로컬 계정만 가능하며 currentPassword와 newPassword를 함께 입력합니다.

			• 이메일 변경: POST /auth/email/request와 POST /auth/email/verify를 각각 verificationType=LOGIN, purpose=EMAIL_CHANGE, 변경할 새 이메일로 호출합니다. 검증 응답의 temporaryAccessToken을 **X-Email-Verification-Token** 헤더에 입력하고 Body의 email에도 인증한 새 이메일을 입력합니다.

			이메일 변경용 임시 토큰을 bearerAuth에 넣으면 안 됩니다. bearerAuth에는 기존 로그인 Access Token을 유지합니다.
			""",
			security = @SecurityRequirement(name = "bearerAuth"))
	@ApiResponses({
			@ApiResponse(responseCode = "200", description = "프로필 수정 성공"),
			@ApiResponse(responseCode = "400", description = "AUTH40001, AUTH40004~AUTH40006, AUTH40017~AUTH40022: 프로필 입력 오류"),
			@ApiResponse(responseCode = "401", description = "AUTH40103~AUTH40105, AUTH40111~AUTH40114: 인증 토큰 또는 현재 비밀번호 오류"),
			@ApiResponse(responseCode = "403", description = "AUTH40301~AUTH40303: 이메일 인증 토큰 권한 오류"),
			@ApiResponse(responseCode = "404", description = "AUTH40401: 사용자 없음")
	})
	public BaseResponse<ProfileResponse> updateProfile(
			Authentication authentication,
			@Parameter(description = "이메일 변경 시 필요한 EMAIL_CHANGE 목적의 임시 Access Token",
					example = "eyJhbGciOiJIUzM4NCJ9.email-verification.signature")
			@RequestHeader(value = "X-Email-Verification-Token", required = false) String emailVerificationToken,
			@Valid @RequestBody ProfileUpdateRequest request) {
		return BaseResponse.success(authService.updateProfile(
				authentication.getName(),
				emailVerificationToken,
				request));
	}

	@PostMapping("/email/request")
	@Operation(summary = "이메일 인증코드 발송",
			description = """
			회원가입, 비밀번호 재설정 또는 이메일 변경에 사용할 10자리 영숫자 인증코드를 생성합니다. 인증코드의 유효시간은 30분입니다.

			요청 유형:

			• 회원가입: Authorize 없이 verificationType=NON_LOGIN, purpose=SIGNUP, email=가입할 이메일

			• 비밀번호 재설정: Authorize 없이 verificationType=NON_LOGIN, purpose=PASSWORD_RESET, email=가입된 이메일

			• 이메일 변경: 로그인 Access Token을 **Authorize > bearerAuth**에 등록하고 verificationType=LOGIN, purpose=EMAIL_CHANGE, email=변경할 새 이메일

			현재는 실제 이메일 전송 전 단계이므로 생성된 코드를 응답의 verificationCode에서 확인합니다. 같은 이메일과 목적의 코드를 다시 발급하면 이전 활성 코드는 폐기됩니다.
			""")
	@ApiResponses({
			@ApiResponse(responseCode = "200", description = "인증코드 발송 성공"),
			@ApiResponse(responseCode = "400", description = "AUTH40006, AUTH40009~AUTH40013: 요청값 오류"),
			@ApiResponse(responseCode = "401", description = "AUTH40103~AUTH40105: 로그인 인증 토큰 오류"),
			@ApiResponse(responseCode = "404", description = "AUTH40401: 사용자 없음"),
			@ApiResponse(responseCode = "429", description = "AUTH42901: 전송 횟수 초과")
	})
	public BaseResponse<EmailVerificationResponse> requestEmailVerification(
			Authentication authentication,
			@Valid @RequestBody EmailVerificationRequest request) {
		String authenticatedEmail = authentication == null ? null : authentication.getName();
		return BaseResponse.success(authService.requestEmailVerification(authenticatedEmail, request));
	}

	@PostMapping("/email/verify")
	@Operation(summary = "이메일 인증코드 검증",
			description = """
			POST /auth/email/request에서 생성된 인증코드를 검증하고 후속 기능에 사용할 임시 Access Token을 발급합니다.

			발송 요청과 동일한 verificationType, purpose, email을 입력하고 응답으로 확인한 verificationCode를 Body의 code에 입력합니다. verificationType=LOGIN인 이메일 변경 검증은 로그인 Access Token을 **Authorize > bearerAuth**에 등록해야 합니다. NON_LOGIN 검증은 Authorize가 필요하지 않습니다.

			성공 응답의 temporaryAccessToken 사용처:

			• SIGNUP: **Authorize > emailVerificationToken**에 등록한 뒤 POST /auth/signup 호출

			• PASSWORD_RESET: **Authorize > emailVerificationToken**에 등록한 뒤 POST /auth/password 호출

			• EMAIL_CHANGE: Swagger 상단 **Authorize > emailVerificationToken**에 등록한 뒤 POST /auth/me/profile 호출

			임시 Access Token의 유효시간은 30분이며 지정된 목적 외에는 사용할 수 없습니다.
			""")
	@ApiResponses({
			@ApiResponse(responseCode = "200", description = "인증코드 검증 성공"),
			@ApiResponse(responseCode = "400", description = "AUTH40006, AUTH40009~AUTH40016: 요청값 또는 인증코드 오류"),
			@ApiResponse(responseCode = "401", description = "AUTH40103~AUTH40105: 로그인 인증 토큰 오류"),
			@ApiResponse(responseCode = "429", description = "AUTH42902: 인증 시도 횟수 초과")
	})
	public BaseResponse<EmailVerificationVerifyResponse> verifyEmail(
			Authentication authentication,
			@Valid @RequestBody EmailVerificationVerifyRequest request) {
		String authenticatedEmail = authentication == null ? null : authentication.getName();
		return BaseResponse.success(authService.verifyEmailCode(authenticatedEmail, request));
	}

	@PostMapping("/password")
	@Operation(summary = "비밀번호 재설정", description = """
			이메일 인증을 완료한 사용자의 비밀번호를 새로운 비밀번호로 변경합니다.

			실행 전 준비:

			① POST /auth/email/request를 verificationType=NON_LOGIN, purpose=PASSWORD_RESET, 가입된 이메일로 호출합니다.

			② 응답의 verificationCode로 POST /auth/email/verify를 호출합니다.

			③ 검증 응답의 temporaryAccessToken을 Swagger 상단 **Authorize > emailVerificationToken**에 등록합니다. Bearer 접두사는 입력하지 않습니다.

			④ Body의 newPassword에 기존 비밀번호와 다른 새 비밀번호를 입력합니다.

			PASSWORD_RESET 목적의 임시 Access Token만 사용할 수 있으며, 토큰은 30분 동안 유효하고 변경 성공 시 사용 처리됩니다.
			""",
			security = @SecurityRequirement(name = "emailVerificationToken"))
	@ApiResponses({
			@ApiResponse(responseCode = "200", description = "비밀번호 재설정 성공"),
			@ApiResponse(responseCode = "400", description = "AUTH40004, AUTH40017~AUTH40018, AUTH40022: 비밀번호 재설정 오류"),
			@ApiResponse(responseCode = "401", description = "AUTH40111~AUTH40113: 임시 Access Token 오류"),
			@ApiResponse(responseCode = "403", description = "AUTH40301~AUTH40302: 토큰 타입 또는 목적 불일치"),
			@ApiResponse(responseCode = "404", description = "AUTH40401: 사용자 없음")
	})
	public BaseResponse<PasswordResetResponse> resetPassword(
			@Parameter(hidden = true)
			@RequestHeader(value = "X-Email-Verification-Token", required = false) String emailVerificationToken,
			@Valid @RequestBody PasswordResetRequest request) {
		return BaseResponse.success(authService.resetPassword(emailVerificationToken, request));
	}
}
