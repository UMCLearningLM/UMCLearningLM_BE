package com.umc.learninglm.domain.tutorial.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

@Schema(description = "튜토리얼 학습 시작 요청")
public record TutorialProgressStartRequest(
		@Schema(description = "학습용 flow 식별자 (POST /flows 성공 응답의 flowId)", example = "77", requiredMode = Schema.RequiredMode.REQUIRED)
		@NotNull(message = "flowId가 필요합니다.")
		Long flowId
) {
}
