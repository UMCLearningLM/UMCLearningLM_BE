package com.umc.learninglm.domain.storage.controller;

import com.umc.learninglm.domain.storage.dto.response.StorageFlowListResponse;
import com.umc.learninglm.domain.storage.dto.response.StorageTutorialListResponse;
import com.umc.learninglm.global.common.BaseResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Storage", description = "내 저장소 API")
@RestController
@RequestMapping("/api/storage")
public class StorageController {

	@GetMapping("/tutorials")
	@Operation(summary = "저장한 튜토리얼 목록 조회 (진행률 포함)", description = "로그인 사용자가 저장한 공식 튜토리얼 목록과 각 튜토리얼의 진행률을 조회합니다. 저장 해제된 튜토리얼은 목록에서 제외됩니다.", security = @SecurityRequirement(name = "bearerAuth"))
	@ApiResponses({
			@ApiResponse(responseCode = "200", description = "저장한 튜토리얼 목록 조회 성공"),
			@ApiResponse(responseCode = "401", description = "AUTH40103~AUTH40104: 인증 토큰 오류")
	})
	public BaseResponse<StorageTutorialListResponse> getSavedTutorials() {
		return BaseResponse.success(new StorageTutorialListResponse(0, List.of(), null));
	}

	@GetMapping("/flows")
	@Operation(summary = "내가 만든/복사한 흐름 목록 조회", description = "로그인 사용자가 스튜디오에서 만든(own) 또는 복사한(copied) 워크플로우 목록을 조회합니다. mode=GUIDED 흐름은 노출하지 않습니다.", security = @SecurityRequirement(name = "bearerAuth"))
	@ApiResponses({
			@ApiResponse(responseCode = "200", description = "흐름 목록 조회 성공"),
			@ApiResponse(responseCode = "400", description = "STORAGE40001: 유효하지 않은 조회 파라미터 (type 누락/코드 오류)"),
			@ApiResponse(responseCode = "401", description = "AUTH40103~AUTH40104: 인증 토큰 오류")
	})
	public BaseResponse<StorageFlowListResponse> getMyFlows(
			@Parameter(description = "조회 유형 (own: 내가 만든 흐름 / copied: 복사한 흐름)", required = true, example = "own")
			@RequestParam(required = false) String type) {
		return BaseResponse.success(new StorageFlowListResponse(0, List.of(), null));
	}
}
