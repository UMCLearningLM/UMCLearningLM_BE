package com.umc.learninglm.domain.tutorial.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.Map;

@Schema(description = "단계별 추천 블록")
public record TutorialStepBlockResponse(
		@Schema(example = "300") Long tutorialBlockId,
		@Schema(example = "21") Long blockId,
		@Schema(example = "텍스트 입력") String name,
		@Schema(description = "블록 단계 (blocks.block_type)", example = "INPUT", allowableValues = {"INPUT", "CONTEXT", "PROCESS", "REVIEW", "OUTPUT"}) String stage,
		@Schema(example = "1") Integer blockOrder,
		@Schema(description = "필수 여부 (tutorial_blocks.required)", example = "true") boolean required,
		@Schema(description = "기본 옵션 (자유 형식 JSON: 블록별 상이)") Map<String, Object> defaultOptions
) {
}
