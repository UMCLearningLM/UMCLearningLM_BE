package com.umc.learninglm.domain.storage.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;
import java.util.List;

@Schema(description = "내가 만든/복사한 흐름 항목")
public record StorageFlowResponse(
		@Schema(example = "501") Long flowId,
		@Schema(example = "제품 리뷰 요약기") String title,
		@Schema(description = "흐름 요약 (없으면 null)", nullable = true) String summary,
		@Schema(description = "난이도 코드", example = "BEGINNER", allowableValues = {"BEGINNER", "BASIC", "ADVANCED"}) String difficulty,
		@Schema(description = "카테고리 목록 (다중)") List<StorageCategoryResponse> categories,
		@Schema(description = "제작 모드 (이 목록엔 CREATE만 노출)", example = "CREATE", allowableValues = {"CREATE"}) String mode,
		@Schema(description = "공개 범위", example = "PRIVATE", allowableValues = {"PRIVATE", "PUBLIC"}) String visibility,
		@Schema(description = "흐름 상태", example = "COMPLETED", allowableValues = {"DRAFT", "COMPLETED"}) String status,
		@Schema(description = "원본 흐름 식별자 (복사본이면 원본 flowId, 내가 만든 흐름이면 null)", example = "101", nullable = true) Long originalFlowId,
		@Schema(description = "원본 작성자 닉네임 (복사본에만 존재, 그 외 null)", example = "운영자", nullable = true) String originalAuthorNickname,
		@Schema(example = "2026-07-03T10:00:00") LocalDateTime updatedAt
) {
}
