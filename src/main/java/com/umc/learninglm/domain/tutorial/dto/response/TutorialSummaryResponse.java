package com.umc.learninglm.domain.tutorial.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;
import java.util.List;

@Schema(description = "튜토리얼 목록 항목")
public record TutorialSummaryResponse(
		@Schema(example = "1") Long tutorialId,
		@Schema(example = "AI로 자료 조사 흐름 만들기") String title,
		@Schema(example = "검색·요약·정리 블록으로 리서치 흐름 완성") String summary,
		@Schema(description = "난이도 코드", example = "BEGINNER", allowableValues = {"BEGINNER", "BASIC", "ADVANCED"}) String difficulty,
		@Schema(description = "카테고리 목록 (다중)") List<TutorialCategoryResponse> categories,
		@Schema(description = "튜토리얼에 포함된 블록 수 (tutorial_blocks 집계값)", example = "4") int blockCount,
		@Schema(example = "15") Integer estimatedMinutes,
		@Schema(description = "썸네일 이미지 URL (없으면 null)", nullable = true) String thumbnailUrl,
		@Schema(example = "2026-07-01T10:00:00") LocalDateTime createdAt
) {
}
