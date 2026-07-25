package com.umc.learninglm.domain.tutorial.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "튜토리얼 프리셋 흐름의 블록")
public record TutorialBlockResponse(
		@Schema(example = "11") Long blockId,
		@Schema(example = "주제 입력하기") String name,
		@Schema(description = "블록 단계 (blocks.block_type)", example = "INPUT", allowableValues = {"INPUT", "CONTEXT", "PROCESS", "REVIEW", "OUTPUT"}) String stage,
		@Schema(example = "조사할 주제를 입력합니다") String description,
		@Schema(example = "검색 범위를 정하기 위해 필요해요") String reason
) {
}
