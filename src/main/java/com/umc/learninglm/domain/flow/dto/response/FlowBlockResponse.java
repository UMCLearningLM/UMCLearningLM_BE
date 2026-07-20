package com.umc.learninglm.domain.flow.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.Map;

@Schema(description = "흐름에 배치된 블록과 옵션")
public record FlowBlockResponse(
		@Schema(example = "101") Long flowBlockId,
		@Schema(example = "1") Long blockId,
		@Schema(example = "주제 입력하기") String name,
		@Schema(description = "블록 단계 (blocks.block_type)", example = "INPUT",
				allowableValues = {"INPUT", "CONTEXT", "PROCESS", "REVIEW", "OUTPUT"}) String stage,
		@Schema(example = "1") Integer blockOrder,
		@Schema(description = "블록별 옵션 (자유 형식 JSON: option_schema에 따라 상이)") Map<String, Object> options,
		@Schema(nullable = true) String inputValue,
		@Schema(nullable = true) String outputValue,
		@Schema(description = "실행 주체", example = "USER") String executionMode,
		@Schema(example = "5", nullable = true) Long promptTemplateId
) {
}
