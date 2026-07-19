package com.umc.learninglm.domain.tutorial.controller;

import com.umc.learninglm.domain.tutorial.dto.request.TutorialProgressStartRequest;
import com.umc.learninglm.domain.tutorial.dto.request.TutorialProgressUpdateRequest;
import com.umc.learninglm.domain.tutorial.dto.response.TutorialDetailResponse;
import com.umc.learninglm.domain.tutorial.dto.response.TutorialListResponse;
import com.umc.learninglm.domain.tutorial.dto.response.TutorialProgressSaveResponse;
import com.umc.learninglm.domain.tutorial.dto.response.TutorialProgressStartResponse;
import com.umc.learninglm.domain.tutorial.dto.response.TutorialProgressUpdateResponse;
import com.umc.learninglm.domain.tutorial.dto.response.TutorialStepsResponse;
import com.umc.learninglm.global.common.BaseResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Tutorial", description = "공식 튜토리얼 API")
@RestController
@RequestMapping("/api/tutorials")
public class TutorialController {

	@GetMapping
	@Operation(summary = "튜토리얼 목록 조회", description = "공식 튜토리얼 목록을 검색어·카테고리·난이도로 필터링하여 조회합니다.")
	@ApiResponses({
			@ApiResponse(responseCode = "200", description = "튜토리얼 목록 조회 성공"),
			@ApiResponse(responseCode = "400", description = "TUTORIAL40002: 유효하지 않은 검색/필터 파라미터")
	})
	public BaseResponse<TutorialListResponse> getTutorials(
			@Parameter(description = "제목(title)·요약(summary) 검색어 (부분 일치)", example = "리서치")
			@RequestParam(required = false) String q,
			@Parameter(description = "카테고리 식별자", example = "1")
			@RequestParam(required = false) Long categoryId,
			@Parameter(description = "난이도 코드 (BEGINNER / BASIC / ADVANCED)", example = "BEGINNER")
			@RequestParam(required = false) String difficulty) {
		return BaseResponse.success(new TutorialListResponse(0, List.of()));
	}

	@GetMapping("/{tutorialId}")
	@Operation(summary = "튜토리얼 상세 조회", description = "단일 튜토리얼의 요약·블록 흐름·활용 사례·예시를 조회합니다.")
	@ApiResponses({
			@ApiResponse(responseCode = "200", description = "튜토리얼 상세 조회 성공"),
			@ApiResponse(responseCode = "404", description = "TUTORIAL40401: 존재하지 않는 튜토리얼")
	})
	@Parameter(name = "Authorization", description = "있으면 saved 여부 반영", in = ParameterIn.HEADER, required = false, example = "Bearer access-token")
	public BaseResponse<TutorialDetailResponse> getTutorialDetail(
			@PathVariable Long tutorialId,
			@RequestHeader(value = "Authorization", required = false) String authorization) {
		return BaseResponse.success(new TutorialDetailResponse(
				tutorialId, null, null, null, null, 0, null,
				List.of(), List.of(), null, List.of(), List.of(), null, false));
	}

	@GetMapping("/{tutorialId}/steps")
	@Operation(summary = "가이드 모드 단계 + 추천 블록 조회", description = "가이드 모드 진입 시 단계 목록과 단계별 추천 블록(필수 여부·기본 옵션 포함)을 일괄 조회합니다. Authorization 토큰이 있고 진행 정보가 있으면 progress를 포함합니다.")
	@ApiResponses({
			@ApiResponse(responseCode = "200", description = "단계 목록 조회 성공"),
			@ApiResponse(responseCode = "404", description = "TUTORIAL40401: 존재하지 않는 튜토리얼 / TUTORIAL40403: 단계 정보 없음")
	})
	@Parameter(name = "Authorization", description = "있으면 progress 포함", in = ParameterIn.HEADER, required = false, example = "Bearer access-token")
	public BaseResponse<TutorialStepsResponse> getTutorialSteps(
			@PathVariable Long tutorialId,
			@RequestHeader(value = "Authorization", required = false) String authorization) {
		return BaseResponse.success(new TutorialStepsResponse(
				tutorialId, null, 0, null, List.of()));
	}

