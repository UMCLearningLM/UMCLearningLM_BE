package com.umc.learninglm.domain.storage.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;

@Schema(description = "저장한 튜토리얼 항목 (진행률 포함)")
public record StorageTutorialResponse(
		@Schema(example = "1") Long tutorialId,
		@Schema(example = "AI로 자료조사 흐름 만들기") String title,
		@Schema(example = "검색·요약·정리 블록으로 리서치 흐름 완성") String summary,
		@Schema(description = "난이도 코드", example = "BEGINNER", allowableValues = {"BEGINNER", "BASIC", "ADVANCED"}) String difficulty,
		@Schema(description = "카테고리 정보") StorageCategoryResponse category,
		@Schema(description = "썸네일 이미지 URL (없으면 null)", nullable = true) String thumbnailUrl,
		@Schema(description = "진행 상태", example = "IN_PROGRESS", allowableValues = {"NOT_STARTED", "IN_PROGRESS", "COMPLETED"}) String status,
		@Schema(example = "4") int currentStepOrder,
		@Schema(example = "4") int totalSteps,
		@Schema(description = "진행률(%)", example = "75") int progressRate,
		@Schema(description = "이어하기용 flow 식별자 (NOT_STARTED면 null)", example = "77", nullable = true) Long flowId,
		@Schema(example = "2026-07-03T10:00:00") LocalDateTime createdAt,
		@Schema(example = "2026-07-04T21:20:00") LocalDateTime updatedAt
) {
}
