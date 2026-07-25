package com.umc.learninglm.domain.tutorial.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "진행 단계 갱신 요청")
public record TutorialProgressUpdateRequest(
		@Schema(description = "현재 진행 중 단계 번호 (1 ≤ 값 ≤ totalSteps)", example = "4", requiredMode = Schema.RequiredMode.REQUIRED) Integer currentStepOrder,
		@Schema(description = "완료 시에만 COMPLETED 전송 (미전송 시 상태 변경 없음)", example = "COMPLETED", allowableValues = {"COMPLETED"}, nullable = true) String status
) {
}
