package com.umc.learninglm.domain.flow.controller;

import com.umc.learninglm.domain.flow.dto.request.FlowCreateRequest;
import com.umc.learninglm.domain.flow.dto.request.FlowPreviewRequest;
import com.umc.learninglm.domain.flow.dto.request.FlowUpdateRequest;
import com.umc.learninglm.domain.flow.dto.request.FlowVerifyRequest;
import com.umc.learninglm.domain.flow.dto.response.FlowBlockResponse;
import com.umc.learninglm.domain.flow.dto.response.FlowCategoryResponse;
import com.umc.learninglm.domain.flow.dto.response.FlowCreateResponse;
import com.umc.learninglm.domain.flow.dto.response.FlowDeleteResponse;
import com.umc.learninglm.domain.flow.dto.response.FlowDetailResponse;
import com.umc.learninglm.domain.flow.dto.response.FlowPreviewResponse;
import com.umc.learninglm.domain.flow.dto.response.FlowUpdateResponse;
import com.umc.learninglm.domain.flow.dto.response.FlowVerifyResponse;
import com.umc.learninglm.domain.flow.dto.response.FlowVerifyRuleResultResponse;
import com.umc.learninglm.domain.flow.dto.response.FlowVerifySummaryResponse;
import com.umc.learninglm.global.common.BaseResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Tag(name = "Flow", description = "흐름 생성/조회/저장/삭제 및 검수·예시 결과 생성 API")
@RestController
@RequestMapping("/api/flows")
public class FlowController {

	@PostMapping
	@Operation(summary = "흐름 생성 (스튜디오 진입)", description = "스튜디오 진입 시 빈 흐름(또는 튜토리얼/복사본 기반)을 생성합니다. 생성 직후 status는 항상 DRAFT입니다.",
			security = @SecurityRequirement(name = "bearerAuth"))
	@ApiResponses({
			@ApiResponse(responseCode = "200", description = "흐름 생성 성공"),
			@ApiResponse(responseCode = "404", description = "COMMON404: 존재하지 않는 tutorialId"),
			@ApiResponse(responseCode = "401", description = "AUTH40103~AUTH40104: Access Token 오류")
	})
	public BaseResponse<FlowCreateResponse> createFlow(@Valid @RequestBody FlowCreateRequest request) {
		return BaseResponse.success(new FlowCreateResponse(
				12L, request.mode(), "DRAFT", LocalDateTime.of(2026, 7, 5, 14, 0)));
	}

	@GetMapping("/{flowId}")
	@Operation(summary = "흐름 상세 (블록 배치 + 옵션)", description = "저장된 흐름의 블록 배치와 옵션을 포함해 전체를 불러옵니다. 편집/미리보기 공통이며, 공개 미리보기도 이 API를 사용합니다.",
			security = @SecurityRequirement(name = "bearerAuth"))
	@ApiResponses({
			@ApiResponse(responseCode = "200", description = "흐름 상세 조회 성공"),
			@ApiResponse(responseCode = "403", description = "FLOW40301: 소유자 불일치"),
			@ApiResponse(responseCode = "404", description = "FLOW40401: 존재하지 않는 흐름"),
			@ApiResponse(responseCode = "401", description = "AUTH40103~AUTH40104: Access Token 오류")
	})
	public BaseResponse<FlowDetailResponse> getFlow(@PathVariable Long flowId) {
		return BaseResponse.success(new FlowDetailResponse(
				flowId,
				"제품 리뷰 요약기",
				"리뷰 더미에서 장단점을 추출",
				"여러 리뷰를 비교해 핵심만 정리",
				"BASIC",
				new FlowCategoryResponse(1L, "RESEARCH", "자료조사"),
				"CREATE",
				"USER",
				"PRIVATE",
				"DRAFT",
				"검색 블록 기간을 좁히면 정확도가 올라갑니다.",
				"리뷰 100건을 항목별로 정리해줘",
				"비교 표 예시",
				null,
				List.of(
						new FlowBlockResponse(101L, 1L, "주제 입력하기", "INPUT", 1,
								Map.of("topic", "전기차 동향", "keywords", List.of("국내", "2025"),
										"topicScope", "NORMAL", "includeExcludeRange", ""),
								null, null, "USER", null),
						new FlowBlockResponse(102L, 5L, "핵심 내용 추출하기", "PROCESS", 1,
								Map.of("extractTargets", List.of("FACT", "DECISION"), "extractUnit", "ITEM",
										"extractStrength", 0.5, "maxItems", 5,
										"showEvidence", false, "showImportance", false),
								null, null, "USER", 5L)
				),
				LocalDateTime.of(2026, 7, 5, 14, 0),
				LocalDateTime.of(2026, 7, 5, 14, 20)));
	}