	@PostMapping("/{tutorialId}/progress")
	@Operation(summary = "튜토리얼 저장", description = "튜토리얼을 내 저장소에 NOT_STARTED 상태로 저장합니다(북마크 전용). 학습 시작은 /progress/start를 사용합니다.", security = @SecurityRequirement(name = "bearerAuth"))
	@ApiResponses({
			@ApiResponse(responseCode = "200", description = "튜토리얼 저장 성공"),
			@ApiResponse(responseCode = "404", description = "TUTORIAL40401: 존재하지 않는 튜토리얼"),
			@ApiResponse(responseCode = "409", description = "TUTORIAL40901: 이미 저장된 튜토리얼"),
			@ApiResponse(responseCode = "401", description = "AUTH40103~AUTH40104: 인증 토큰 오류")
	})
	public BaseResponse<TutorialProgressSaveResponse> saveTutorial(@PathVariable Long tutorialId) {
		return BaseResponse.success(new TutorialProgressSaveResponse(
				tutorialId, 1, 5, 0, "NOT_STARTED", null));
	}

	@PostMapping("/{tutorialId}/progress/start")
	@Operation(summary = "튜토리얼 시작", description = "가이드 모드 학습을 시작합니다. 상태를 IN_PROGRESS로 전환하고 학습용 flow를 연결합니다. POST /flows 성공 직후 호출합니다.", security = @SecurityRequirement(name = "bearerAuth"))
	@ApiResponses({
			@ApiResponse(responseCode = "200", description = "튜토리얼 시작 성공"),
			@ApiResponse(responseCode = "400", description = "TUTORIAL40004: 해당 튜토리얼의 flow가 아님"),
			@ApiResponse(responseCode = "404", description = "TUTORIAL40401: 존재하지 않는 튜토리얼"),
			@ApiResponse(responseCode = "401", description = "AUTH40103~AUTH40104: 인증 토큰 오류")
	})
	public BaseResponse<TutorialProgressStartResponse> startTutorial(
			@PathVariable Long tutorialId,
			@Valid @RequestBody TutorialProgressStartRequest request) {
		return BaseResponse.success(new TutorialProgressStartResponse(
				tutorialId, 1, 5, 0, "IN_PROGRESS", request.flowId(), null));
	}

	@PatchMapping("/{tutorialId}/progress")
	@Operation(summary = "진행 단계 갱신 (이어하기)", description = "가이드 모드에서 단계 이동 시 현재 진행 단계를 갱신합니다.", security = @SecurityRequirement(name = "bearerAuth"))
	@ApiResponses({
			@ApiResponse(responseCode = "200", description = "진행 단계 갱신 성공"),
			@ApiResponse(responseCode = "400", description = "TUTORIAL40001: 유효하지 않은 진행 단계 값 / TUTORIAL40003: 학습이 시작되지 않음"),
			@ApiResponse(responseCode = "404", description = "TUTORIAL40401: 존재하지 않는 튜토리얼 / TUTORIAL40402: 저장(진행) 정보 없음"),
			@ApiResponse(responseCode = "401", description = "AUTH40103~AUTH40104: 인증 토큰 오류")
	})
	public BaseResponse<TutorialProgressUpdateResponse> updateProgress(
			@PathVariable Long tutorialId,
			@RequestBody TutorialProgressUpdateRequest request) {
		return BaseResponse.success(new TutorialProgressUpdateResponse(
				tutorialId, 4, 5, 60, "IN_PROGRESS", null));
	}

	@DeleteMapping("/{tutorialId}/progress")
	@Operation(summary = "튜토리얼 저장 해제", description = "저장한 튜토리얼을 저장 해제합니다(진행 기록 포함 삭제).", security = @SecurityRequirement(name = "bearerAuth"))
	@ApiResponses({
			@ApiResponse(responseCode = "200", description = "저장 해제 성공"),
			@ApiResponse(responseCode = "404", description = "TUTORIAL40401: 존재하지 않는 튜토리얼 / TUTORIAL40402: 저장(진행) 정보 없음"),
			@ApiResponse(responseCode = "401", description = "AUTH40103~AUTH40104: 인증 토큰 오류")
	})
	public BaseResponse<Void> deleteProgress(@PathVariable Long tutorialId) {
		return BaseResponse.success(null);
	}
}
