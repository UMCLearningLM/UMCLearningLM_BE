package com.umc.learninglm.domain.flow.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

@Schema(description = "흐름 생성 응답")
public record FlowCreateResponse(
		@Schema(example = "12") Long flowId,
		@Schema(description = "생성 모드", example = "CREATE", allowableValues = {"GUIDED", "CREATE"}) String mode,
		@Schema(description = "생성 직후 항상 DRAFT", example = "DRAFT", allowableValues = {"DRAFT", "COMPLETED"}) String status,
		@Schema(type = "string", format = "date-time", example = "2026-07-05T14:00:00") LocalDateTime createdAt
) {
}