	@PutMapping("/{flowId}")
	@Operation(summary = "흐름 저장 (전체 갱신)", description = "블록 배치와 옵션 전체를 저장합니다. 자유 제작/복사본/튜토리얼 이어하기 저장을 공통 처리하며, 기존 blocks 전부를 교체하는 전체 갱신 방식입니다.",
			security = @SecurityRequirement(name = "bearerAuth"))
	@ApiResponses({
			@ApiResponse(responseCode = "200", description = "흐름 저장 성공"),
			@ApiResponse(responseCode = "400", description = "FLOW40003: options 파싱 실패"),
			@ApiResponse(responseCode = "403", description = "FLOW40301: 소유자 불일치"),
			@ApiResponse(responseCode = "404", description = "FLOW40401: 존재하지 않는 flowId / FLOW40402: 잘못된 blockId"),
			@ApiResponse(responseCode = "401", description = "AUTH40103~AUTH40104: Access Token 오류")
	})
	public BaseResponse<FlowUpdateResponse> updateFlow(
			@PathVariable Long flowId, @Valid @RequestBody FlowUpdateRequest request) {
		return BaseResponse.success(new FlowUpdateResponse(
				flowId, request.status(), LocalDateTime.of(2026, 7, 5, 14, 20)));
	}

	@DeleteMapping("/{flowId}")
	@Operation(summary = "흐름 삭제", description = "사용자 소유 흐름을 삭제합니다. 삭제 시 flow_blocks, flow_likes, flow_comments, flow_bookmarks, flow_tags가 함께 제거됩니다.",
			security = @SecurityRequirement(name = "bearerAuth"))
	@ApiResponses({
			@ApiResponse(responseCode = "200", description = "흐름 삭제 성공"),
			@ApiResponse(responseCode = "403", description = "FLOW40301: 소유자 불일치"),
			@ApiResponse(responseCode = "404", description = "FLOW40401: 존재하지 않는 flowId"),
			@ApiResponse(responseCode = "401", description = "AUTH40103~AUTH40104: Access Token 오류")
	})
	public BaseResponse<FlowDeleteResponse> deleteFlow(@PathVariable Long flowId) {
		return BaseResponse.success(new FlowDeleteResponse(flowId, true));
	}

	@PostMapping("/{flowId}/verify")
	@Operation(summary = "블록 조합 검수", description = "조합된 블록의 논리적 모순/필수 조건을 검수해 통과/미흡/대기로 채점합니다. 요청 body의 blocks 배열을 기준으로 검수하며, 결과는 저장하지 않습니다.",
			security = @SecurityRequirement(name = "bearerAuth"))
	@ApiResponses({
			@ApiResponse(responseCode = "200", description = "검수 성공 (블록 0개면 전체 PENDING 반환)"),
			@ApiResponse(responseCode = "403", description = "FLOW40301: 소유자 불일치"),
			@ApiResponse(responseCode = "404", description = "FLOW40401: 존재하지 않는 flowId"),
			@ApiResponse(responseCode = "401", description = "AUTH40103~AUTH40104: Access Token 오류")
	})
	public BaseResponse<FlowVerifyResponse> verifyFlow(
			@PathVariable Long flowId, @Valid @RequestBody FlowVerifyRequest request) {
		return BaseResponse.success(new FlowVerifyResponse(
				"FAIL",
				new FlowVerifySummaryResponse(1, 1, 0),
				List.of(
						new FlowVerifyRuleResultResponse(1L, "입력 노드 CORE 블록", "PASS",
								"입력 단계에 필수 블록 1개 이상이 포함되어야 합니다.",
								"\"텍스트 입력\" 블록이 포함되어 있습니다.", null, "INPUT"),
						new FlowVerifyRuleResultResponse(4L, "필수 슬롯 채움", "INSUFFICIENT",
								"각 노드의 required slot이 모두 채워져야 합니다.",
								"검토 노드 \"품질 검토\"의 기준이 비어 있습니다.",
								"검토 노드에서 기준(정확성·간결성)을 선택하세요.", "REVIEW")
				)));
	}

	@PostMapping("/{flowId}/preview")
	@Operation(summary = "예시 결과 생성 (Gemini+Fallback)", description = "조립한 흐름으로 프롬프트를 만들어 AI 예시 결과를 생성합니다. AI가 실패해도 200과 함께 TEMPLATE Fallback으로 응답합니다.",
			security = @SecurityRequirement(name = "bearerAuth"))
	@ApiResponses({
			@ApiResponse(responseCode = "200", description = "예시 결과 생성 성공 (AI 타임아웃/오류 시 TEMPLATE Fallback 반환)"),
			@ApiResponse(responseCode = "403", description = "FLOW40301: 소유자 불일치"),
			@ApiResponse(responseCode = "404", description = "FLOW40401: 존재하지 않는 흐름"),
			@ApiResponse(responseCode = "401", description = "AUTH40103~AUTH40104: Access Token 오류")
	})
	public BaseResponse<FlowPreviewResponse> previewFlow(
			@PathVariable Long flowId, @Valid @RequestBody FlowPreviewRequest request) {
		return BaseResponse.success(new FlowPreviewResponse(
				"리뷰 100건 분석 결과, 장점은 …", "AI", "gemini-2.0-flash"));
	}
}
