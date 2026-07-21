package com.umc.learninglm.domain.tutorial.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;

@Schema(description = "튜토리얼 목록 조회 응답")
public record TutorialListResponse(
		@Schema(description = "필터 조건에 해당하는 전체 튜토리얼 수", example = "6") long totalElements,
		@Schema(description = "튜토리얼 목록 (최신순 createdAt DESC)") List<TutorialSummaryResponse> tutorials
) {
}
