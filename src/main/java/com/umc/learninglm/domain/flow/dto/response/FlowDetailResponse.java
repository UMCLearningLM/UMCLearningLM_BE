package com.umc.learninglm.domain.flow.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;
import java.util.List;

@Schema(description = "흐름 상세 응답 (블록 배치 + 옵션 포함)")
public record FlowDetailResponse(
		@Schema(example = "12") Long flowId,
		@Schema(example = "제품 리뷰 요약기") String title,
		@Schema(example = "리뷰 더미에서 장단점을 추출") String summary,
		@Schema(example = "여러 리뷰를 비교해 핵심만 정리") String purpose,
		@Schema(description = "난이도 코드", example = "BASIC", allowableValues = {"BEGINNER", "BASIC", "ADVANCED"}) String difficulty,
		@Schema(description = "카테고리 정보") FlowCategoryResponse category,
		@Schema(description = "생성 모드", example = "CREATE", allowableValues = {"GUIDED", "CREATE"}) String mode,
		@Schema(description = "흐름 유형", example = "USER") String flowType,
		@Schema(description = "공개 범위", example = "PRIVATE", allowableValues = {"PRIVATE", "PUBLIC"}) String visibility,
		@Schema(example = "DRAFT", allowableValues = {"DRAFT", "COMPLETED"}) String status,
		@Schema(example = "검색 블록 기간을 좁히면 정확도가 올라갑니다.", nullable = true) String authorNote,
		@Schema(example = "리뷰 100건을 항목별로 정리해줘", nullable = true) String exampleInput,
		@Schema(example = "비교 표 예시", nullable = true) String exampleResult,
		@Schema(description = "복사본이면 원본 흐름 식별자, 아니면 null", nullable = true) Long originFlowId,
		@Schema(description = "배치된 블록과 옵션 목록") List<FlowBlockResponse> blockFlow,
		@Schema(type = "string", format = "date-time", example = "2026-07-05T14:00:00") LocalDateTime createdAt,
		@Schema(type = "string", format = "date-time", example = "2026-07-05T14:20:00") LocalDateTime updatedAt
) {
}
