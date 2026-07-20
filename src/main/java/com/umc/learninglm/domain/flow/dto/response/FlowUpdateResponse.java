package com.umc.learninglm.domain.flow.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

@Schema(description = "흐름 저장 응답")
public record FlowUpdateResponse(
		@Schema(example = "12") Long flowId,
		@Schema(example = "COMPLETED", allowableValues = {"DRAFT", "COMPLETED"}) String status,
		@Schema(type = "string", format = "date-time", example = "2026-07-05T14:20:00") LocalDateTime updatedAt
) {
}
