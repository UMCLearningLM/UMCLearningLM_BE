package com.umc.learninglm.domain.tutorial.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;

@Schema(description = "진행 단계 갱신 응답")
public record TutorialProgressUpdateResponse(
		@Schema(example = "1") Long tutorialId,
		@Schema(example = "4") int currentStepOrder,
		@Schema(example = "5") int totalSteps,
		@Schema(description = "진행률(%)", example = "60") int progressRate,
		@Schema(description = "진행 상태", example = "IN_PROGRESS", allowableValues = {"NOT_STARTED", "IN_PROGRESS", "COMPLETED"}) String status,
		@Schema(example = "2026-07-05T14:10:00") LocalDateTime updatedAt
) {
}
