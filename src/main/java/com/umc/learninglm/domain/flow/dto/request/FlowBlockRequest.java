package com.umc.learninglm.domain.flow.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

import java.util.Map;

@Schema(description = "흐름 저장 요청의 블록 배치 항목")
public record FlowBlockRequest(
		@Schema(example = "3")
		@NotNull(message = "블록 식별자는 필수입니다.")
		Long blockId,

		@Schema(example = "1")
		@NotNull(message = "블록 순서는 필수입니다.")
		Integer blockOrder,

		@Schema(description = "블록별 옵션 (자유 형식 JSON: option_schema에 따라 상이)")
		Map<String, Object> options,

		@Schema(example = "5", nullable = true)
		Long promptTemplateId
) {
}