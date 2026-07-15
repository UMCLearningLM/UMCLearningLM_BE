package com.umc.learninglm.domain.flow.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "흐름 생성 요청")
public record FlowCreateRequest(
		@Schema(description = "생성 모드", example = "CREATE", allowableValues = {"GUIDED", "CREATE"})
		@NotBlank(message = "모드는 필수입니다.")
		String mode,

		@Schema(description = "가이드/참고 시작이면 튜토리얼 식별자, 아니면 null", example = "5", nullable = true)
		Long tutorialId,

		@Schema(description = "복사본이면 원본 흐름 식별자, 아니면 null", example = "7", nullable = true)
		Long originFlowId
) {
}